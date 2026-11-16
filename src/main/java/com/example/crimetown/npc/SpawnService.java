package com.example.crimetown.npc;

import com.example.crimetown.economy.TownData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

import java.util.concurrent.ThreadLocalRandom;

/** Keeps the street populated: citizens on the plaza, dealers and thugs in the alleys. */
public final class SpawnService {
    private static final String[] NAMES = {"Вася", "Жорик", "Михалыч", "Тётя Люда", "Гоша", "Палыч", "Зинаида", "Кеша"};
    private static final String[] DEALER_NAMES = {"Барыга Тимоха", "Подозрительный Дима", "Дядя Шнырь", "Лысый Ашот"};
    /** Alley offsets relative to the town centre (x, z). */
    private static final int[][] ALLEYS = {{-11, -21}, {11, -21}, {-11, 21}, {11, 21}};

    private SpawnService() {}

    public static void tick(MinecraftServer server) {
        TownData d = TownData.get();
        if (d == null || !d.built) return;
        ServerLevel lv = server.overworld();
        if (!lv.hasChunkAt(new BlockPos(d.cx, d.cy, d.cz))) return;

        AABB box = new AABB(d.cx - 50, d.cy - 5, d.cz - 50, d.cx + 50, d.cy + 25, d.cz + 50);
        int citizens = lv.getEntitiesOfClass(Mob.class, box, e -> e.getTags().contains(Npcs.CITIZEN)).size();
        int thugs = lv.getEntitiesOfClass(Mob.class, box, e -> e.getTags().contains(Npcs.THUG)).size();
        int dealers = lv.getEntitiesOfClass(Mob.class, box, e -> e.getTags().contains(Npcs.DEALER)).size();
        ThreadLocalRandom r = ThreadLocalRandom.current();

        for (int i = citizens; i < 8; i++) {
            Npcs.spawn(lv, EntityType.VILLAGER, d.cx + r.nextInt(-12, 13) + 0.5, d.cy, d.cz + r.nextInt(8, 16) + 0.5,
                    Npcs.CITIZEN, NAMES[r.nextInt(NAMES.length)], false);
        }
        for (int i = thugs; i < 2; i++) {
            int[] a = ALLEYS[r.nextInt(ALLEYS.length)];
            Npcs.spawn(lv, EntityType.VILLAGER, d.cx + a[0] + 0.5, d.cy, d.cz + a[1] + 0.5,
                    Npcs.THUG, "Хулиган", false);
        }
        for (int i = dealers; i < 2; i++) {
            int[] a = ALLEYS[r.nextInt(ALLEYS.length)];
            Npcs.spawn(lv, EntityType.VILLAGER, d.cx + a[0] + 1.5, d.cy, d.cz + a[1] + 0.5,
                    Npcs.DEALER, DEALER_NAMES[r.nextInt(DEALER_NAMES.length)], true);
        }
    }
}
