package wcorrupt.aeternum;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CreateKitCommand implements CommandExecutor {

    private final Aeternum plugin;

    public CreateKitCommand(Aeternum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can create kits.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("Usage: /createkit <name>");
            return true;
        }

        Player player = (Player) sender;
        String kitName = args[0];
        FileConfiguration kitsConfig = plugin.getKitsConfig();

        // Check if a kit with this name already exists
        if (kitsConfig.contains("kits." + kitName)) {
            player.sendMessage("A kit with the name '" + kitName + "' already exists. Please choose a different name.");
            return true;
        }

        // Save inventory contents, armor, and offhand
        kitsConfig.set("kits." + kitName + ".inventory", serializeInventory(player.getInventory().getContents()));
        kitsConfig.set("kits." + kitName + ".armor", serializeInventory(player.getInventory().getArmorContents()));
        kitsConfig.set("kits." + kitName + ".offhand", serializeItem(player.getInventory().getItemInOffHand()));

        plugin.saveKitsConfig();
        plugin.reloadKitsConfig(); // Reload the kits configuration

        player.sendMessage("Kit '" + kitName + "' created!");
        return true;
    }

    private Map<String, Map<String, Object>> serializeInventory(ItemStack[] items) {
        return IntStream.range(0, items.length)
                .boxed()
                .collect(Collectors.toMap(
                        String::valueOf,
                        i -> items[i] != null ? items[i].serialize() : new HashMap<>(),
                        (e1, e2) -> e1,
                        HashMap::new
                ));
    }

    private Map<String, Object> serializeItem(ItemStack item) {
        return item == null ? new HashMap<>() : item.serialize();
    }
}
