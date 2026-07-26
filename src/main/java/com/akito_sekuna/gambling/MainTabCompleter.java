package com.akito_sekuna.gambling;

import org.bukkit.command.Command;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("reload", "info", "history", "unflag").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
