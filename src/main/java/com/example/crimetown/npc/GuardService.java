package com.example.crimetown.npc;

import com.example.crimetown.economy.TownData;
import com.example.crimetown.progression.ProgressionService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Bouncers (neutral) and thugs (pickpockets). */
public final class GuardService {
    private static final Map<UUID, Long> STEAL_COOLDOWN = new HashMap<>();

    private GuardService() {}

    /** Player drew/fired a weapon or hit a resident: nearby bouncers turn on them. */
    public static void alert(ServerPlayer p) {
        TownData d = TownData.get();
        if (d == null || !d.inTown(p.getX(), p.getZ())) return;
        if (ProgressionService.isBoss(p)) return; // the Boss owns the place
        if (!(p.level() instanceof ServerLevel lv)) return;
        AABB box = p.getBoundingBox().inflate(40);
        boolean any = false;
        for (Mob g : lv.getEntitiesOfClass(Mob.class, box, e -> e.getTags().contains(Npcs.GUARD))) {
            g.setTarget(p);
            any = true;
        }
        if (any) p.displayClientMessage(Component.literal("Вышибалы заметили вас!"), true);
    }

    /** Pickpocket logic, run every few ticks. */
    public static void thugTick(net.minecraft.server.MinecraftServer server) {
        TownData d = TownData.get();
        if (d == null || !d.built) return;
        long now = server.overworld().getGameTime();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (!(p.level() instanceof ServerLevel lv) || !d.inTown(p.getX(), p.getZ())) continue;
            if (p.isCreative() || p.isSpectator() || ProgressionService.isBoss(p)) continue;
            if (STEAL_COOLDOWN.getOrDefault(p.getUUID(), 0L) > now) continue;
            for (Mob thug : lv.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(2.0), e -> e.getTags().contains(Npcs.THUG))) {
                int gold = com.example.crimetown.economy.CurrencyHelper.count(p, com.example.crimetown.economy.Currency.GOLD);
                if (gold <= 0) break;
                int take = Math.min(gold, 1 + ThreadLocalRandom.current().nextInt(4));
                com.example.crimetown.economy.CurrencyHelper.remove(p, com.example.crimetown.economy.Currency.GOLD, take);
                thug.addTag("ct_loot_" + take);
                p.sendSystemMessage(Component.literal("[Хулиган] Ловкость рук! У вас стащили золота: " + take + ". Догоните и верните!"));
                STEAL_COOLDOWN.put(p.getUUID(), now + 200);
                double ang = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
                thug.getNavigation().moveTo(thug.getX() + Math.cos(ang) * 18, thug.getY(), thug.getZ() + Math.sin(ang) * 18, 1.4);
                break;
            }
        }
    }

    /** When a thug dies, he drops what he stole. */
    public static int lootOf(net.minecraft.world.entity.Entity e) {
        int total = 0;
        for (String t : e.getTags()) {
            if (t.startsWith("ct_loot_")) {
                try {
                    total += Integer.parseInt(t.substring("ct_loot_".length()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return total;
    }

    public static boolean isThugType(Mob m) {
        return m.getType() == EntityType.VILLAGER && m.getTags().contains(Npcs.THUG);
    }
}
