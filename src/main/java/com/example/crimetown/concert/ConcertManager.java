package com.example.crimetown.concert;

import com.example.crimetown.economy.TownData;
import com.example.crimetown.npc.Npcs;
import com.example.crimetown.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

import java.util.concurrent.ThreadLocalRandom;

/**
 * One concert per in-game day, starting at dusk. The singer is a freshly named NPC; music is a
 * short melody played through note-block style sounds (no audio files required).
 */
public final class ConcertManager {
    private static final int START_TIME = 13000;
    private static final int END_TIME = 23000;
    private static final int DURATION = 4800;
    private static final int BEAT = 6;

    /** Semitones relative to the base pitch, range -12..12. */
    private static final int[] MELODY = {0, 4, 7, 12, 7, 4, 0, -5, -1, 2, 7, 2, -1, -5, 0, 0,
            5, 9, 12, 9, 5, 0, 2, 7, 11, 7, 2, -3, 0, 4, 7, 0};
    private static final int[] BASS = {-12, -12, -5, -5, -8, -8, -3, -3};

    private static final String[] FIRST = {"Дима", "Леночка", "Тимур", "Ксюша", "Гарик", "Мишель", "Серёга", "Алиса"};
    private static final String[] NICK = {"Жёлтый Утюг", "Мс. Хрусталь", "Дядя Бит", "Золотой Голос", "Тихий Мафиози",
            "Королева Слотов", "Лысый Бард", "Незеритовый Рэп"};

    private static boolean active;
    private static int ticks;
    private static String singerName = "";

    private ConcertManager() {}

    public static void tick(MinecraftServer server) {
        TownData d = TownData.get();
        if (d == null || !d.built) return;
        ServerLevel lv = server.overworld();
        long gt = lv.getGameTime();
        long day = gt / 24000L;
        long tod = gt % 24000L;

        if (!active && tod >= START_TIME && tod < END_TIME && d.lastConcertDay != day) {
            start(server, lv, d, day);
        }
        if (!active) return;

        ticks++;
        if (ticks >= DURATION || tod < START_TIME) {
            stop(server, lv, d);
            return;
        }
        music(lv, d, ticks);
        if (ticks % 40 == 0) gatherAudience(lv, d);
    }

    private static void start(MinecraftServer server, ServerLevel lv, TownData d, long day) {
        d.lastConcertDay = day;
        d.save();
        removeSingers(lv, d);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        singerName = FIRST[r.nextInt(FIRST.length)] + " «" + NICK[r.nextInt(NICK.length)] + "»";
        // random "skin": pick a different base mob for variety
        EntityType<? extends Mob> type = r.nextBoolean() ? EntityType.VILLAGER : EntityType.WITCH;
        Mob singer = Npcs.spawn(lv, type, d.cx + 0.5, d.cy + 2, d.cz - 1.5, Npcs.SINGER, singerName, true);
        if (singer != null) singer.setYRot(0f);
        active = true;
        ticks = 0;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal("[Город] Сегодня на сцене: " + singerName + "! Все на площадь!"));
        }
    }

    private static void stop(MinecraftServer server, ServerLevel lv, TownData d) {
        active = false;
        removeSingers(lv, d);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (d.inTown(p.getX(), p.getZ())) {
                p.sendSystemMessage(Component.literal("[Город] Концерт окончен. Спасибо, что были с нами!"));
            }
        }
    }

    private static void removeSingers(ServerLevel lv, TownData d) {
        AABB box = new AABB(d.cx - 60, d.cy - 5, d.cz - 60, d.cx + 60, d.cy + 25, d.cz + 60);
        for (Mob m : lv.getEntitiesOfClass(Mob.class, box, e -> e.getTags().contains(Npcs.SINGER))) {
            m.discard();
        }
    }

    private static void music(ServerLevel lv, TownData d, int t) {
        if (t % BEAT != 0) return;
        int beat = t / BEAT;
        double x = d.cx + 0.5;
        double y = d.cy + 3;
        double z = d.cz - 1.5;
        int semi = MELODY[beat % MELODY.length];
        lv.playSound(null, x, y, z, ModSounds.CONCERT_LEAD, SoundSource.RECORDS, 4.0f, pitch(semi));
        if (beat % 2 == 0) {
            int b = BASS[(beat / 2) % BASS.length];
            lv.playSound(null, x, y, z, ModSounds.CONCERT_BASS, SoundSource.RECORDS, 4.0f, pitch(b));
        }
        lv.sendParticles(ParticleTypes.NOTE, x + ThreadLocalRandom.current().nextDouble(-4, 4), y,
                z + ThreadLocalRandom.current().nextDouble(-1, 2), 2, 0.3, 0.4, 0.3, 1.0);
    }

    private static float pitch(int semi) {
        float p = (float) Math.pow(2.0, semi / 12.0);
        return Math.max(0.5f, Math.min(2.0f, p));
    }

    /** Residents walk to a ring in front of the stage. */
    private static void gatherAudience(ServerLevel lv, TownData d) {
        AABB box = new AABB(d.cx - 50, d.cy - 5, d.cz - 50, d.cx + 50, d.cy + 25, d.cz + 50);
        for (Mob m : lv.getEntitiesOfClass(Mob.class, box, e -> e.getTags().contains(Npcs.CITIZEN))) {
            double ang = (Math.floorMod(m.getUUID().hashCode(), 360)) * Math.PI / 180.0;
            double rad = 5 + Math.floorMod(m.getUUID().hashCode() >> 8, 4);
            double tx = d.cx + Math.cos(ang) * rad;
            double tz = d.cz + 4 + Math.abs(Math.sin(ang)) * rad; // south of the stage (audience side)
            if (m.distanceToSqr(tx, d.cy, tz) > 4.0) {
                m.getNavigation().moveTo(tx, d.cy, tz, 0.7);
            }
        }
    }
}
