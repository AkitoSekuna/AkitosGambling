package com.akito_sekuna.gambling;

import com.akito_sekuna.gambling.utils.GameRecord;
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
                sender.sendMessage("§cYou don't have permission to do this!");
                return true;
            }
            plugin.getConfigManager().reload();
            sender.sendMessage("§aAkitosGambling reloaded!");
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            sendInfo(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("history")) {
            if (!sender.hasPermission("akitosgambling.admin")) {
                sender.sendMessage("§cYou don't have permission to do this!");
                return true;
            }
            if (args.length < 2) { sender.sendMessage("§cUsage: /ag history <player>"); return true; }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) { sender.sendMessage("§cPlayer not found!"); return true; }
            List<GameRecord> history = plugin.getHistoryManager().getHistory(target.getUniqueId());
            sender.sendMessage("§8--- §6" + target.getName() + "'s History §8---");
            if (history.isEmpty()) {
                sender.sendMessage("§7No notable games recorded.");
            } else {
                for (GameRecord r : history) {
                    String result = r.win() ? "§aWIN" : "§cLOSS";
                    sender.sendMessage("§7" + r.gameType() + " §8| " + result + " §8| §7Bet: §f" + r.betAmount()
                            + " §8| §7Payout: §f" + r.payout() + " §8| §7Ratio: §f" + String.format("%.1f", r.ratio()) + "x");
                }
            }
            if (plugin.getHistoryManager().isFlagged(target.getUniqueId())) {
                sender.sendMessage("§c[FLAGGED: " + plugin.getHistoryManager().getFlagReason(target.getUniqueId()) + "]");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("unflag")) {
            if (!sender.hasPermission("akitosgambling.admin")) {
                sender.sendMessage("§cYou don't have permission to do this!");
                return true;
            }
            if (args.length < 2) { sender.sendMessage("§cUsage: /ag unflag <player>"); return true; }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) { sender.sendMessage("§cPlayer not found!"); return true; }
            plugin.getHistoryManager().unflag(target.getUniqueId());
            sender.sendMessage("§aUnflagged §f" + target.getName() + "§a.");
            return true;
        }

        sendInfo(sender);
        return true;
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage("§8--- §6AkitosGambling §8---");
        sender.sendMessage("§7Version: §f" + plugin.getPluginMeta().getVersion());
        sender.sendMessage("§7Author: §fAkito_Sekuna");
        sender.sendMessage("§7/slots §8- §7Play slots");
        sender.sendMessage("§7/roulette §8- §7Play roulette");
        sender.sendMessage("§7/ag reload §8- §7Reload config");
        sender.sendMessage("§7/ag history <player> §8- §7View player history");
        sender.sendMessage("§7/ag unflag <player> §8- §7Unflag a player");
    }
}
