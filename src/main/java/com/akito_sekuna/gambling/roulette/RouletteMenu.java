package com.akito_sekuna.gambling.roulette;

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

public class RouletteMenu {

    public static final String TITLE = "§6§lRoulette";
    public static final int[] WHEEL_SLOTS = {
            0,1,2,3,4,5,6,7,8,
            17,26,35,44,53,
            52,51,50,49,48,47,46,45,
            36,27,18,9,
            10,11
    };

    private static final Map<UUID, Boolean> spinning = new HashMap<>();
    private static final Map<UUID, RouletteBet> bets = new HashMap<>();
    private static final Map<UUID, Double> betAmounts = new HashMap<>();
    private static final Map<UUID, Integer> betTypeIndex = new HashMap<>();

    private static final BetType[] BET_CYCLE = {
            BetType.RED, BetType.BLACK, BetType.GREEN,
            BetType.EVEN, BetType.ODD,
            BetType.DOZEN_1, BetType.DOZEN_2, BetType.DOZEN_3
    };

    public static void open(Player player, Main plugin) {
        Inventory menu = Bukkit.createInventory(null, 54, TITLE);
        build(menu, player, plugin);
        player.openInventory(menu);
    }

    public static boolean isSpinning(UUID uuid) {
        return spinning.getOrDefault(uuid, false);
    }

    public static void clearAll(UUID uuid) {
        spinning.remove(uuid);
        bets.remove(uuid);
        betAmounts.remove(uuid);
        betTypeIndex.remove(uuid);
    }

    public static RouletteBet getBet(Player player) {
        return bets.get(player.getUniqueId());
    }

    public static void build(Inventory menu, Player player, Main plugin) {
        for (int i = 0; i < 54; ++i) menu.setItem(i, new ItemStack(Material.AIR));
        List<RouletteNumber> wheel = RouletteWheel.WHEEL;
        for (int i = 0; i < Math.min(WHEEL_SLOTS.length, wheel.size()); ++i) {
            menu.setItem(WHEEL_SLOTS[i], makeNumberItem(wheel.get(i)));
        }
        menu.setItem(20, makeBetTypeButton(player));
        menu.setItem(22, makeBetInfo(player, plugin));
        menu.setItem(24, makeSpinButton());
        menu.setItem(29, makePane(Material.LIME_DYE, "§a§l+ Bet"));
        menu.setItem(33, makePane(Material.RED_DYE, "§c§l- Bet"));
        menu.setItem(31, makeBalanceItem(player, plugin));
    }

    public static void cycleBetType(Player player, Inventory menu, Main plugin) {
        int index = (betTypeIndex.getOrDefault(player.getUniqueId(), 0) + 1) % BET_CYCLE.length;
        betTypeIndex.put(player.getUniqueId(), index);
        double amount = betAmounts.getOrDefault(player.getUniqueId(), plugin.getConfigManager().getRouletteMinBet());
        bets.put(player.getUniqueId(), RouletteBet.of(BET_CYCLE[index], amount));
        menu.setItem(20, makeBetTypeButton(player));
        menu.setItem(22, makeBetInfo(player, plugin));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
        player.sendActionBar("§7Bet type: " + BET_CYCLE[index].displayName);
    }

    public static void setBetNumber(Player player, Inventory menu, int number, Main plugin) {
        double amount = betAmounts.getOrDefault(player.getUniqueId(), plugin.getConfigManager().getRouletteMinBet());
        bets.put(player.getUniqueId(), RouletteBet.ofNumber(amount, number));
        menu.setItem(22, makeBetInfo(player, plugin));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
        player.sendActionBar("§7Betting on number: §e" + number);
    }

    public static void adjustBet(Player player, Inventory menu, boolean increase, Main plugin) {
        double minBet = plugin.getConfigManager().getRouletteMinBet();
        double maxBet = plugin.getConfigManager().getRouletteMaxBet();
        double amount = betAmounts.getOrDefault(player.getUniqueId(), minBet);

        if (increase) {
            double newAmount = Math.min(amount + minBet, maxBet);
            double balance = plugin.getCoreAPI().getEconomy().getBalance(player.getUniqueId());
            if (balance < newAmount) {
                player.sendActionBar("§cNot enough balance to raise bet!");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            amount = newAmount;
        } else {
            amount = Math.max(amount - minBet, minBet);
        }

        betAmounts.put(player.getUniqueId(), amount);
        RouletteBet existing = bets.get(player.getUniqueId());
        if (existing != null) {
            bets.put(player.getUniqueId(), new RouletteBet(existing.type(), amount, existing.number()));
        }
        menu.setItem(22, makeBetInfo(player, plugin));
        menu.setItem(31, makeBalanceItem(player, plugin));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, increase ? 1.2f : 0.8f);
    }

    public static void spin(Player player, Inventory menu, Main plugin) {
        if (plugin.getHistoryManager().isFlagged(player.getUniqueId())) {
            player.sendMessage(plugin.getHistoryManager().getFlaggedMessage(player.getUniqueId()));
            return;
        }
        if (spinning.getOrDefault(player.getUniqueId(), false)) {
            player.sendActionBar("§cWait for the current spin to finish!");
            return;
        }
        RouletteBet bet = bets.get(player.getUniqueId());
        if (bet == null) {
            player.sendActionBar("§cChoose a bet type first!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }
        double balance = plugin.getCoreAPI().getEconomy().getBalance(player.getUniqueId());
        if (balance < bet.amount()) {
            player.sendActionBar("§cNot enough balance!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        plugin.getCoreAPI().getEconomy().take(player.getUniqueId(), bet.amount());
        spinning.put(player.getUniqueId(), true);
        menu.setItem(31, makeBalanceItem(player, plugin));

        RouletteWheel.SpinPlan plan = RouletteWheel.plan();
        List<RouletteNumber> wheel = RouletteWheel.WHEEL;
        int[] currentSpin = {0};
        int[] prevSlotIndex = {-1};

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!player.isOnline() || player.getOpenInventory().getTopInventory() != menu) {
                task.cancel();
                spinning.put(player.getUniqueId(), false);
                return;
            }
            if (currentSpin[0] >= plan.totalSpins()) {
                task.cancel();
                spinning.put(player.getUniqueId(), false);
                handleResult(player, menu, bet, plan.result(), plugin);
                return;
            }
            if (prevSlotIndex[0] >= 0) {
                menu.setItem(WHEEL_SLOTS[prevSlotIndex[0]], makeNumberItem(wheel.get(prevSlotIndex[0])));
            }
            int index = currentSpin[0] % wheel.size();
            menu.setItem(WHEEL_SLOTS[index], makeBallItem(wheel.get(index)));
            prevSlotIndex[0] = index;
            float progress = (float) currentSpin[0] / plan.totalSpins();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, Math.max(1.5f - progress * 0.8f, 0.5f));
            currentSpin[0]++;
        }, 0L, 1L);
    }

    private static void handleResult(Player player, Inventory menu, RouletteBet bet, RouletteNumber result, Main plugin) {
        double payout = RouletteGame.evaluate(bet, result);
        String colorName = switch (result.color()) {
            case "red" -> "§cRed";
            case "black" -> "§8Black";
            default -> "§aGreen";
        };

        if (payout > 0) {
            plugin.getCoreAPI().getEconomy().give(player.getUniqueId(), payout);
            if (payout >= bet.amount() * 10) {
                player.sendTitle("§b§lJACKPOT!", "§e" + result.number() + " (" + colorName + "§e) +" + String.format("%.1f", payout), 10, 60, 20);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            } else {
                player.sendTitle("§a§lWIN!", "§e" + result.number() + " (" + colorName + "§e) +" + String.format("%.1f", payout), 10, 60, 20);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
        } else {
            player.sendTitle("§c§lLOSS", "§7" + result.number() + " (" + colorName + "§7)", 10, 60, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
        }

        menu.setItem(31, makeBalanceItem(player, plugin));
        plugin.getHistoryManager().record(player.getUniqueId(), GameRecord.of("roulette", bet.amount(), payout));
    }

    // --- Item builders ---

    private static ItemStack makeNumberItem(RouletteNumber num) {
        Material mat = switch (num.color()) {
            case "red" -> Material.RED_STAINED_GLASS_PANE;
            case "black" -> Material.BLACK_STAINED_GLASS_PANE;
            default -> Material.LIME_STAINED_GLASS_PANE;
        };
        String color = switch (num.color()) {
            case "red" -> "§c";
            case "black" -> "§8";
            default -> "§a";
        };
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + "§l" + num.number());
        meta.setLore(List.of("§7Click to bet on §e" + num.number(), "§7Payout: §ex35"));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeBallItem(RouletteNumber num) {
        ItemStack item = new ItemStack(Material.SNOWBALL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f§l● " + num.number());
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeBetTypeButton(Player player) {
        int index = betTypeIndex.getOrDefault(player.getUniqueId(), 0);
        BetType current = BET_CYCLE[index];
        ItemStack item = new ItemStack(getBetWool(current));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§7Bet Type: " + current.displayName);
        meta.setLore(List.of(
                "§7Click to cycle bet type",
                "§7Payout: §e" + current.payout,
                "",
                "§8Next: " + BET_CYCLE[(index + 1) % BET_CYCLE.length].displayName
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeBetInfo(Player player, Main plugin) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§lCurrent Bet");
        RouletteBet bet = bets.get(player.getUniqueId());
        if (bet == null) {
            meta.setLore(List.of("§7Type: §fNot selected", "§7Amount: §f0"));
        } else {
            String typeDisplay = bet.type() == BetType.NUMBER ? "§fNumber §e" + bet.number() : bet.type().displayName;
            meta.setLore(List.of(
                    "§7Type: " + typeDisplay,
                    "§7Amount: §e" + String.format("%.1f", bet.amount()),
                    "§7Payout: §e" + bet.type().payout
            ));
        }
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeSpinButton() {
        ItemStack item = new ItemStack(Material.LEVER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§lSPIN!");
        meta.setLore(List.of("§7Click to spin the wheel!"));
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

    private static ItemStack makePane(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private static Material getBetWool(BetType type) {
        return switch (type) {
            case RED -> Material.RED_WOOL;
            case BLACK -> Material.GRAY_WOOL;
            case GREEN -> Material.LIME_WOOL;
            case EVEN -> Material.LIGHT_BLUE_WOOL;
            case ODD -> Material.MAGENTA_WOOL;
            case DOZEN_1 -> Material.YELLOW_WOOL;
            case DOZEN_2 -> Material.ORANGE_WOOL;
            case DOZEN_3 -> Material.BROWN_WOOL;
            default -> Material.WHITE_WOOL;
        };
    }
}
