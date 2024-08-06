package wcorrupt.aeternum;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class NameSanitizer implements Listener {

    private final JavaPlugin plugin;

    public NameSanitizer(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Get the player who joined
        Player player = event.getPlayer();

        // Set display name and player list name to white
        String whiteName = ChatColor.WHITE + player.getName();
        player.setDisplayName(whiteName);
        player.setPlayerListName(whiteName);

        // Log the action
        plugin.getLogger().info("Set name to white for player: " + player.getName());
    }
}
