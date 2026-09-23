package com.akito_sekuna.gambling.listeners;

import com.akito_sekuna.gambling.Main;
import com.akito_sekuna.gambling.roulette.RouletteBet;
import com.akito_sekuna.gambling.roulette.RouletteMenu;
import com.akito_sekuna.gambling.roulette.RouletteWheel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
        if (!event.getView().title().equals(RouletteMenu.TITLE)) return;

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
                player.sendActionBar(Component.text()
                        .append(Component.text("Wait ", NamedTextColor.RED))
                        .append(Component.text(remaining + "s", NamedTextColor.WHITE))
                        .append(Component.text(" before spinning again!", NamedTextColor.RED))
                        .build());
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
        if (!event.getView().title().equals(RouletteMenu.TITLE)) return;
        cooldowns.remove(player.getUniqueId());
    }

    /**
     * Refunds a player's active roulette wager if they disconnect mid spin.
     *
     * <p>{@link RouletteMenu#spin} takes the wager from the player's balance up front
     * (before the wheel animation runs) and marks the player as spinning. If the player
     * quits while {@link RouletteMenu#isSpinning} is still true for them, the spin never
     * reaches {@code handleResult}, so no outcome, payout, ludoman grant, or game-history
     * entry is ever produced for that spin. This refunds exactly the original wager
     * ({@code bet.amount()}), restoring the pre-spin balance; it is not a payout
     * calculation and ignores where the ball would have landed.
     *
     * <p>{@link RouletteMenu#clearAll} then drops the player's spinning flag, bet, bet
     * amount, and bet-type index. The still-running {@code runTaskTimer} animation task
     * (in {@code RouletteMenu#spin}) independently checks {@code player.isOnline()} each
     * tick and cancels itself once the player is gone; that check only stops the
     * animation, it does not refund again.
     *
     * <p>If the player quits with no active spin (no bet placed, or the spin already
     * resolved), no refund happens; this only clears any leftover per-player state.
     */
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
