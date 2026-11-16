package com.example.crimetown.economy;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.ThreadLocalRandom;

/** Dynamic exchange rates and the atomic exchange transaction. */
public final class EconomyService {
    public static final double BASE_DIAMOND = 8;
    public static final double BASE_NETHERITE = 64;
    /** Bank commission. Without it, cycling A -> B -> A could mint free money. */
    public static final double SPREAD = 0.05;

    private EconomyService() {}

    public static double value(Currency c, TownData d) {
        return switch (c) {
            case GOLD -> 1.0;
            case DIAMOND -> d.diamondValue;
            case NETHERITE -> d.netheriteValue;
        };
    }

    /** Called every tick (cheap); rates change once per in-game day. */
    public static void dailyTick(MinecraftServer server, TownData d) {
        ServerLevel lv = server.overworld();
        long day = lv.getGameTime() / 24000L;
        if (d.lastDay == day) return;
        boolean first = d.lastDay < 0;
        d.lastDay = day;

        d.prevDiamond = d.diamondValue;
        d.prevNetherite = d.netheriteValue;
        d.diamondValue = walk(d.diamondValue, BASE_DIAMOND);
        d.netheriteValue = walk(d.netheriteValue, BASE_NETHERITE);

        com.example.crimetown.progression.ProgressionService.dailyIncome(d);
        d.save();

        if (!first) {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                p.sendSystemMessage(Component.literal(
                        "[Банк] Новые курсы дня: Алмаз = " + fmt(d.diamondValue)
                                + " зол., Незерит = " + fmt(d.netheriteValue) + " зол."));
            }
        }
    }

    private static double walk(double cur, double base) {
        double step = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.24; // +-12% per day
        double next = cur * (1.0 + step);
        return Math.max(base * 0.7, Math.min(base * 1.3, next));
    }

    public static String fmt(double v) {
        return String.format(java.util.Locale.ROOT, "%.2f", v);
    }

    /** Returns a human-readable result message. */
    public static String exchange(ServerPlayer p, Currency from, Currency to, int amount) {
        TownData d = TownData.get();
        double out = amount * value(from, d) / value(to, d) * (1.0 - SPREAD);
        int n = (int) Math.floor(out);
        if (n < 1) return "[Банк] Слишком маленькая сумма: после комиссии вы бы ничего не получили.";
        if (!CurrencyHelper.remove(p, from, amount)) {
            return "[Банк] Не хватает: " + from.genitive + " (нужно " + amount + ").";
        }
        CurrencyHelper.give(p, to, n);
        return "[Банк] Обмен: " + amount + " " + from.genitive + " -> " + n + " " + to.genitive + ".";
    }
}
