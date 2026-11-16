package com.example.crimetown.casino;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Slot machine maths. Symbols: 0 cherry, 1 lemon, 2 bell, 3 diamond, 4 seven.
 * Theoretical return to player is about 91 percent.
 */
public final class SlotLogic {
    public static final String[] SYMBOLS = {"Вишня", "Лимон", "Колокол", "Алмаз", "СЕМЁРКА"};
    private static final int[] WEIGHTS = {30, 25, 20, 15, 10};
    private static final int[] TRIPLE = {4, 6, 10, 20, 50};
    public static final int JACKPOT_MULT = 50;

    private SlotLogic() {}

    public static int[] spin() {
        return new int[]{pick(), pick(), pick()};
    }

    private static int pick() {
        int r = ThreadLocalRandom.current().nextInt(100);
        int acc = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            acc += WEIGHTS[i];
            if (r < acc) return i;
        }
        return 0;
    }

    /** Payout multiplier of the stake: 0 = lose, 1 = stake back, n = win n times the stake. */
    public static int multiplier(int[] r) {
        if (r[0] == r[1] && r[1] == r[2]) return TRIPLE[r[0]];
        if (r[0] == r[1] || r[1] == r[2] || r[0] == r[2]) return 1;
        return 0;
    }
}
