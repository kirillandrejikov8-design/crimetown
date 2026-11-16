package com.example.crimetown.weapon;

import com.example.crimetown.registry.ModItems;
import com.example.crimetown.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public enum GunType {
    //          name       dmg  pellets spread range recoil cooldown mag reload
    PISTOL("Пистолет", 6.0f, 1, 0.012, 48, 2.2f, 8, 12, 30,
            () -> ModItems.PISTOL_AMMO, () -> ModSounds.GUN_PISTOL),
    SHOTGUN("Дробовик", 3.0f, 8, 0.075, 22, 6.5f, 26, 6, 45,
            () -> ModItems.SHOTGUN_SHELLS, () -> ModSounds.GUN_SHOTGUN),
    RIFLE("Автомат", 4.0f, 1, 0.03, 64, 1.4f, 4, 30, 55,
            () -> ModItems.RIFLE_AMMO, () -> ModSounds.GUN_RIFLE);

    public final String label;
    public final float damage;
    public final int pellets;
    public final double spread;
    public final double range;
    public final float recoil;
    public final int cooldown;
    public final int magazine;
    public final int reloadTicks;
    private final Supplier<Item> ammo;
    private final Supplier<SoundEvent> sound;

    GunType(String label, float damage, int pellets, double spread, double range, float recoil,
            int cooldown, int magazine, int reloadTicks, Supplier<Item> ammo, Supplier<SoundEvent> sound) {
        this.label = label;
        this.damage = damage;
        this.pellets = pellets;
        this.spread = spread;
        this.range = range;
        this.recoil = recoil;
        this.cooldown = cooldown;
        this.magazine = magazine;
        this.reloadTicks = reloadTicks;
        this.ammo = ammo;
        this.sound = sound;
    }

    public Item ammoItem() {
        return ammo.get();
    }

    public SoundEvent sound() {
        return sound.get();
    }
}
