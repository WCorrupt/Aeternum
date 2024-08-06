package wcorrupt.aeternum;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ShieldDisabler implements Listener, CommandExecutor {
    private final JavaPlugin plugin;
    private final NamespacedKey customItemKey;

    public ShieldDisabler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.customItemKey = new NamespacedKey(plugin, "shieldbreaker");
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player damager = (Player) event.getDamager();
        ItemStack item = damager.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();

        if (dataContainer.has(customItemKey, PersistentDataType.BYTE) && dataContainer.get(customItemKey, PersistentDataType.BYTE) == (byte) 1) {
            // Check if the item is on cooldown
            if (damager.hasCooldown(item.getType())) {
                return; // Do not trigger the shield disable effect if the item is on cooldown
            }

            Player target = (Player) event.getEntity();

            // Apply the shield disable effect
            target.setCooldown(Material.SHIELD, 200); // 200 ticks = 10 seconds
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 200, 1)); // Simulate shield being lowered

            // Apply the item cooldown
            damager.setCooldown(item.getType(), 400); // 400 ticks = 20 seconds
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("giveshieldbreaker")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by a player.");
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("aeternum.giveshieldbreaker")) {
                player.sendMessage("You do not have permission to use this command.");
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) {
                player.sendMessage("You must be holding an item to give it the custom tag.");
                return true;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(customItemKey, PersistentDataType.BYTE, (byte) 1);
                item.setItemMeta(meta);
                player.sendMessage("The item in your hand has been given the shield breaker tag.");
            }
            return true;
        }

        return false;
    }
}
