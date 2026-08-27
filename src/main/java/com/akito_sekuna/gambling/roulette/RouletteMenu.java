package com.akito_sekuna.gambling.roulette;

import com.akito_sekuna.gambling.Main;
import com.akito_sekuna.gambling.utils.GameRecord;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RouletteMenu {

    public static final Component TITLE = Component.text("Roulette", NamedTextColor.GOLD).decorate(TextDecoration.BOLD);
    public static final int[] WHEEL_SLOTS = {4, 5, 6, 7, 8, 17, 26, 35, 44, 53, 52, 51, 50, 49, 48, 47, 46, 45, 36, 27, 18, 9, 0, 1, 2, 3};

    private static final Map<UUID, Boolean> spinning = new HashMap<>();
    private static final Map<UUID, RouletteBet> bets = new HashMap<>();
    private static final Map<UUID, Double> betAmounts = new HashMap<>();
    private static final Map<UUID, Integer> betTypeIndex = new HashMap<>();

    private static final BetType[] BET_CYCLE = {
            BetType.RED, BetType.BLACK, BetType.GREEN,
            BetType.EVEN, BetType.ODD,
            BetType.DOZEN_1, BetType.DOZEN_2
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
        for (int i = 0; i < 54; i++) {
            menu.setItem(i, new ItemStack(Material.AIR));
        }

        List<RouletteNumber> wheel = RouletteWheel.WHEEL;
        for (int i = 0; i < Math.min(WHEEL_SLOTS.length, wheel.size()); i++) {
            menu.setItem(WHEEL_SLOTS[i], makeNumberItem(wheel.get(i)));
        }

        menu.setItem(20, makeBetTypeButton(player));
        menu.setItem(22, makeBetInfo(player, plugin));
        menu.setItem(24, makeSpinButton());
        menu.setItem(29, makePane(Material.LIME_DYE, Component.text("+ Bet", NamedTextColor.GREEN).decorate(TextDecoration.BOLD)));
        menu.setItem(33, makePane(Material.RED_DYE, Component.text("- Bet", NamedTextColor.RED).decorate(TextDecoration.BOLD)));
        menu.setItem(31, makeBalanceItem(player, plugin));
    }

    public static void cycleBetType(Player player, Inventory menu, Main plugin) {
        int index = (betTypeIndex.getOrDefault(player.getUniqueId(), 0) + 1) % BET_CYCLE.length;
        betTypeIndex.put(player.getUniqueId(), index);

        double amount = betAmounts.getOrDefault(player.getUniqueId(), plugin.getConfigManager().getRouletteMinBet());
        bets.put(player.getUniqueId(), RouletteBet.of(BET_CYCLE[index], amount));

        menu.setItem(20, makeBetTypeButton(player));
        menu.setItem(22, makeBetInfo(player, plugin));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        player.sendActionBar(Component.text()
                .append(Component.text("Bet type: ", NamedTextColor.GRAY))
                .append(legacyBetName(BET_CYCLE[index]))
                .build());
    }

    public static void setBetNumber(Player player, Inventory menu, int number, Main plugin) {
        double amount = betAmounts.getOrDefault(player.getUniqueId(), plugin.getConfigManager().getRouletteMinBet());
        bets.put(player.getUniqueId(), RouletteBet.ofNumber(amount, number));

        menu.setItem(22, makeBetInfo(player, plugin));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        player.sendActionBar(Component.text()
                .append(Component.text("Betting on number: ", NamedTextColor.GRAY))
                .append(Component.text(formatNumber(number), NamedTextColor.YELLOW))
                .build());
    }

    public static void adjustBet(Player player, Inventory menu, boolean increase, Main plugin) {
        double minBet = plugin.getConfigManager().getRouletteMinBet();
        double maxBet = plugin.getConfigManager().getRouletteMaxBet();
        double amount = betAmounts.getOrDefault(player.getUniqueId(), minBet);

        if (increase) {
            double newAmount = Math.min(amount + minBet, maxBet);
            double balance = plugin.getCoreAPI().getEconomy().getBalance(player.getUniqueId());
            if (balance < newAmount) {
                player.sendActionBar(Component.text("Not enough balance to raise bet!", NamedTextColor.RED));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
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
            player.sendActionBar(Component.text("Wait for the current spin to finish!", NamedTextColor.RED));
            return;
        }

        RouletteBet bet = bets.get(player.getUniqueId());
        if (bet == null) {
            player.sendActionBar(Component.text("Choose a bet type first!", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        double balance = plugin.getCoreAPI().getEconomy().getBalance(player.getUniqueId());
        if (balance < bet.amount()) {
            player.sendActionBar(Component.text("Not enough balance!", NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        plugin.getCoreAPI().getEconomy().take(player.getUniqueId(), bet.amount());
        spinning.put(player.getUniqueId(), true);
        menu.setItem(31, makeBalanceItem(player, plugin));

        RouletteWheel.SpinPlan plan = RouletteWheel.plan();
        List<RouletteNumber> wheel = RouletteWheel.WHEEL;
        int[] currentSpin = {0};
        int[] prevSlotIndex = {-1};

        Bukkit.getScheduler().runTaskTimer((Plugin) plugin, task -> {
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

            int index = currentSpin[0] % WHEEL_SLOTS.length;
            menu.setItem(WHEEL_SLOTS[index], makeBallItem(wheel.get(index)));
            prevSlotIndex[0] = index;

            float progress = (float) currentSpin[0] / (float) plan.totalSpins();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, Math.max(1.5f - progress * 0.8f, 0.5f));

            currentSpin[0]++;
        }, 0L, 1L);
    }

    private static void handleResult(Player player, Inventory menu, RouletteBet bet, RouletteNumber result, Main plugin) {
        double payout = RouletteGame.evaluate(bet, result);

        NamedTextColor colorTint;
        String colorLabel;
        switch (result.color()) {
            case "red":
                colorTint = NamedTextColor.RED;
                colorLabel = "Red";
                break;
            case "black":
                colorTint = NamedTextColor.DARK_GRAY;
                colorLabel = "Black";
                break;
            default:
                colorTint = NamedTextColor.GREEN;
                colorLabel = "Green";
                break;
        }

        if (payout > 0.0) {
            plugin.getCoreAPI().getEconomy().give(player.getUniqueId(), payout);
            Component subtitle = resultSubtitle(result, colorTint, colorLabel, NamedTextColor.YELLOW, "+" + String.format("%.0f", payout));
            if (payout >= bet.amount() * 10.0) {
                showTitle(player,
                        Component.text("JACKPOT!", NamedTextColor.AQUA).decorate(TextDecoration.BOLD),
                        subtitle);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            } else {
                showTitle(player,
                        Component.text("WIN!", NamedTextColor.GREEN).decorate(TextDecoration.BOLD),
                        subtitle);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        } else {
            Component subtitle = resultSubtitle(result, colorTint, colorLabel, NamedTextColor.GRAY, null);
            showTitle(player,
                    Component.text("LOSS", NamedTextColor.RED).decorate(TextDecoration.BOLD),
                    subtitle);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
        }

        menu.setItem(31, makeBalanceItem(player, plugin));
        plugin.getGamesPlayedTracker().record("roulette");
        plugin.getHistoryManager().record(player.getUniqueId(), GameRecord.of("roulette", bet.amount(), payout));
    }

    private static Component resultSubtitle(RouletteNumber result, NamedTextColor colorTint, String colorLabel,
                                              NamedTextColor baseColor, String suffix) {
        TextComponent.Builder builder = Component.text()
                .append(Component.text(formatNumber(result.number()), baseColor))
                .append(Component.text(" (", baseColor))
                .append(Component.text(colorLabel, colorTint))
                .append(Component.text(")", baseColor));
        if (suffix != null) {
            builder.append(Component.text(" " + suffix, baseColor));
        }
        return builder.build();
    }

    private static void showTitle(Player player, Component title, Component subtitle) {
        player.showTitle(Title.title(title, subtitle,
                Title.Times.times(Duration.ofMillis(500L), Duration.ofMillis(3000L), Duration.ofMillis(1000L))));
    }

    private static ItemStack makeNumberItem(RouletteNumber num) {
        Material mat = switch (num.color()) {
            case "red" -> Material.RED_STAINED_GLASS_PANE;
            case "black" -> Material.BLACK_STAINED_GLASS_PANE;
            default -> Material.LIME_STAINED_GLASS_PANE;
        };
        NamedTextColor color = switch (num.color()) {
            case "red" -> NamedTextColor.RED;
            case "black" -> NamedTextColor.DARK_GRAY;
            default -> NamedTextColor.GREEN;
        };

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(formatNumber(num.number()), color).decorate(TextDecoration.BOLD));
        meta.lore(List.of(
                Component.text()
                        .append(Component.text("Click to bet on ", NamedTextColor.GRAY))
                        .append(Component.text(formatNumber(num.number()), NamedTextColor.YELLOW))
                        .build(),
                Component.text()
                        .append(Component.text("Payout: ", NamedTextColor.GRAY))
                        .append(Component.text(BetType.NUMBER.payout, NamedTextColor.YELLOW))
                        .build()
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeBallItem(RouletteNumber num) {
        ItemStack item = new ItemStack(Material.SNOWBALL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text()
                .append(Component.text("\u25cf ", NamedTextColor.WHITE).decorate(TextDecoration.BOLD))
                .append(Component.text(formatNumber(num.number()), NamedTextColor.WHITE).decorate(TextDecoration.BOLD))
                .build());
        item.setItemMeta(meta);
        return item;
    }

    private static String formatNumber(int number) {
        return number == -1 ? "00" : String.valueOf(number);
    }

    private static ItemStack makeBetTypeButton(Player player) {
        int index = betTypeIndex.getOrDefault(player.getUniqueId(), 0);
        BetType current = BET_CYCLE[index];

        ItemStack item = new ItemStack(getBetWool(current));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text()
                .append(Component.text("Bet Type: ", NamedTextColor.GRAY))
                .append(legacyBetName(current))
                .build());
        meta.lore(List.of(
                Component.text("Click to cycle bet type", NamedTextColor.GRAY),
                Component.text()
                        .append(Component.text("Payout: ", NamedTextColor.GRAY))
                        .append(Component.text(current.payout, NamedTextColor.YELLOW))
                        .build(),
                Component.empty(),
                Component.text()
                        .append(Component.text("Next: ", NamedTextColor.DARK_GRAY))
                        .append(legacyBetName(BET_CYCLE[(index + 1) % BET_CYCLE.length]))
                        .build()
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeBetInfo(Player player, Main plugin) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Current Bet", NamedTextColor.AQUA).decorate(TextDecoration.BOLD));

        RouletteBet bet = bets.get(player.getUniqueId());
        if (bet == null) {
            meta.lore(List.of(
                    Component.text()
                            .append(Component.text("Type: ", NamedTextColor.GRAY))
                            .append(Component.text("Not selected", NamedTextColor.WHITE))
                            .build(),
                    Component.text()
                            .append(Component.text("Amount: ", NamedTextColor.GRAY))
                            .append(Component.text("0", NamedTextColor.WHITE))
                            .build()
            ));
        } else {
            Component typeDisplay = bet.type() == BetType.NUMBER
                    ? Component.text()
                            .append(Component.text("Number ", NamedTextColor.WHITE))
                            .append(Component.text(formatNumber(bet.number()), NamedTextColor.YELLOW))
                            .build()
                    : legacyBetName(bet.type());
            meta.lore(List.of(
                    Component.text()
                            .append(Component.text("Type: ", NamedTextColor.GRAY))
                            .append(typeDisplay)
                            .build(),
                    Component.text()
                            .append(Component.text("Amount: ", NamedTextColor.GRAY))
                            .append(Component.text(String.format("%.0f", bet.amount()), NamedTextColor.YELLOW))
                            .build(),
                    Component.text()
                            .append(Component.text("Payout: ", NamedTextColor.GRAY))
                            .append(Component.text(bet.type().payout, NamedTextColor.YELLOW))
                            .build()
            ));
        }

        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makeSpinButton() {
        ItemStack item = new ItemStack(Material.LEVER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("SPIN!", NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
        meta.lore(List.of(Component.text("Click to spin the wheel!", NamedTextColor.GRAY)));
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

    private static ItemStack makePane(Material mat, Component name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private static Component legacyBetName(BetType type) {
        return LegacyComponentSerializer.legacySection().deserialize(type.displayName);
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
            default -> Material.WHITE_WOOL;
        };
    }
}
