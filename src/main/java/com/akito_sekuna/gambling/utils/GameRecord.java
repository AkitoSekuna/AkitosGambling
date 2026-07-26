package com.akito_sekuna.gambling.utils;

public record GameRecord(String gameType, double betAmount, double payout, double ratio, boolean win, long timestamp) {

    public static GameRecord of(String gameType, double bet, double payout) {
        boolean win = payout > 0;
        double ratio = bet > 0 ? payout / bet : 0;
        return new GameRecord(gameType, bet, payout, ratio, win, System.currentTimeMillis());
    }
}
