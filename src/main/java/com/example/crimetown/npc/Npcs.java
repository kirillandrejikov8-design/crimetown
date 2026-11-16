package com.example.crimetown.npc;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

/**
 * Town NPCs are vanilla mobs marked with scoreboard tags. This keeps the mod free of custom
 * entity registration, renderers and models: they render with vanilla assets on any client,
 * and a dedicated server needs no extra client code.
 */
public final class Npcs {
    public static final String CASHIER = "ct_cashier";
    public static final String GUNSMITH = "ct_gunsmith";
    public static final String MAYOR = "ct_mayor";
    public static final String DEALER = "ct_dealer";
    public static final String GUARD = "ct_guard";
    public static final String THUG = "ct_thug";
    public static final String CITIZEN = "ct_citizen";
    public static final String SINGER = "ct_singer";
    public static final String BODY = "ct_body";

    private Npcs() {}

    public static <T extends Mob> T spawn(ServerLevel lv, EntityType<T> type, double x, double y, double z,
                                          String tag, String name, boolean fixed) {
        T e = type.create(lv, EntitySpawnReason.COMMAND);
        if (e == null) return null;
        e.setPos(x, y, z);
        e.addTag(tag);
        if (name != null) {
            e.setCustomName(Component.literal(name));
            e.setCustomNameVisible(true);
        }
        e.setPersistenceRequired();
        if (fixed) {
            e.setNoAi(true);
            e.setInvulnerable(true);
        }
        lv.addFreshEntity(e);
        return e;
    }
}
