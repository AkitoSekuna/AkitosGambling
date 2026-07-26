package com.akito_sekuna.gambling.slots;

import com.akito_sekuna.gambling.Main;
import com.akito_sekuna.gambling.utils.GameRecord;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SlotsMenu {

    public static final String TITLE = "§6§lSlot Machine";

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
        for (int i = 0; i < 27; ++i) menu.setItem(i, makePane(Material.GRAY_STAINED_GLASS_PANE, "§8"));
        menu.setItem(10, makeReel("?"));
        menu.setItem(13, makeReel("?"));
        menu.setItem(16, makeReel("?"));
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
            player.sendActionBar("§cAlready spinning!");
            return;
        }
        double balance = plugin.getCoreAPI().getEconomy().getBalance(player.getUniqueId());
        if (balance < bet) {
            player.sendActionBar("§cNot enough balance!");
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
            menu.setItem(10, makeReel(plugin.getSlotsConfig().getRandom().emoji()));
            menu.setItem(13, makeReel(plugin.getSlotsConfig().getRandom().emoji()));
            menu.setItem(16, makeReel(plugin.getSlotsConfig().getRandom().emoji()));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.3f, 1f + (tick[0] * 0.02f));
            tick[0]++;
        }, 0L, 2L);
    }

    private static void showResult(Player player, Inventory menu, SlotsGame.SpinResult result, double bet, Main plugin) {
        menu.setItem(10, makeReel(result.reels()[0].emoji()));
        menu.setItem(13, makeReel(result.reels()[1].emoji()));
        menu.setItem(16, makeReel(result.reels()[2].emoji()));

        if (result.win()) {
            plugin.getCoreAPI().getEconomy().give(player.getUniqueId(), result.payout());
            if (result.payout() >= bet * 10) {
                player.sendTitle("§b§lJACKPOT!", "§e+" + String.format("%.1f", result.payout()), 10, 60, 20);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            } else {
                player.sendTitle("§a§lWIN!", "§e+" + String.format("%.1f", result.payout()), 10, 60, 20);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
        } else {
            player.sendTitle("§c§lNO MATCH", "§7Better luck next time!", 10, 60, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
        }

        menu.setItem(0, makeBalanceItem(player, plugin));
        plugin.getHistoryManager().record(player.getUniqueId(), GameRecord.of("slots", bet, result.payout()));
    }

    public static void adjustBet(Player player, Inventory menu, boolean increase, Main plugin) {
        double min = plugin.getSlotsConfig().getMinBet();
        double max = plugin.getSlotsConfig().getMaxBet();
        double current = betAmounts.getOrDefault(player.getUniqueId(), min);

        if (increase) {
            double newAmount = Math.min(current + min, max);
            double balance = plugin.getCoreAPI().getEconomy().getBalance(player.getUniqueId());
            if (balance < newAmount) {
                player.sendActionBar("§cNot enough balance to raise bet!");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            current = newAmount;
        } else {
            current = Math.max(current - min, min);
        }

        betAmounts.put(player.getUniqueId(), current);
        menu.setItem(8, makeBetItem(player, plugin));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, increase ? 1.2f : 0.8f);
    }

    // --- Item builders ---

    private static ItemStack makeReel(String symbol) {
        ItemStack item = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f§l" + symbol);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeSpinButton(Player player, Main plugin) {
        double bet = betAmounts.getOrDefault(player.getUniqueId(), plugin.getSlotsConfig().getMinBet());
        ItemStack item = new ItemStack(Material.LEVER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§lSPIN!");
        meta.setLore(List.of(
                "§7Bet: §e" + String.format("%.1f", bet),
                "",
                "§7Left Click §8- §7Spin",
                "§7Right Click §8- §7Decrease bet",
                "§7Shift + Left §8- §7Increase bet"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeBalanceItem(Player player, Main plugin) {
        double balance = plugin.getCoreAPI().getEconomy().getBalance(player.getUniqueId());
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lBalance");
        meta.setLore(List.of("§e" + String.format("%.1f", balance)));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeBetItem(Player player, Main plugin) {
        double bet = betAmounts.getOrDefault(player.getUniqueId(), plugin.getSlotsConfig().getMinBet());
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§lCurrent Bet");
        meta.setLore(List.of("§e" + String.format("%.1f", bet)));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makePane(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
