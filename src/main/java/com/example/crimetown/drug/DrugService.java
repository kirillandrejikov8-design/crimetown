package com.example.crimetown.drug;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Applies effect cocktails, spawns silly particles while "high", then schedules the comedown. */
public final class DrugService {
    private static final Map<UUID, Long> HIGH_UNTIL = new HashMap<>();
    private static final Map<UUID, DrugType> HIGH_TYPE = new HashMap<>();

    private DrugService() {}

    public static void apply(ServerPlayer p, DrugType type) {
        for (DrugType.Fx fx : type.effects) {
            p.addEffect(new MobEffectInstance(fx.effect(), fx.ticks(), fx.amplifier()));
        }
        long now = p.level().getGameTime();
        HIGH_UNTIL.put(p.getUUID(), now + type.highTicks);
        HIGH_TYPE.put(p.getUUID(), type);
        p.sendSystemMessage(Component.literal("[" + type.label + "] " + type.message));
    }

    /** Called every server tick. */
    public static void tick(MinecraftServer server) {
        if (HIGH_UNTIL.isEmpty()) return;
        long now = server.overworld().getGameTime();
        Iterator<Map.Entry<UUID, Long>> it = HIGH_UNTIL.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> e = it.next();
            ServerPlayer p = server.getPlayerList().getPlayer(e.getKey());
            if (p == null) {
                it.remove();
                HIGH_TYPE.remove(e.getKey());
                continue;
            }
            if (now >= e.getValue()) {
                comedown(p);
                it.remove();
                HIGH_TYPE.remove(e.getKey());
            } else if (now % 4 == 0 && p.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.NOTE, p.getX(), p.getY() + 2.1, p.getZ(), 2, 0.4, 0.2, 0.4, 1.0);
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 1.0, p.getZ(), 3, 0.5, 0.7, 0.5, 0.0);
                sl.sendParticles(ParticleTypes.ENTITY_EFFECT, p.getX(), p.getY() + 1.0, p.getZ(), 2, 0.4, 0.8, 0.4, 0.0);
            }
        }
    }

    private static void comedown(ServerPlayer p) {
        p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 500, 2));
        p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 700, 2));
        p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 0));
        p.sendSystemMessage(Component.literal("[Отходняк] Реальность вернулась. Очень хочется есть. И лечь."));
    }
}
