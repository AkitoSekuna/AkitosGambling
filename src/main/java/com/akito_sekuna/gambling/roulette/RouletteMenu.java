import com.akito_sekuna.gambling.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

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

void main() {
}