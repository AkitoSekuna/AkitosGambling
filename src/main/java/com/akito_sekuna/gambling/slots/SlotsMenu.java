package com.akito_sekuna.gambling.slots;

import com.akito_sekuna.gambling.Main;
import com.akito_sekuna.gambling.utils.GameRecord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SlotsMenu {

    public static final Component TITLE = Component.text("Slot Machine", NamedTextColor.GOLD).decorate(TextDecoration.BOLD);

    private static final double BET_STEP = 2.0;
    private static final Map<UUID, Boolean> spinning = new HashMap<>();
    private static final Map<UUID, Double> betAmounts = new HashMap<>();

    public static void open(Player player, Main plugin) {
        Inventory menu = Bukkit.createInventory(null, 27, TITLE);
        build(menu, player, plugin);
        player.openInventory(menu);
    }

    public static boolean isSpinning(UUID uuid) {
        return spinning.getOrDefault(uuid, false);
    }

    public static void clearAll(UUID uuid) {
        spinning.remove(uuid);
        betAmounts.remove(uuid);
    }

    public static double getBet(Player player, Main plugin) {
        return betAmounts.getOrDefault(player.getUniqueId(), plugin.getSlotsConfig().getMinBet());
    }

    public static void build(Inventory menu, Player player, Main plugin) {
        for (int i = 0; i < 27; ++i) menu.setItem(i, makePane(Material.GRAY_STAINED_GLASS_PANE, Component.text("")));
        menu.setItem(10, makeUnrevealedReel());
        menu.setItem(13, makeUnrevealedReel());
        menu.setItem(16, makeUnrevealedReel());
        menu.setItem(22, makeSpinButton(player, plugin));
        menu.setItem(0, makeBalanceItem(player, plugin));
        menu.setItem(8, makeBetItem(player, plugin));
    }

    public static void spin(Player player, Inventory menu, double bet, Main plugin) {
        if (plugin.getHistoryManager().isFlagged(player.getUniqueId())) {
            player.sendMessage(plugin.getHistoryManager().getFlaggedMessage(player.getUniqueId()));
            return;
        }
        if (spinning.getOrDefault(player.getUniqueId(), false)) {
            player.sendActionBar(Component.text("Already spinning!", NamedTextColor.RED));
            return;
        }
        double balance = plugin.getCoreAPI().getEconomy().getBalance(player.getUniqueId());
        if (balance < bet) {
            player.sendActionBar(Component.text("Not enough balance!", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        plugin.getCoreAPI().getEconomy().take(player.getUniqueId(), bet);
        spinning.put(player.getUniqueId(), true);
        menu.setItem(0, makeBalanceItem(player, plugin));

        int[] tick = {0};
        int totalTicks = 20 + (int) (Math.random() * 10);

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!player.isOnline() || player.getOpenInventory().getTopInventory() != menu) {
                task.cancel();
                spinning.put(player.getUniqueId(), false);
                return;
            }
            if (tick[0] >= totalTicks) {
                task.cancel();
                spinning.put(player.getUniqueId(), false);
                SlotsGame.SpinResult result = SlotsGame.spin(bet, plugin.getSlotsConfig());
                showResult(player, menu, result, bet, plugin);
                return;
            }
            menu.setItem(10, makeReel(plugin.getSlotsConfig().getRandom()));
            menu.setItem(13, makeReel(plugin.getSlotsConfig().getRandom()));
            menu.setItem(16, makeReel(plugin.getSlotsConfig().getRandom()));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.3f, 1f + (tick[0] * 0.02f));
            tick[0]++;
        }, 0L, 2L);
    }

    private static void showResult(Player player, Inventory menu, SlotsGame.SpinResult result, double bet, Main plugin) {
        menu.setItem(10, makeReel(result.reels()[0]));
        menu.setItem(13, makeReel(result.reels()[1]));
        menu.setItem(16, makeReel(result.reels()[2]));

        if (result.win()) {
            plugin.getCoreAPI().getEconomy().give(player.getUniqueId(), result.payout());
            Component subtitle = Component.text("+" + String.format("%.0f", result.payout()), NamedTextColor.YELLOW);
            if (result.payout() >= bet * 10) {
                showTitle(player,
                        Component.text("JACKPOT!", NamedTextColor.AQUA).decorate(TextDecoration.BOLD),
                        subtitle);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            } else {
                showTitle(player,
                        Component.text("WIN!", NamedTextColor.GREEN).decorate(TextDecoration.BOLD),
                        subtitle);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
        } else {
            showTitle(player,
                    Component.text("NO MATCH", NamedTextColor.RED).decorate(TextDecoration.BOLD),
                    Component.text("Better luck next time!", NamedTextColor.GRAY));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
        }

        menu.setItem(0, makeBalanceItem(player, plugin));
        plugin.getGamesPlayedTracker().record("slots");
        plugin.getHistoryManager().record(player.getUniqueId(), GameRecord.of("slots", bet, result.payout()));
    }

    private static void showTitle(Player player, Component title, Component subtitle) {
        player.showTitle(Title.title(title, subtitle,
                Title.Times.times(Duration.ofMillis(500L), Duration.ofMillis(3000L), Duration.ofMillis(1000L))));
    }

    public static void adjustBet(Player player, Inventory menu, boolean increase, Main plugin) {
        double min = plugin.getSlotsConfig().getMinBet();
        double max = plugin.getSlotsConfig().getMaxBet();
        double current = betAmounts.getOrDefault(player.getUniqueId(), min);

        if (increase) {
            double newAmount = Math.min(current + BET_STEP, max);
            double balance = plugin.getCoreAPI().getEconomy().getBalance(player.getUniqueId());
            if (balance < newAmount) {
                player.sendActionBar(Component.text("Not enough balance to raise bet!", NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            current = newAmount;
        } else {
            current = Math.max(current - BET_STEP, min);
        }

        betAmounts.put(player.getUniqueId(), current);
        menu.setItem(8, makeBetItem(player, plugin));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, increase ? 1.2f : 0.8f);
    }

    // --- Item builders ---

    private static ItemStack makeReel(SlotSymbol symbol) {
        ItemStack item = new ItemStack(symbol.material());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(symbol.name(), symbol.color()).decorate(TextDecoration.BOLD));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeUnrevealedReel() {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("???", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeSpinButton(Player player, Main plugin) {
        double bet = betAmounts.getOrDefault(player.getUniqueId(), plugin.getSlotsConfig().getMinBet());
        ItemStack item = new ItemStack(Material.LEVER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("SPIN!", NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
        meta.lore(List.of(
                Component.text()
                        .append(Component.text("Bet: ", NamedTextColor.GRAY))
                        .append(Component.text(String.format("%.0f", bet), NamedTextColor.YELLOW))
                        .build(),
                Component.empty(),
                Component.text()
                        .append(Component.text("Left Click ", NamedTextColor.GRAY))
                        .append(Component.text("- ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("Spin", NamedTextColor.GRAY))
                        .build(),
                Component.text()
                        .append(Component.text("Right Click ", NamedTextColor.GRAY))
                        .append(Component.text("- ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("Decrease bet", NamedTextColor.GRAY))
                        .build(),
                Component.text()
                        .append(Component.text("Shift + Left ", NamedTextColor.GRAY))
                        .append(Component.text("- ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("Increase bet", NamedTextColor.GRAY))
                        .build()
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeBalanceItem(Player player, Main plugin) {
        double balance = plugin.getCoreAPI().getEconomy().getBalance(player.getUniqueId());
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Balance", NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        meta.lore(List.of(Component.text(String.format("%.0f", balance), NamedTextColor.YELLOW)));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeBetItem(Player player, Main plugin) {
        double bet = betAmounts.getOrDefault(player.getUniqueId(), plugin.getSlotsConfig().getMinBet());
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Current Bet", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));
        meta.lore(List.of(Component.text(String.format("%.0f", bet), NamedTextColor.YELLOW)));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makePane(Material mat, Component name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
