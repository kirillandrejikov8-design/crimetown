package com.example.crimetown.client.screen;

import com.example.crimetown.economy.Currency;
import com.example.crimetown.network.ScreenIds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/** values: [diamond*100, netherite*100, prevDiamond*100, prevNetherite*100, gold, diamonds, netherite] */
public class BankScreen extends CtScreen {
    private static final int[] AMOUNTS = {1, 5, 10, 32, 64};
    private int amountIdx = 0;

    public BankScreen(List<Integer> values) {
        super("Банк", values);
    }

    private static String fmt(int x100) {
        return String.format(Locale.ROOT, "%.2f", x100 / 100.0);
    }

    private static Component arrow(int now, int prev) {
        if (now > prev) return Component.literal(" ▲").withStyle(ChatFormatting.GREEN);
        if (now < prev) return Component.literal(" ▼").withStyle(ChatFormatting.RED);
        return Component.literal(" =").withStyle(ChatFormatting.GRAY);
    }

    @Override
    protected void init() {
        int y = top(7);
        label(Component.literal("БАНК — курс обновляется раз в игровые сутки"), y);
        label(Component.literal("1 Алмаз = " + fmt(v.get(0)) + " зол.").append(arrow(v.get(0), v.get(2))), y + 24);
        label(Component.literal("1 Незерит = " + fmt(v.get(1)) + " зол.").append(arrow(v.get(1), v.get(3))), y + 48);
        label(Component.literal("У вас: " + v.get(4) + " зол. / " + v.get(5) + " алм. / " + v.get(6) + " незер."), y + 72);

        int amount = AMOUNTS[amountIdx];
        button(Component.literal("Сумма обмена: " + amount + " (нажмите, чтобы сменить)"), this.width / 2 - 130, y + 96, 260, () -> {
            amountIdx = (amountIdx + 1) % AMOUNTS.length;
            this.rebuildWidgets();
        });

        int bx = this.width / 2 - 130;
        int by = y + 120;
        int i = 0;
        for (Currency from : Currency.values()) {
            for (Currency to : Currency.values()) {
                if (from == to) continue;
                int col = i % 2;
                int row = i / 2;
                final int action = from.ordinal() * 3 + to.ordinal();
                button(Component.literal(from.label + " → " + to.label), bx + col * 132, by + row * 22, 128,
                        () -> send(ScreenIds.BANK, action, AMOUNTS[amountIdx]));
                i++;
            }
        }
        button(Component.literal("Закрыть"), this.width / 2 - 50, by + 3 * 22 + 6, 100, this::onClose);
    }
}
