package wcorrupt.aeternum;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

public class NameSanitizer implements Listener {

    private final JavaPlugin plugin;

    public NameSanitizer(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setDisplayName(ChatColor.GRAY + player.getName());
        player.setPlayerListName(ChatColor.GRAY + player.getName());

        // Remove player from all teams
        for (Team team : player.getScoreboard().getTeams()) {
            team.removeEntry(player.getName());
        }
    }
}