package com.example.crimetown.client.screen;

import com.example.crimetown.network.ScreenIds;
import com.example.crimetown.progression.Business;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * values: for each business [status(0 free, 1 mine, 2 someone else), price],
 * then [netherite in inventory, unclaimed income, isBoss].
 */
public class MayorScreen extends CtScreen {
    public MayorScreen(List<Integer> values) {
        super("Мэрия", values);
    }

    @Override
    protected void init() {
        int y = top(7);
        boolean boss = v.get(8) == 1;
        label(boss
                ? Component.literal("МЭРИЯ — вы БОСС города!").withStyle(ChatFormatting.GOLD)
                : Component.literal("МЭРИЯ — выкупите все заведения, чтобы стать Боссом"), y);
        label(Component.literal("Незерита в инвентаре: " + v.get(6)), y + 24);

        Business[] all = Business.values();
        for (int i = 0; i < all.length; i++) {
            int status = v.get(i * 2);
            int price = v.get(i * 2 + 1);
            final int idx = i;
            String text;
            if (status == 1) text = all[i].label + ": ВАША ДОЛЯ";
            else if (status == 2) text = all[i].label + ": перекупить за " + price + " незерита";
            else text = all[i].label + ": выкупить за " + price + " незерита";
            var b = button(Component.literal(text), this.width / 2 - 130, y + 48 + i * 24, 260,
                    () -> send(ScreenIds.MAYOR, idx, 0));
            b.active = status != 1;
        }

        int pending = v.get(7);
        button(Component.literal("Забрать доход: " + pending + " незерита"), this.width / 2 - 130, y + 48 + all.length * 24 + 6, 260,
                () -> send(ScreenIds.MAYOR, 10, 0)).active = pending > 0;
        button(Component.literal("Закрыть"), this.width / 2 - 50, y + 48 + all.length * 24 + 32, 100, this::onClose);
    }
}
