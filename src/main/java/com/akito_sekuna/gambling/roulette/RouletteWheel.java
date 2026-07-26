package com.akito_sekuna.gambling.roulette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RouletteWheel {

    private static final Random RANDOM = new Random();

    // Standard European roulette wheel order
    public static final List<RouletteNumber> WHEEL = new ArrayList<>(List.of(
            new RouletteNumber(0, "green"),
            new RouletteNumber(32, "red"), new RouletteNumber(15, "black"),
            new RouletteNumber(19, "red"), new RouletteNumber(4, "black"),
            new RouletteNumber(21, "red"), new RouletteNumber(2, "black"),
            new RouletteNumber(25, "red"), new RouletteNumber(17, "black"),
            new RouletteNumber(34, "red"), new RouletteNumber(6, "black"),
            new RouletteNumber(27, "red"), new RouletteNumber(13, "black"),
            new RouletteNumber(36, "red"), new RouletteNumber(11, "black"),
            new RouletteNumber(30, "red"), new RouletteNumber(8, "black"),
            new RouletteNumber(23, "red"), new RouletteNumber(10, "black"),
            new RouletteNumber(5, "red"), new RouletteNumber(24, "black"),
            new RouletteNumber(16, "red"), new RouletteNumber(33, "black"),
            new RouletteNumber(1, "red"), new RouletteNumber(20, "black"),
            new RouletteNumber(14, "red"), new RouletteNumber(31, "black"),
            new RouletteNumber(9, "red"), new RouletteNumber(22, "black"),
            new RouletteNumber(18, "red"), new RouletteNumber(29, "black"),
            new RouletteNumber(7, "red"), new RouletteNumber(28, "black"),
            new RouletteNumber(12, "red"), new RouletteNumber(35, "black"),
            new RouletteNumber(3, "red"), new RouletteNumber(26, "black")
    ));

    public static void shuffleDisplay() {
        Collections.shuffle(WHEEL);
    }

    public static SpinPlan plan() {
        int totalSpins = 60 + RANDOM.nextInt(40);
        RouletteNumber result = WHEEL.get(RANDOM.nextInt(WHEEL.size()));
        return new SpinPlan(totalSpins, result);
    }

    public record SpinPlan(int totalSpins, RouletteNumber result) {}
}
