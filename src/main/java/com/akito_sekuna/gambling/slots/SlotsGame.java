package com.akito_sekuna.gambling.slots;

public class SlotsGame {

    public record SpinResult(SlotSymbol[] reels, boolean win, double payout) {}

    public static SpinResult spin(double bet, SlotsConfig config) {
        SlotSymbol[] reels = new SlotSymbol[]{
                config.getRandom(),
                config.getRandom(),
                config.getRandom()
        };

        boolean allMatch = reels[0].name().equals(reels[1].name()) && reels[1].name().equals(reels[2].name());
        boolean twoMatch = reels[0].name().equals(reels[1].name())
                || reels[1].name().equals(reels[2].name())
                || reels[0].name().equals(reels[2].name());

        double payout = 0;
        if (allMatch) {
            payout = bet * reels[0].multiplier();
        } else if (twoMatch) {
            // partial match pays back half the bet
            payout = bet * 0.5;
        }

        return new SpinResult(reels, payout > 0, payout);
    }
}
