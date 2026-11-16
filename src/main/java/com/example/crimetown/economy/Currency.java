package com.example.crimetown.economy;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Three-tier town currency made of vanilla items. */
public enum Currency {
    GOLD(Items.GOLD_INGOT, "Золото", "Золота"),
    DIAMOND(Items.DIAMOND, "Алмаз", "Алмазов"),
    NETHERITE(Items.NETHERITE_INGOT, "Незерит", "Незерита");

    public final Item item;
    public final String label;
    public final String genitive;

    Currency(Item item, String label, String genitive) {
        this.item = item;
        this.label = label;
        this.genitive = genitive;
    }

    public static Currency byIndex(int i) {
        Currency[] v = values();
        return v[Math.floorMod(i, v.length)];
    }
}
