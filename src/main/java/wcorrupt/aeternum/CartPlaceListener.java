package wcorrupt.aeternum;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class CartPlaceListener implements Listener {

    private final NamespacedKey infiniteKey;

    public CartPlaceListener(JavaPlugin plugin) {
        this.infiniteKey = new NamespacedKey(plugin, "infinite");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Ensure the event is from the main hand and a right-click action
        if (event.getHand() == EquipmentSlot.HAND && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.TNT_MINECART) {
                // Check if the item has the 'infinite' tag
                if (item.getItemMeta() != null && item.getItemMeta().getPersistentDataContainer().has(infiniteKey, PersistentDataType.BYTE)) {
                    if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.RAIL) {
                        event.setCancelled(true); // Prevent the default item use

                        // Check for existing TNT Minecart at the location
                        boolean tntCartPresent = event.getClickedBlock().getWorld().getNearbyEntities(event.getClickedBlock().getLocation(), 1, 1, 1).stream()
                                .anyMatch(entity -> entity instanceof Minecart && entity.getType() == EntityType.MINECART_TNT);

                        if (!tntCartPresent) {
                            // Calculate centered location for the minecart
                            Vector location = event.getClickedBlock().getLocation().toVector();
                            location.add(new Vector(0.5, 0, 0.5));

                            // Spawn TNT Minecart centered on the rail
                            Minecart tntMinecart = (Minecart) event.getClickedBlock().getWorld().spawnEntity(
                                    location.toLocation(event.getClickedBlock().getWorld()), EntityType.MINECART_TNT);

                            // Tag the minecart to prevent drops when broken
                            tntMinecart.getPersistentDataContainer().set(infiniteKey, PersistentDataType.BYTE, (byte) 1);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.getVehicle() instanceof Minecart) {
            Minecart minecart = (Minecart) event.getVehicle();
            // Check if the minecart has the 'infinite' tag
            if (minecart.getPersistentDataContainer().has(infiniteKey, PersistentDataType.BYTE)) {
                event.setCancelled(true); // Cancel the destruction to prevent item drop
                minecart.remove(); // Remove the minecart without dropping items
            }
        }
    }
}
