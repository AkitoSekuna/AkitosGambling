package com.akito_sekuna.gambling.roulette;

import com.akito_sekuna.gambling.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RouletteCommand implements CommandExecutor {

    private final Main plugin;

    public RouletteCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!"));
            return true;
        }
        if (plugin.getCoreAPI() == null) {
            player.sendMessage(Component.text("AkitosCore is not available. Try again in a moment.", NamedTextColor.RED));
            return true;
        }
        RouletteMenu.open(player, plugin);
        return true;
    }
}
