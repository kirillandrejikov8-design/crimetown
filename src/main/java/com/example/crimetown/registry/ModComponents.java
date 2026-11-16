package com.example.crimetown.registry;

import com.example.crimetown.CrimeTown;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

/** Item data components. */
public final class ModComponents {
    /** Rounds currently loaded in a gun. */
    public static final DataComponentType<Integer> AMMO = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(CrimeTown.MOD_ID, "ammo"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    private ModComponents() {}

    public static void init() {}
}
