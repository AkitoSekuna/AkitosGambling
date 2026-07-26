package com.akito_sekuna.gambling.roulette;

import com.akito_sekuna.gambling.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RouletteCommand implements CommandExecutor {

    private final Main plugin;

    public RouletteCommand(Main plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        RouletteMenu.open(player, plugin);
        return true;
    }
}
