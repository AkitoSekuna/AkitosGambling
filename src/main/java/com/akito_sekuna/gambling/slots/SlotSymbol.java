package com.akito_sekuna.gambling.slots;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

public record SlotSymbol(String name, Material material, double weight, double multiplier, NamedTextColor color) {}
