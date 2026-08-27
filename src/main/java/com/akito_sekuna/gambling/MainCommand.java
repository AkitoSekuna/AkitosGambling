package com.akito_sekuna.gambling;

import com.akito_sekuna.gambling.utils.GameRecord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainCommand implements CommandExecutor {

    private final Main plugin;

    public MainCommand(Main plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) { sendInfo(sender); return true; }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("akitosgambling.admin")) {
                sender.sendMessage(Component.text("You don't have permission to do this!", NamedTextColor.RED));
                return true;
            }
            plugin.getConfigManager().reload();
            sender.sendMessage(Component.text("AkitosGambling reloaded!", NamedTextColor.GREEN));
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            sendInfo(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("history")) {
            if (!sender.hasPermission("akitosgambling.admin")) {
                sender.sendMessage(Component.text("You don't have permission to do this!", NamedTextColor.RED));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(Component.text("Usage: /ag history <player>", NamedTextColor.RED));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return true;
            }
            List<GameRecord> history = plugin.getHistoryManager().getHistory(target.getUniqueId());
            sender.sendMessage(Component.text()
                    .append(Component.text("--- ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(target.getName() + "'s History", NamedTextColor.GOLD))
                    .append(Component.text(" ---", NamedTextColor.DARK_GRAY))
                    .build());
            if (history.isEmpty()) {
                sender.sendMessage(Component.text("No notable games recorded.", NamedTextColor.GRAY));
            } else {
                for (GameRecord r : history) {
                    Component result = r.win()
                            ? Component.text("WIN", NamedTextColor.GREEN)
                            : Component.text("LOSS", NamedTextColor.RED);
                    sender.sendMessage(Component.text()
                            .append(Component.text(r.gameType(), NamedTextColor.GRAY))
                            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                            .append(result)
                            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                            .append(Component.text("Bet: ", NamedTextColor.GRAY))
                            .append(Component.text(r.betAmount(), NamedTextColor.WHITE))
                            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                            .append(Component.text("Payout: ", NamedTextColor.GRAY))
                            .append(Component.text(r.payout(), NamedTextColor.WHITE))
                            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                            .append(Component.text("Ratio: ", NamedTextColor.GRAY))
                            .append(Component.text(String.format("%.1f", r.ratio()) + "x", NamedTextColor.WHITE))
                            .build());
                }
            }
            if (plugin.getHistoryManager().isFlagged(target.getUniqueId())) {
                sender.sendMessage(Component.text(
                        "[FLAGGED: " + plugin.getHistoryManager().getFlagReason(target.getUniqueId()) + "]",
                        NamedTextColor.RED));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("unflag")) {
            if (!sender.hasPermission("akitosgambling.admin")) {
                sender.sendMessage(Component.text("You don't have permission to do this!", NamedTextColor.RED));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(Component.text("Usage: /ag unflag <player>", NamedTextColor.RED));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return true;
            }
            plugin.getHistoryManager().unflag(target.getUniqueId());
            sender.sendMessage(Component.text()
                    .append(Component.text("Unflagged ", NamedTextColor.GREEN))
                    .append(Component.text(target.getName(), NamedTextColor.WHITE))
                    .append(Component.text(".", NamedTextColor.GREEN))
                    .build());
            return true;
        }

        sendInfo(sender);
        return true;
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage(Component.text()
                .append(Component.text("--- ", NamedTextColor.DARK_GRAY))
                .append(Component.text("AkitosGambling", NamedTextColor.GOLD))
                .append(Component.text(" ---", NamedTextColor.DARK_GRAY))
                .build());
        sender.sendMessage(Component.text()
                .append(Component.text("Version: ", NamedTextColor.GRAY))
                .append(Component.text(plugin.getPluginMeta().getVersion(), NamedTextColor.WHITE))
                .build());
        sender.sendMessage(Component.text()
                .append(Component.text("Author: ", NamedTextColor.GRAY))
                .append(Component.text("Akito_Sekuna", NamedTextColor.WHITE))
                .build());
        sender.sendMessage(infoLine("/slots", "Play slots"));
        sender.sendMessage(infoLine("/roulette", "Play roulette"));
        sender.sendMessage(infoLine("/ag reload", "Reload config"));
        sender.sendMessage(infoLine("/ag history <player>", "View player history"));
        sender.sendMessage(infoLine("/ag unflag <player>", "Unflag a player"));
    }

    private Component infoLine(String command, String description) {
        return Component.text()
                .append(Component.text(command, NamedTextColor.GRAY))
                .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
                .append(Component.text(description, NamedTextColor.GRAY))
                .build();
    }
}
