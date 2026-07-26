package com.akito_sekuna.gambling.listeners;

import com.akito_sekuna.gambling.Main;
import com.akito_sekuna.gambling.roulette.RouletteBet;
import com.akito_sekuna.gambling.roulette.RouletteMenu;
import com.akito_sekuna.gambling.roulette.RouletteWheel;
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

public class RouletteListener implements Listener {

    private final Main plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public RouletteListener(Main plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(RouletteMenu.TITLE)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        int slot = event.getRawSlot();

        if (slot == 20) { RouletteMenu.cycleBetType(player, event.getView().getTopInventory(), plugin); return; }
        if (slot == 29) { RouletteMenu.adjustBet(player, event.getView().getTopInventory(), true, plugin); return; }
        if (slot == 33) { RouletteMenu.adjustBet(player, event.getView().getTopInventory(), false, plugin); return; }

        if (slot == 24) {
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            long cooldownMs = (long) plugin.getConfigManager().getRouletteCooldown() * 1000L;
            if (cooldowns.containsKey(uuid) && now - cooldowns.get(uuid) < cooldownMs) {
                long remaining = (cooldownMs - (now - cooldowns.get(uuid))) / 1000L + 1L;
                player.sendActionBar("§cWait §f" + remaining + "s §cbefore spinning again!");
                return;
            }
            cooldowns.put(uuid, now);
            RouletteMenu.spin(player, event.getView().getTopInventory(), plugin);
            return;
        }

        for (int i = 0; i < RouletteMenu.WHEEL_SLOTS.length; ++i) {
            if (slot == RouletteMenu.WHEEL_SLOTS[i]) {
                int number = RouletteWheel.WHEEL.get(i).number();
                RouletteMenu.setBetNumber(player, event.getView().getTopInventory(), number, plugin);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(RouletteMenu.TITLE)) return;
        cooldowns.remove(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (RouletteMenu.isSpinning(uuid)) {
            RouletteBet bet = RouletteMenu.getBet(player);
            if (bet != null) plugin.getCoreAPI().getEconomy().give(uuid, bet.amount());
        }
        RouletteMenu.clearAll(uuid);
        cooldowns.remove(uuid);
    }
}
