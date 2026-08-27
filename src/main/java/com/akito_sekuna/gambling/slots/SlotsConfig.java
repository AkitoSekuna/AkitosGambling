package com.akito_sekuna.gambling.slots;

import com.akito_sekuna.gambling.managers.ConfigManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

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
        symbols.add(new SlotSymbol("\ud83c\udf52 Cherry \ud83c\udf52", Material.SWEET_BERRIES, 30, 1.0, NamedTextColor.RED));
        symbols.add(new SlotSymbol("\ud83c\udf4b Lemon \ud83c\udf4b", Material.GOLDEN_APPLE, 25, 2.0, NamedTextColor.YELLOW));
        symbols.add(new SlotSymbol("\ud83c\udf4a Orange \ud83c\udf4a", Material.GLOW_BERRIES, 20, 3.0, NamedTextColor.GOLD));
        symbols.add(new SlotSymbol("\ud83c\udf47 Plum \ud83c\udf47", Material.CHORUS_FRUIT, 12, 4.0, NamedTextColor.DARK_PURPLE));
        symbols.add(new SlotSymbol("\ud83d\udd14 Bell \ud83d\udd14", Material.BELL, 8, 8.0, NamedTextColor.YELLOW));
        symbols.add(new SlotSymbol("\u2796 Bar \u2796", Material.IRON_INGOT, 6, 10.0, NamedTextColor.GRAY));
        symbols.add(new SlotSymbol("7\ufe0f\u20e3 Seven 7\ufe0f\u20e3", Material.DIAMOND, 4, 15.0, NamedTextColor.AQUA));
        symbols.add(new SlotSymbol("\u2b50 Jackpot \u2b50", Material.NETHER_STAR, 1, 50.0, NamedTextColor.YELLOW));
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
