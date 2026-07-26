package com.akito_sekuna.gambling.slots;

import com.akito_sekuna.gambling.managers.ConfigManager;

import java.util.ArrayList;
import java.util.List;

public class SlotsConfig {

    private final List<SlotSymbol> symbols = new ArrayList<>();
    private final int cooldown;
    private final double minBet;
    private final double maxBet;

    public SlotsConfig(ConfigManager config) {
        this.cooldown = config.getSlotsCooldown();
        this.minBet = config.getSlotsMinBet();
        this.maxBet = config.getSlotsMaxBet();

        // Default symbols if not configured
        symbols.add(new SlotSymbol("Cherry", "🍒", 30, 1.5));
        symbols.add(new SlotSymbol("Lemon", "🍋", 25, 2.0));
        symbols.add(new SlotSymbol("Orange", "🍊", 20, 2.5));
        symbols.add(new SlotSymbol("Plum", "🍇", 12, 4.0));
        symbols.add(new SlotSymbol("Bell", "🔔", 8, 8.0));
        symbols.add(new SlotSymbol("Bar", "BAR", 4, 15.0));
        symbols.add(new SlotSymbol("Seven", "7️⃣", 1, 50.0));
    }

    public List<SlotSymbol> getSymbols() { return symbols; }
    public int getCooldown() { return cooldown; }
    public double getMinBet() { return minBet; }
    public double getMaxBet() { return maxBet; }

    public SlotSymbol getRandom() {
        double totalWeight = symbols.stream().mapToDouble(SlotSymbol::weight).sum();
        double roll = Math.random() * totalWeight;
        double cumulative = 0;
        for (SlotSymbol symbol : symbols) {
            cumulative += symbol.weight();
            if (roll <= cumulative) return symbol;
        }
        return symbols.get(symbols.size() - 1);
    }
}
