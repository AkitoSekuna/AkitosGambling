package com.akito_sekuna.gambling.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigManager {

    private FileConfiguration config;
    private final File file;
    private final com.akito_sekuna.gambling.Main plugin;

    public ConfigManager(com.akito_sekuna.gambling.Main plugin) {
        this.plugin = plugin;
        this.file = new File(com.akito_sekuna.gambling.Main.getPluginFolder(), "config.yml");
        if (!file.exists()) {
            if (!com.akito_sekuna.gambling.Main.getPluginFolder().mkdirs()) {
                plugin.getLogger().warning("Plugin folder already exists or could not be created.");
            }
            try (InputStream in = plugin.getResource("config.yml")) {
                if (in != null) Files.copy(in, file.toPath());
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to copy default config.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public double getSlotsMinBet() { return config.getDouble("slots.min-bet", 5.0); }
    public double getSlotsMaxBet() { return config.getDouble("slots.max-bet", 500.0); }
    public int getSlotsCooldown() { return config.getInt("slots.cooldown-seconds", 3); }
    public double getRouletteMinBet() { return config.getDouble("roulette.min-bet", 5.0); }
    public double getRouletteMaxBet() { return config.getDouble("roulette.max-bet", 1000.0); }
    public int getRouletteCooldown() { return config.getInt("roulette.cooldown-seconds", 30); }
    public int getRouletteSpinDuration() { return config.getInt("roulette.spin-duration-seconds", 5); }

    public FileConfiguration getRaw() { return config; }
}
