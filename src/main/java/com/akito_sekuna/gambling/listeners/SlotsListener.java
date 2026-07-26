package com.akito_sekuna.gambling.listeners;

import com.akito_sekuna.gambling.Main;
import com.akito_sekuna.gambling.slots.SlotsMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SlotsListener implements Listener {

    private final Main plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public SlotsListener(Main plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(SlotsMenu.TITLE)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        if (event.getRawSlot() == 22) {
            switch (event.getClick()) {
                case LEFT -> {
                    UUID uuid = player.getUniqueId();
                    long now = System.currentTimeMillis();
                    long cooldownMs = (long) plugin.getSlotsConfig().getCooldown() * 1000L;
                    if (cooldowns.containsKey(uuid) && now - cooldowns.get(uuid) < cooldownMs) {
                        long remaining = (cooldownMs - (now - cooldowns.get(uuid))) / 1000L + 1L;
                        player.sendActionBar("§cWait §f" + remaining + "s §cbefore spinning again!");
                        return;
                    }
                    cooldowns.put(uuid, now);
                    double bet = SlotsMenu.getBet(player, plugin);
                    SlotsMenu.spin(player, event.getView().getTopInventory(), bet, plugin);
                }
                case RIGHT -> SlotsMenu.adjustBet(player, event.getView().getTopInventory(), false, plugin);
                case SHIFT_LEFT -> SlotsMenu.adjustBet(player, event.getView().getTopInventory(), true, plugin);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(SlotsMenu.TITLE)) return;
        cooldowns.remove(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (SlotsMenu.isSpinning(uuid)) {
            plugin.getCoreAPI().getEconomy().give(uuid, SlotsMenu.getBet(player, plugin));
        }
        SlotsMenu.clearAll(uuid);
        cooldowns.remove(uuid);
    }
}
