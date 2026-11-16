package com.example.crimetown.progression;

public enum Business {
    BANK("bank", "Банк", 64, 4),
    CASINO("casino", "Казино", 48, 3),
    ARMORY("armory", "Оружейная", 32, 2);

    public final String id;
    public final String label;
    /** Base price in netherite ingots. */
    public final int basePrice;
    /** Daily income in netherite ingots. */
    public final int dailyIncome;

    Business(String id, String label, int basePrice, int dailyIncome) {
        this.id = id;
        this.label = label;
        this.basePrice = basePrice;
        this.dailyIncome = dailyIncome;
    }
}
