package com.akito_sekuna.gambling.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counts completed games by type (slots/roulette) between bStats reports.
 * getAndReset() snapshots and clears the counts, matching AkitosCore's
 * CommandIssueTracker pattern.
 */
public class GamesPlayedTracker {

    private final Map<String, AtomicInteger> counts = new ConcurrentHashMap<>();

    public void record(String gameType) {
        counts.computeIfAbsent(gameType, k -> new AtomicInteger()).incrementAndGet();
    }

    public Map<String, Integer> getAndReset() {
        Map<String, Integer> snapshot = new HashMap<>();
        counts.forEach((type, counter) -> {
            int value = counter.getAndSet(0);
            if (value > 0) snapshot.put(type, value);
        });
        return snapshot;
    }
}
