package wcorrupt.aeternum;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class TrashCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;

    public TrashCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("aeternum.trash")) {
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }

        Inventory trashInventory = Bukkit.createInventory(null, 54, "Trash");
        player.openInventory(trashInventory);
        return true;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Trash")) {
            event.getInventory().clear();
        }
    }
}
