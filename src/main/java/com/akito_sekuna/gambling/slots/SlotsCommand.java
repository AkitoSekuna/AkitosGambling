package com.akito_sekuna.gambling.slots;

import com.akito_sekuna.gambling.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SlotsCommand implements CommandExecutor {

    private final Main plugin;

    public SlotsCommand(Main plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        SlotsMenu.open(player, plugin);
        return true;
    }
}
