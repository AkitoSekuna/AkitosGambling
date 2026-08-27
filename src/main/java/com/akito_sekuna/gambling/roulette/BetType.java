package com.akito_sekuna.gambling.roulette;

public enum BetType {
    RED("\u00a7cRed", "x2"),
    BLACK("\u00a78Black", "x2"),
    GREEN("\u00a7aGreen", "x10"),
    EVEN("\u00a7bEven", "x2"),
    ODD("\u00a7dOdd", "x2"),
    DOZEN_1("\u00a7e1st Dozen (1-12)", "x2"),
    DOZEN_2("\u00a7e2nd Dozen (13-24)", "x2"),
    NUMBER("\u00a7fSingle Number", "x20");

    public final String displayName;
    public final String payout;

    BetType(String displayName, String payout) {
        this.displayName = displayName;
        this.payout = payout;
    }
}
