package com.akito_sekuna.gambling;

import com.akito_sekuna.core.AkitosAddon;
import com.akito_sekuna.core.ReloadReason;
import com.akito_sekuna.core.api.ICoreAPI;
import com.akito_sekuna.gambling.listeners.RouletteListener;
import com.akito_sekuna.gambling.listeners.SlotsListener;
import com.akito_sekuna.gambling.managers.ConfigManager;
import com.akito_sekuna.gambling.managers.GameHistoryManager;
import com.akito_sekuna.gambling.roulette.RouletteCommand;
import com.akito_sekuna.gambling.roulette.RouletteWheel;
import com.akito_sekuna.gambling.slots.SlotsCommand;
import com.akito_sekuna.gambling.slots.SlotsConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin implements AkitosAddon {

    private static Main instance;
    private ICoreAPI coreAPI;

    private ConfigManager configManager;
    private SlotsConfig slotsConfig;
    private GameHistoryManager historyManager;

    public static Main getInstance() { return instance; }

    public static File getPluginFolder() {
        return new File(instance.getServer().getPluginsFolder(), "AkitosPlugins/AkitosGambling");
    }

    public ConfigManager getConfigManager() { return configManager; }
    public SlotsConfig getSlotsConfig() { return slotsConfig; }
    public GameHistoryManager getHistoryManager() { return historyManager; }
    public ICoreAPI getCoreAPI() { return coreAPI; }

    // --- AkitosAddon ---

    @Override public String getAddonName() { return "AkitosGambling"; }
    @Override public String getAddonVersion() { return getPluginMeta().getVersion(); }

    @Override
    public void onCoreReady(ICoreAPI api) {
        this.coreAPI = api;
    }

    @Override
    public void onCoreReload(ICoreAPI newApi, ReloadReason reason) {
        this.coreAPI = newApi;
        configManager.reload();
        slotsConfig = new SlotsConfig(configManager);
    }

    @Override
    public void onCoreShutdown() {}

    // --- Lifecycle ---

    @Override
    public void onEnable() {
        instance = this;
        com.akito_sekuna.core.Main.registerAddon(this);

        configManager = new ConfigManager(this);
        historyManager = new GameHistoryManager(this);
        slotsConfig = new SlotsConfig(configManager);

        long sixHours = 432000L;
        Bukkit.getScheduler().runTaskTimer(this, RouletteWheel::shuffleDisplay, sixHours, sixHours);

        PluginCommand agCmd = getCommand("akitosgambling");
        if (agCmd != null) {
            agCmd.setExecutor(new MainCommand(this));
            agCmd.setTabCompleter(new MainTabCompleter());
        } else {
            getLogger().severe("Failed to register 'akitosgambling' command -- check plugin.yml!");
        }

        PluginCommand slotsCmd = getCommand("slots");
        if (slotsCmd != null) {
            slotsCmd.setExecutor(new SlotsCommand(this));
        } else {
            getLogger().severe("Failed to register 'slots' command -- check plugin.yml!");
        }

        PluginCommand rouletteCmd = getCommand("roulette");
        if (rouletteCmd != null) {
            rouletteCmd.setExecutor(new RouletteCommand(this));
        } else {
            getLogger().severe("Failed to register 'roulette' command -- check plugin.yml!");
        }

        getServer().getPluginManager().registerEvents(new RouletteListener(this), this);
        getServer().getPluginManager().registerEvents(new SlotsListener(this), this);

        getLogger().info("AkitosGambling v" + getPluginMeta().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("AkitosGambling disabled!");
    }
}
