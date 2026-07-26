package com.akito_sekuna.gambling.roulette;

public record RouletteBet(BetType type, double amount, int number) {

    public static RouletteBet of(BetType type, double amount) {
        return new RouletteBet(type, amount, -1);
    }

    public static RouletteBet ofNumber(double amount, int number) {
        return new RouletteBet(BetType.NUMBER, amount, number);
    }
}
