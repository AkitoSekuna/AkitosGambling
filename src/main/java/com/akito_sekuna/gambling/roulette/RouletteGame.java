package com.akito_sekuna.gambling.roulette;

public class RouletteGame {

    public static double evaluate(RouletteBet bet, RouletteNumber result) {
        return switch (bet.type()) {
            case RED -> result.color().equals("red") ? bet.amount() * 2 : 0;
            case BLACK -> result.color().equals("black") ? bet.amount() * 2 : 0;
            case GREEN -> result.color().equals("green") ? bet.amount() * 14 : 0;
            case EVEN -> result.number() != 0 && result.number() % 2 == 0 ? bet.amount() * 2 : 0;
            case ODD -> result.number() % 2 != 0 ? bet.amount() * 2 : 0;
            case DOZEN_1 -> result.number() >= 1 && result.number() <= 12 ? bet.amount() * 3 : 0;
            case DOZEN_2 -> result.number() >= 13 && result.number() <= 24 ? bet.amount() * 3 : 0;
            case DOZEN_3 -> result.number() >= 25 && result.number() <= 36 ? bet.amount() * 3 : 0;
            case NUMBER -> result.number() == bet.number() ? bet.amount() * 35 : 0;
        };
    }
}
