package com.example.crimetown.shop;

import com.example.crimetown.economy.Currency;
import com.example.crimetown.economy.CurrencyHelper;
import com.example.crimetown.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Offer lists shared by server (validation) and client (display). */
public final class Shop {
    public static final int ARMORY = 0;
    public static final int DEALER = 1;

    public record Offer(Item item, int amount, Currency currency, int price) {}

    private Shop() {}

    public static String title(int shop) {
        return shop == ARMORY ? "Оружейная лавка" : "Барыга (psst...)";
    }

    public static List<Offer> offers(int shop) {
        if (shop == ARMORY) {
            return List.of(
                    new Offer(ModItems.PISTOL, 1, Currency.DIAMOND, 5),
                    new Offer(ModItems.SHOTGUN, 1, Currency.DIAMOND, 12),
                    new Offer(ModItems.RIFLE, 1, Currency.NETHERITE, 2),
                    new Offer(ModItems.PISTOL_AMMO, 24, Currency.GOLD, 10),
                    new Offer(ModItems.SHOTGUN_SHELLS, 12, Currency.GOLD, 12),
                    new Offer(ModItems.RIFLE_AMMO, 30, Currency.DIAMOND, 2));
        }
        return List.of(
                new Offer(ModItems.SUGAR, 1, Currency.GOLD, 6),
                new Offer(ModItems.ENERGY_DRINK, 1, Currency.DIAMOND, 3),
                new Offer(ModItems.CRYSTALS, 1, Currency.NETHERITE, 1));
    }

    public static void buy(ServerPlayer p, int shop, int index) {
        List<Offer> list = offers(shop);
        if (index < 0 || index >= list.size()) return;
        Offer o = list.get(index);
        if (!CurrencyHelper.remove(p, o.currency(), o.price())) {
            p.sendSystemMessage(Component.literal("Не хватает: " + o.currency().genitive + " (нужно " + o.price() + ")"));
            return;
        }
        ItemStack st = new ItemStack(o.item(), o.amount());
        if (!p.getInventory().add(st)) p.drop(st, false);
        p.sendSystemMessage(Component.literal("Покупка совершена: " + new ItemStack(o.item()).getHoverName().getString()));
    }
}
