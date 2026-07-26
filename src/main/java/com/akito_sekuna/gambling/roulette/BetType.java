package com.akito_sekuna.gambling.roulette;

public enum BetType {
    RED("§cRed", "x2"),
    BLACK("§8Black", "x2"),
    GREEN("§aGreen", "x14"),
    EVEN("§bEven", "x2"),
    ODD("§dOdd", "x2"),
    DOZEN_1("§e1st Dozen (1-12)", "x3"),
    DOZEN_2("§e2nd Dozen (13-24)", "x3"),
    DOZEN_3("§e3rd Dozen (25-36)", "x3"),
    NUMBER("§fSingle Number", "x35");

    public final String displayName;
    public final String payout;

    BetType(String displayName, String payout) {
        this.displayName = displayName;
        this.payout = payout;
    }
}
