package com.example.crimetown.network;

import com.example.crimetown.casino.SlotLogic;
import com.example.crimetown.economy.Currency;
import com.example.crimetown.economy.CurrencyHelper;
import com.example.crimetown.economy.EconomyService;
import com.example.crimetown.economy.TownData;
import com.example.crimetown.progression.Business;
import com.example.crimetown.progression.ProgressionService;
import com.example.crimetown.registry.ModSounds;
import com.example.crimetown.shop.Shop;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;

/** Server-authoritative handling of every screen action. */
public final class ActionHandler {
    private ActionHandler() {}

    private static int x100(double v) {
        return (int) Math.round(v * 100);
    }

    private static List<Integer> counts(ServerPlayer p) {
        return List.of(
                CurrencyHelper.count(p, Currency.GOLD),
                CurrencyHelper.count(p, Currency.DIAMOND),
                CurrencyHelper.count(p, Currency.NETHERITE));
    }

    // ---------- opening screens ----------

    public static void openBank(ServerPlayer p) {
        TownData d = TownData.get();
        List<Integer> v = new ArrayList<>(List.of(
                x100(d.diamondValue), x100(d.netheriteValue), x100(d.prevDiamond), x100(d.prevNetherite)));
        v.addAll(counts(p));
        ServerPlayNetworking.send(p, new ScreenPayload(ScreenIds.BANK, v));
    }

    public static void openSlot(ServerPlayer p) {
        sendSlot(p, 0, 0, 0, 0, 0, 1);
    }

    private static void sendSlot(ServerPlayer p, int r1, int r2, int r3, int delta, int cur, int bet) {
        List<Integer> v = new ArrayList<>(List.of(r1, r2, r3, delta, cur, bet));
        v.addAll(counts(p));
        ServerPlayNetworking.send(p, new ScreenPayload(ScreenIds.SLOT, v));
    }

    public static void openMayor(ServerPlayer p) {
        TownData d = TownData.get();
        String me = p.getUUID().toString();
        List<Integer> v = new ArrayList<>();
        for (Business b : Business.values()) {
            String owner = d.owners.get(b.id);
            int status = owner == null ? 0 : owner.equals(me) ? 1 : 2;
            v.add(status);
            v.add(ProgressionService.price(d, b));
        }
        v.add(CurrencyHelper.count(p, Currency.NETHERITE));
        v.add(ProgressionService.pendingFor(p));
        v.add(ProgressionService.isBoss(p) ? 1 : 0);
        ServerPlayNetworking.send(p, new ScreenPayload(ScreenIds.MAYOR, v));
    }

    public static void openShop(ServerPlayer p, int shop) {
        List<Integer> v = new ArrayList<>();
        v.add(shop);
        v.addAll(counts(p));
        ServerPlayNetworking.send(p, new ScreenPayload(ScreenIds.SHOP, v));
    }

    // ---------- actions ----------

    public static void handle(ServerPlayer p, int screen, int action, int arg) {
        TownData d = TownData.get();
        if (d == null || !d.built || !d.inTown(p.getX(), p.getZ())) return;

        switch (screen) {
            case ScreenIds.BANK -> {
                int from = action / 3;
                int to = action % 3;
                if (from < 0 || from > 2 || from == to || arg <= 0 || arg > 100000) return;
                p.sendSystemMessage(Component.literal(
                        EconomyService.exchange(p, Currency.byIndex(from), Currency.byIndex(to), arg)));
                openBank(p);
            }
            case ScreenIds.SLOT -> spin(p, action, arg);
            case ScreenIds.MAYOR -> {
                if (action == 10) {
                    p.sendSystemMessage(Component.literal(ProgressionService.claim(p)));
                } else if (action >= 0 && action < Business.values().length) {
                    p.sendSystemMessage(Component.literal(ProgressionService.buy(p, Business.values()[action])));
                }
                openMayor(p);
            }
            case ScreenIds.SHOP -> {
                if (action != Shop.ARMORY && action != Shop.DEALER) return;
                Shop.buy(p, action, arg);
                openShop(p, action);
            }
            default -> {
            }
        }
    }

    private static void spin(ServerPlayer p, int curIdx, int bet) {
        if (curIdx < 0 || curIdx > 2 || bet < 1 || bet > 64) return;
        Currency c = Currency.byIndex(curIdx);
        if (!CurrencyHelper.remove(p, c, bet)) {
            p.sendSystemMessage(Component.literal("[Казино] Не хватает: " + c.genitive));
            sendSlot(p, 0, 0, 0, 0, curIdx, bet);
            return;
        }
        int[] r = SlotLogic.spin();
        int mult = SlotLogic.multiplier(r);
        int win = bet * mult;
        if (win > 0) {
            // pay out in the same currency, in stacks
            CurrencyHelper.give(p, c, win);
        }
        if (p.level() instanceof ServerLevel sl) {
            var sound = mult >= SlotLogic.JACKPOT_MULT ? ModSounds.CASINO_JACKPOT : ModSounds.CASINO_WIN;
            if (mult > 1) sl.playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundSource.BLOCKS, 1f, 1f);
        }
        if (mult >= SlotLogic.JACKPOT_MULT) {
            p.level().getServer().getPlayerList().broadcastSystemMessage(Component.literal(
                    "[Казино] ДЖЕКПОТ! " + p.getName().getString() + " сорвал " + win + " " + c.genitive + "!"), false);
        }
        sendSlot(p, r[0], r[1], r[2], win - bet, curIdx, bet);
    }
}
