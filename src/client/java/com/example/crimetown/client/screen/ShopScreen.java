package com.example.crimetown.client.screen;

import com.example.crimetown.network.ScreenIds;
import com.example.crimetown.shop.Shop;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** values: [shopId, gold, diamonds, netherite] */
public class ShopScreen extends CtScreen {
    public ShopScreen(List<Integer> values) {
        super("Магазин", values);
    }

    @Override
    protected void init() {
        int shop = v.get(0);
        List<Shop.Offer> offers = Shop.offers(shop);
        int y = top(offers.size() / 2 + 3);
        label(Component.literal(Shop.title(shop).toUpperCase()), y);
        label(Component.literal("У вас: " + v.get(1) + " зол. / " + v.get(2) + " алм. / " + v.get(3) + " незер."), y + 24);
        for (int i = 0; i < offers.size(); i++) {
            Shop.Offer o = offers.get(i);
            final int idx = i;
            String name = new ItemStack(o.item()).getHoverName().getString();
            String text = name + (o.amount() > 1 ? " x" + o.amount() : "") + " — " + o.price() + " " + o.currency().genitive.toLowerCase();
            button(Component.literal(text), this.width / 2 - 130, y + 48 + i * 22, 260, () -> send(ScreenIds.SHOP, shop, idx));
        }
        button(Component.literal("Закрыть"), this.width / 2 - 50, y + 48 + offers.size() * 22 + 6, 100, this::onClose);
    }
}
