    private static ItemStack makeBalanceItem(Player player, Main plugin) {
        double balance = 0.0;
        if (plugin != null && plugin.getCoreAPI() != null) {
            balance = plugin.getCoreAPI().getEconomy().getBalance(player.getUniqueId());
        } else {
            Bukkit.getLogger().warning("[AkitosGambling] Could not get balance for " + player.getName() + " -- CoreAPI is null.");
        }
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lBalance");
            meta.setLore(List.of("§e" + String.format("%.1f", balance)));
            item.setItemMeta(meta);
        }
        return item;
    }