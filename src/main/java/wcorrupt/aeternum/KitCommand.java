package wcorrupt.aeternum;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class KitCommand implements CommandExecutor {

    private final Aeternum plugin;

    public KitCommand(Aeternum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use kits.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("Usage: /kit <name>");
            return true;
        }

        Player player = (Player) sender;
        String kitName = args[0];
        FileConfiguration kitsConfig = plugin.getKitsConfig();

        // Check if the kit exists
        if (!kitsConfig.contains("kits." + kitName)) {
            player.sendMessage("Kit '" + kitName + "' does not exist.");
            return true;
        }

        // Clear player's inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setItemInOffHand(null);

        // Load and set inventory contents, armor, and offhand
        ConfigurationSection inventorySection = kitsConfig.getConfigurationSection("kits." + kitName + ".inventory");
        ConfigurationSection armorSection = kitsConfig.getConfigurationSection("kits." + kitName + ".armor");
        ConfigurationSection offhandSection = kitsConfig.getConfigurationSection("kits." + kitName + ".offhand");

        if (inventorySection != null) {
            player.getInventory().setContents(deserializeInventory(inventorySection));
        }

        if (armorSection != null) {
            player.getInventory().setArmorContents(deserializeInventory(armorSection));
        }

        if (offhandSection != null) {
            player.getInventory().setItemInOffHand(deserializeItem(offhandSection.getValues(true)));
        }

        player.sendMessage("You have equipped the kit '" + kitName + "'!");
        return true;
    }

    private ItemStack[] deserializeInventory(ConfigurationSection section) {
        ItemStack[] items = new ItemStack[section.getKeys(false).size()];
        for (String key : section.getKeys(false)) {
            int slot = Integer.parseInt(key);
            items[slot] = deserializeItem(section.getConfigurationSection(key).getValues(true));
        }
        return items;
    }

    private ItemStack deserializeItem(Map<String, Object> itemData) {
        return itemData == null || itemData.isEmpty() ? null : ItemStack.deserialize(itemData);
    }
}
