package com.example.crimetown.progression;

import com.example.crimetown.economy.Currency;
import com.example.crimetown.economy.CurrencyHelper;
import com.example.crimetown.economy.TownData;
import com.example.crimetown.npc.Npcs;
import com.example.crimetown.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

/** Buying out the town: shares, the Boss title, passive income, bodyguards. */
public final class ProgressionService {
    private ProgressionService() {}

    /** Re-buying from the current owner gets 25 percent more expensive each time. */
    public static int price(TownData d, Business b) {
        int n = d.buyCount.getOrDefault(b.id, 0);
        return b.basePrice + (b.basePrice * n) / 4;
    }

    public static String buy(ServerPlayer p, Business b) {
        TownData d = TownData.get();
        String me = p.getUUID().toString();
        if (me.equals(d.owners.get(b.id))) return "[Мэрия] Эта доля уже ваша.";
        int price = price(d, b);
        if (!CurrencyHelper.remove(p, Currency.NETHERITE, price)) {
            return "[Мэрия] Нужно " + price + " незеритовых слитков.";
        }
        String prev = d.owners.get(b.id);
        if (prev != null) d.pending.merge(prev, price / 2, Integer::sum); // previous owner is paid out
        d.owners.put(b.id, me);
        d.buyCount.merge(b.id, 1, Integer::sum);
        recompute(p.level().getServer(), p);
        d.save();
        return "[Мэрия] Вы выкупили долю: " + b.label + " за " + price + " незерита.";
    }

    public static String claim(ServerPlayer p) {
        TownData d = TownData.get();
        int n = d.pending.getOrDefault(p.getUUID().toString(), 0);
        if (n <= 0) return "[Мэрия] Для вас пока ничего нет.";
        d.pending.put(p.getUUID().toString(), 0);
        CurrencyHelper.give(p, Currency.NETHERITE, n);
        d.save();
        return "[Мэрия] Получено дохода: " + n + " незерита.";
    }

    public static int pendingFor(ServerPlayer p) {
        return TownData.get().pending.getOrDefault(p.getUUID().toString(), 0);
    }

    /** Called once per in-game day. */
    public static void dailyIncome(TownData d) {
        boolean bossActive = d.boss != null;
        for (Business b : Business.values()) {
            String owner = d.owners.get(b.id);
            if (owner == null) continue;
            int inc = b.dailyIncome;
            if (bossActive && owner.equals(d.boss)) inc = inc * 3 / 2; // Boss bonus
            d.pending.merge(owner, inc, Integer::sum);
        }
    }

    public static boolean isBoss(ServerPlayer p) {
        TownData d = TownData.get();
        return d != null && p.getUUID().toString().equals(d.boss);
    }

    /** Re-evaluates who (if anyone) owns 100 percent of the town. */
    public static void recompute(MinecraftServer server, ServerPlayer trigger) {
        TownData d = TownData.get();
        String first = d.owners.get(Business.BANK.id);
        boolean all = first != null;
        for (Business b : Business.values()) {
            if (!first_equals(first, d.owners.get(b.id))) all = false;
        }
        String newBoss = all ? first : null;
        String old = d.boss;
        d.boss = newBoss;
        if (newBoss != null && !newBoss.equals(old)) {
            ServerPlayer boss = server.getPlayerList().getPlayer(UUID.fromString(newBoss));
            if (boss != null) crown(server, boss, d);
        }
        if (old != null && !old.equals(newBoss)) {
            server.getPlayerList().broadcastSystemMessage(Component.literal("[Город] Босс свергнут! Власть снова ничья."), false);
        }
    }

    private static boolean first_equals(String a, String b) {
        return a != null && a.equals(b);
    }

    private static void crown(MinecraftServer server, ServerPlayer boss, TownData d) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(
                "[Город] " + boss.getName().getString() + " выкупил весь город и стал БОССОМ!"), false);
        boss.sendSystemMessage(Component.literal(
                "[Босс] Теперь у вас пассивный доход x1.5, личная охрана и Корона Олигарха."));
        String id = boss.getUUID().toString();
        if (!d.crownGiven.contains(id)) {
            d.crownGiven.add(id);
            ItemStack crown = new ItemStack(ModItems.OLIGARCH_CROWN);
            if (!boss.getInventory().add(crown)) boss.drop(crown, false);
        }
    }

    /** Keeps two golem bodyguards next to each online Boss. */
    public static void bodyguardTick(MinecraftServer server) {
        TownData d = TownData.get();
        ServerLevel lv = server.overworld();
        if (d == null || !d.built) return;

        // dismiss bodyguards whose owner is no longer Boss
        AABB all = new AABB(d.cx - 400, lv.getMinY(), d.cz - 400, d.cx + 400, lv.getMaxY(), d.cz + 400);
        for (Mob m : lv.getEntitiesOfClass(Mob.class, all, e -> e.getTags().contains(Npcs.BODY))) {
            boolean valid = d.boss != null && m.getTags().contains(Npcs.BODY + "_" + d.boss);
            if (!valid) m.discard();
        }

        if (d.boss == null) return;
        ServerPlayer boss = server.getPlayerList().getPlayer(UUID.fromString(d.boss));
        if (boss == null || !(boss.level() instanceof ServerLevel bl)) return;

        String tag = Npcs.BODY + "_" + d.boss;
        List<Mob> guards = bl.getEntitiesOfClass(Mob.class, boss.getBoundingBox().inflate(96), e -> e.getTags().contains(tag));
        for (Mob g : guards) {
            double dist = g.distanceTo(boss);
            if (dist > 40) {
                g.setPos(boss.getX() + 1.5, boss.getY(), boss.getZ() + 1.5);
            } else if (dist > 7) {
                g.getNavigation().moveTo(boss.getX(), boss.getY(), boss.getZ(), 1.2);
            }
        }
        for (int i = guards.size(); i < 2; i++) {
            Mob g = Npcs.spawn(bl, EntityType.IRON_GOLEM, boss.getX() + 2, boss.getY(), boss.getZ() + 2,
                    Npcs.BODY, "Охрана Босса", false);
            if (g != null) g.addTag(tag);
        }
    }
}
