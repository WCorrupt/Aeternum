package wcorrupt.aeternum;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class GiveInfCartCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public GiveInfCartCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        ItemStack tntCart = new ItemStack(Material.TNT_MINECART, 1);
        ItemMeta meta = tntCart.getItemMeta();

        // Set a custom tag to identify this as an infinite TNT minecart
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "infinite"), PersistentDataType.BYTE, (byte) 1);

        tntCart.setItemMeta(meta);

        player.getInventory().addItem(tntCart);
        player.sendMessage("You have been given an infinite TNT Minecart!");
        return true;
    }
}
