package com.example.crimetown.registry;

import com.example.crimetown.CrimeTown;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Custom sound events. They are mapped to vanilla sounds in assets/crimetown/sounds.json
 * ("type": "event"), so the mod needs no .ogg files; drop your own .ogg in later if desired.
 */
public final class ModSounds {
    public static final SoundEvent GUN_PISTOL = register("gun_pistol");
    public static final SoundEvent GUN_SHOTGUN = register("gun_shotgun");
    public static final SoundEvent GUN_RIFLE = register("gun_rifle");
    public static final SoundEvent GUN_RELOAD = register("gun_reload");
    public static final SoundEvent GUN_EMPTY = register("gun_empty");
    public static final SoundEvent CASINO_WIN = register("casino_win");
    public static final SoundEvent CASINO_JACKPOT = register("casino_jackpot");
    public static final SoundEvent CONCERT_LEAD = register("concert_lead");
    public static final SoundEvent CONCERT_BASS = register("concert_bass");

    private ModSounds() {}

    private static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(CrimeTown.MOD_ID, name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {}
}
