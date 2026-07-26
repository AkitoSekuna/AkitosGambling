package com.akito_sekuna.gambling.managers;

import com.akito_sekuna.gambling.Main;
import com.akito_sekuna.gambling.utils.GameRecord;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GameHistoryManager {

    private final Main plugin;
    private final File historyFolder;
    private final Map<UUID, List<GameRecord>> cache = new HashMap<>();
    private final Map<UUID, String> flags = new HashMap<>();
    private final Map<UUID, Long> flagTimestamps = new HashMap<>();
    private final Random random = new Random();

    public GameHistoryManager(Main plugin) {
        this.plugin = plugin;
        this.historyFolder = new File(Main.getPluginFolder(), "history");
        if (!historyFolder.exists() && !historyFolder.mkdirs()) {
            plugin.getLogger().severe("Failed to create history directory: " + historyFolder.getPath());
        }
    }

    public void record(UUID uuid, GameRecord record) {
        synchronized (this) {
            List<GameRecord> history = getHistory(uuid);
            double threshold = plugin.getConfigManager().getRaw().getDouble("anti-cheat.notable-payout-multiplier", 3.0);
            if (record.ratio() >= threshold || !record.win()) {
                history.add(record);
                int maxSize = plugin.getConfigManager().getRaw().getInt("anti-cheat.history-size", 10);
                while (history.size() > maxSize) history.remove(0);
            }
            cache.put(uuid, history);
            checkFlags(uuid);
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> save(uuid));
    }

    public synchronized List<GameRecord> getHistory(UUID uuid) {
        return cache.containsKey(uuid) ? cache.get(uuid) : load(uuid);
    }

    public synchronized boolean isFlagged(UUID uuid) {
        if (!flags.containsKey(uuid)) return false;
        long flagTime = flagTimestamps.getOrDefault(uuid, 0L);
        long cooldownMs = plugin.getConfigManager().getRaw().getLong("anti-cheat.flag-cooldown-hours", 24L) * 3600000L;
        if (System.currentTimeMillis() - flagTime > cooldownMs) {
            flags.remove(uuid);
            flagTimestamps.remove(uuid);
            return false;
        }
        return true;
    }

    public synchronized String getFlagReason(UUID uuid) {
        return flags.getOrDefault(uuid, null);
    }

    public synchronized void unflag(UUID uuid) {
        flags.remove(uuid);
        flagTimestamps.remove(uuid);
    }

    public String getFlaggedMessage(UUID uuid) {
        String reason = getFlagReason(uuid);
        List<String> messages = reason != null && reason.equals("WINNING_STREAK")
                ? plugin.getConfigManager().getRaw().getStringList("anti-cheat.messages-flagged-winning")
                : plugin.getConfigManager().getRaw().getStringList("anti-cheat.messages-flagged-losing");
        if (messages.isEmpty()) return "§cThe casino is temporarily unavailable.";
        return messages.get(random.nextInt(messages.size()));
    }

    private synchronized void checkFlags(UUID uuid) {
        List<GameRecord> history = getHistory(uuid);
        int winStreak = plugin.getConfigManager().getRaw().getInt("anti-cheat.flag-win-streak", 5);
        int lossStreak = plugin.getConfigManager().getRaw().getInt("anti-cheat.flag-loss-streak", 10);
        double threshold = plugin.getConfigManager().getRaw().getDouble("anti-cheat.notable-payout-multiplier", 3.0);
        int consecutiveLosses = 0;
        int consecutiveWins = 0;
        for (int i = history.size() - 1; i >= 0; --i) {
            GameRecord r = history.get(i);
            if (!r.win()) { ++consecutiveLosses; consecutiveWins = 0; }
            else if (r.ratio() >= threshold) { ++consecutiveWins; consecutiveLosses = 0; }
            else break;
        }
        if (consecutiveWins >= winStreak) {
            flags.put(uuid, "WINNING_STREAK");
            flagTimestamps.put(uuid, System.currentTimeMillis());
        } else if (consecutiveLosses >= lossStreak) {
            flags.put(uuid, "LOSING_STREAK");
            flagTimestamps.put(uuid, System.currentTimeMillis());
        }
    }

    private synchronized List<GameRecord> load(UUID uuid) {
        File file = new File(historyFolder, uuid + ".yml");
        List<GameRecord> history = new ArrayList<>();
        if (!file.exists()) { cache.put(uuid, history); return history; }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map<?, ?> map : config.getMapList("history")) {
            history.add(new GameRecord(
                    (String) map.get("game"),
                    ((Number) map.get("bet")).doubleValue(),
                    ((Number) map.get("payout")).doubleValue(),
                    ((Number) map.get("ratio")).doubleValue(),
                    (Boolean) map.get("win"),
                    ((Number) map.get("timestamp")).longValue()
            ));
        }
        cache.put(uuid, history);
        return history;
    }

    private void save(UUID uuid) {
        File file = new File(historyFolder, uuid + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> records = new ArrayList<>();
        synchronized (this) {
            for (GameRecord r : getHistory(uuid)) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("game", r.gameType());
                map.put("bet", r.betAmount());
                map.put("payout", r.payout());
                map.put("ratio", r.ratio());
                map.put("win", r.win());
                map.put("timestamp", r.timestamp());
                records.add(map);
            }
        }
        config.set("history", records);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save history for " + uuid + ": " + e.getMessage());
        }
    }
}
