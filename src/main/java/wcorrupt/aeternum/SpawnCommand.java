package wcorrupt.aeternum;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import wcorrupt.aeternum.Aeternum;

import java.util.HashMap;
import java.util.UUID;

public class SpawnCommand implements CommandExecutor, Listener {
    private final Aeternum plugin;
    private final HashMap<UUID, BukkitRunnable> teleportTasks = new HashMap<>();
    private final Location spawnLocation = new Location(Bukkit.getWorld("building"), -65.500, 66, 17.5, 180, 0);

    public SpawnCommand(Aeternum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;
        if (label.equalsIgnoreCase("spawn")) {
            if (teleportTasks.containsKey(player.getUniqueId())) {
                player.sendMessage("You are already being teleported!");
                return true;
            }

            player.sendMessage("You will be teleported to spawn in 5 seconds. Do not move!");

            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (teleportTasks.containsKey(player.getUniqueId())) {
                        player.teleport(spawnLocation);
                        player.sendMessage("Teleported to spawn!");
                        teleportTasks.remove(player.getUniqueId());
                    }
                }
            };

            task.runTaskLater(plugin, 100);
            teleportTasks.put(player.getUniqueId(), task);

        } else if (label.equalsIgnoreCase("spawnadmin")) {
            if (player.hasPermission("aeternum.spawnadmin")) {
                player.teleport(spawnLocation);
                player.sendMessage("Teleported to spawn instantly!");
            } else {
                player.sendMessage("You do not have permission to use this command.");
            }
        }

        return true;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (teleportTasks.containsKey(player.getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();

            // Check if the player moved their position (ignoring rotation changes)
            if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
                teleportTasks.get(player.getUniqueId()).cancel();
                teleportTasks.remove(player.getUniqueId());
                player.sendMessage("Teleportation cancelled because you moved.");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (teleportTasks.containsKey(player.getUniqueId())) {
            teleportTasks.get(player.getUniqueId()).cancel();
            teleportTasks.remove(player.getUniqueId());
        }
    }
}
