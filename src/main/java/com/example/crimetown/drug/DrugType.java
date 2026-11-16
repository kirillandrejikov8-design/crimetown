package com.example.crimetown.drug;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

/** Satirical "substances": a ridiculous effect cocktail followed by a comedown. */
public enum DrugType {
    SUGAR("Сверхъестественный сахар",
            "Мир стал приторно-сладким. Ты — Флэш! Почти.",
            400,
            new Fx(MobEffects.SPEED, 400, 4),
            new Fx(MobEffects.NAUSEA, 240, 0),
            new Fx(MobEffects.LEVITATION, 60, 0),
            new Fx(MobEffects.JUMP_BOOST, 400, 2)),
    ENERGY("Подозрительный энергетик",
            "Сердце стучит в ритме техно. Глаза светятся. Это нормально.",
            600,
            new Fx(MobEffects.HASTE, 600, 3),
            new Fx(MobEffects.SPEED, 600, 2),
            new Fx(MobEffects.STRENGTH, 600, 1),
            new Fx(MobEffects.GLOWING, 600, 0)),
    CRYSTALS("Кристаллы веселья",
            "Хихи-хаха! Пол — это лава, а лава — это пол!",
            300,
            new Fx(MobEffects.LEVITATION, 80, 1),
            new Fx(MobEffects.NAUSEA, 300, 0),
            new Fx(MobEffects.JUMP_BOOST, 300, 5),
            new Fx(MobEffects.SLOW_FALLING, 600, 0),
            new Fx(MobEffects.REGENERATION, 200, 1));

    public record Fx(Holder<MobEffect> effect, int ticks, int amplifier) {}

    public final String label;
    public final String message;
    /** How long the "high" (particles) lasts before the comedown hits, in ticks. */
    public final int highTicks;
    public final Fx[] effects;

    DrugType(String label, String message, int highTicks, Fx... effects) {
        this.label = label;
        this.message = message;
        this.highTicks = highTicks;
        this.effects = effects;
    }
}
