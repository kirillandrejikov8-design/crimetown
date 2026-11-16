package com.example.crimetown.economy;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Counting, removing and handing out currency items. Server-side use only. */
public final class CurrencyHelper {
    private CurrencyHelper() {}

    public static int count(Player p, Currency c) {
        int n = 0;
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(c.item)) n += s.getCount();
        }
        return n;
    }

    /** Atomic: removes exactly {@code amount} or nothing. */
    public static boolean remove(Player p, Currency c, int amount) {
        if (amount <= 0) return true;
        if (count(p, c) < amount) return false;
        int left = amount;
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize() && left > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(c.item)) {
                int take = Math.min(left, s.getCount());
                s.shrink(take);
                left -= take;
            }
        }
        return true;
    }

    public static void give(Player p, Currency c, int amount) {
        int left = amount;
        while (left > 0) {
            int n = Math.min(left, 64);
            ItemStack st = new ItemStack(c.item, n);
            if (!p.getInventory().add(st)) {
                p.drop(st, false);
            }
            left -= n;
        }
    }
}
