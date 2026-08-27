package com.akito_sekuna.gambling.roulette;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RouletteWheel {

    private static final Random RANDOM = new Random();

    // Numbers displayed in strict ascending order: 0, 00, then 1-24.
    // Colors for 1-24 match real-world roulette color assignments (12 red / 12 black).
    public static final List<RouletteNumber> WHEEL = new ArrayList<>(List.of(
            new RouletteNumber(0, "green"),
            new RouletteNumber(1, "black"), new RouletteNumber(2, "red"),
            new RouletteNumber(3, "black"), new RouletteNumber(4, "red"),
            new RouletteNumber(5, "black"), new RouletteNumber(6, "red"),
            new RouletteNumber(7, "black"), new RouletteNumber(8, "red"),
            new RouletteNumber(9, "black"), new RouletteNumber(10, "red"),
            new RouletteNumber(11, "black"), new RouletteNumber(12, "red"),
            new RouletteNumber(-1, "green"),
            new RouletteNumber(13, "black"), new RouletteNumber(14, "red"),
            new RouletteNumber(15, "black"), new RouletteNumber(16, "red"),
            new RouletteNumber(17, "black"), new RouletteNumber(18, "red"),
            new RouletteNumber(19, "black"), new RouletteNumber(20, "red"),
            new RouletteNumber(21, "black"), new RouletteNumber(22, "red"),
            new RouletteNumber(23, "black"), new RouletteNumber(24, "red")
    ));

    // How many full loops around the board the ball visibly spins through
    // before settling, purely for animation feel -- does not affect the result.
    private static final int MIN_EXTRA_LOOPS = 2;
    private static final int MAX_EXTRA_LOOPS = 4;

    public static SpinPlan plan() {
        RouletteNumber result = WHEEL.get(RANDOM.nextInt(WHEEL.size()));
        int targetIndex = WHEEL.indexOf(result);

        int extraLoops = MIN_EXTRA_LOOPS + RANDOM.nextInt(MAX_EXTRA_LOOPS - MIN_EXTRA_LOOPS + 1);
        // Chosen so that (totalSpins - 1) % WHEEL.size() == targetIndex,
        // meaning the ball's final animated position always matches the
        // actual result -- no more mismatch between the board and the title.
        int totalSpins = extraLoops * WHEEL.size() + targetIndex + 1;

        return new SpinPlan(totalSpins, result);
    }

    public record SpinPlan(int totalSpins, RouletteNumber result) {}
}
