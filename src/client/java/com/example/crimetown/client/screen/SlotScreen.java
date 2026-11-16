package com.example.crimetown.client.screen;

import com.example.crimetown.casino.SlotLogic;
import com.example.crimetown.economy.Currency;
import com.example.crimetown.network.ScreenIds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

/** values: [r1, r2, r3, delta, currencyIdx, bet, gold, diamonds, netherite] */
public class SlotScreen extends CtScreen {
    private static final int[] BETS = {1, 2, 5, 10, 25, 64};
    private int curIdx;
    private int betIdx;

    public SlotScreen(List<Integer> values) {
        super("Слот-машина", values);
        this.curIdx = Math.max(0, Math.min(2, values.get(4)));
        for (int i = 0; i < BETS.length; i++) if (BETS[i] == values.get(5)) betIdx = i;
    }

    @Override
    protected void init() {
        int y = top(6);
        label(Component.literal("СЛОТ-МАШИНА «ЗОЛОТАЯ ЖИЛА»"), y);

        String reels = "[ " + SlotLogic.SYMBOLS[v.get(0)] + " ] [ " + SlotLogic.SYMBOLS[v.get(1)] + " ] [ " + SlotLogic.SYMBOLS[v.get(2)] + " ]";
        label(Component.literal(reels), y + 24);

        int delta = v.get(3);
        Component result;
        if (delta > 0) result = Component.literal("Выигрыш: +" + delta).withStyle(ChatFormatting.GREEN);
        else if (delta < 0) result = Component.literal("Проигрыш: " + delta).withStyle(ChatFormatting.RED);
        else result = Component.literal("Ставка возвращена / ждём спин").withStyle(ChatFormatting.GRAY);
        label(result, y + 48);

        label(Component.literal("У вас: " + v.get(6) + " зол. / " + v.get(7) + " алм. / " + v.get(8) + " незер."), y + 72);

        button(Component.literal("Валюта: " + Currency.byIndex(curIdx).label), this.width / 2 - 130, y + 96, 128, () -> {
            curIdx = (curIdx + 1) % 3;
            this.rebuildWidgets();
        });
        button(Component.literal("Ставка: " + BETS[betIdx]), this.width / 2 + 2, y + 96, 128, () -> {
            betIdx = (betIdx + 1) % BETS.length;
            this.rebuildWidgets();
        });
        button(Component.literal("КРУТИТЬ!"), this.width / 2 - 130, y + 122, 260,
                () -> send(ScreenIds.SLOT, curIdx, BETS[betIdx]));
        button(Component.literal("Уйти"), this.width / 2 - 50, y + 148, 100, this::onClose);
    }
}
