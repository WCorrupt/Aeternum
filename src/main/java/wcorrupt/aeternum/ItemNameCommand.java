package wcorrupt.aeternum;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class ItemNameCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You must hold an item to rename it.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Please provide a name for the item.");
            return false;
        }

        String name = String.join(" ", args);
        String formattedName = translateHexColorCodes(name);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(formattedName);
            item.setItemMeta(meta);
            player.sendMessage(ChatColor.GREEN + "Item renamed to: " + formattedName);
        } else {
            player.sendMessage(ChatColor.RED + "Failed to rename the item.");
        }

        return true;
    }

    private String translateHexColorCodes(String message) {
        StringBuilder result = new StringBuilder();
        char[] chars = message.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&' && i + 7 < chars.length && chars[i + 1] == '#') {
                String hex = message.substring(i + 2, i + 8);
                result.append("§x");
                for (char hexChar : hex.toCharArray()) {
                    result.append('§').append(hexChar);
                }
                i += 7; // Skip the hex code
            } else {
                result.append(chars[i]);
            }
        }

        return result.toString();
    }
}
