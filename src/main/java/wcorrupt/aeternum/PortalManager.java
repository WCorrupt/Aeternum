package wcorrupt.aeternum;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PortalManager implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private final Map<UUID, String> portalWands = new HashMap<>();
    private final List<Portal> portals = new ArrayList<>();
    private File portalFile;
    private FileConfiguration portalConfig;
    private final Map<UUID, Long> lastCommandExecution = new HashMap<>();

    public PortalManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.portalFile = new File(plugin.getDataFolder(), "portals.yml");
        if (!portalFile.exists()) {
            try {
                portalFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.portalConfig = YamlConfiguration.loadConfiguration(portalFile);
        loadPortals();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /portalwand <command>");
            return true;
        }

        String commandText = String.join(" ", args);
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Portal Wand");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Command: " + commandText);
        meta.setLore(lore);
        wand.setItemMeta(meta);

        portalWands.put(player.getUniqueId(), commandText);
        player.getInventory().addItem(wand);
        player.sendMessage(ChatColor.GREEN + "You have received a Portal Wand with the command: " + commandText);
        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!portalWands.containsKey(player.getUniqueId())) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.STICK) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Portal Wand")) return;

        event.setCancelled(true);

        String command = portalWands.get(player.getUniqueId());
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Location loc = event.getClickedBlock().getLocation();
            portals.add(new Portal(loc, command));
            savePortals();
            player.sendMessage(ChatColor.GREEN + "Portal created with command: " + command);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        for (Portal portal : portals) {
            if (portal.isInside(event.getTo())) {
                long currentTime = System.currentTimeMillis();
                long lastExecution = lastCommandExecution.getOrDefault(player.getUniqueId(), 0L);
                if (currentTime - lastExecution > 1000) {
                    lastCommandExecution.put(player.getUniqueId(), currentTime);
                    player.performCommand(portal.getCommand());
                }
                break;
            }
        }
    }

    private void savePortals() {
        List<Map<String, Object>> portalList = new ArrayList<>();
        for (Portal portal : portals) {
            portalList.add(portal.serialize());
        }
        portalConfig.set("portals", portalList);
        try {
            portalConfig.save(portalFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPortals() {
        List<Map<String, Object>> portalList = (List<Map<String, Object>>) portalConfig.getList("portals", new ArrayList<>());
        for (Map<String, Object> portalMap : portalList) {
            portals.add(Portal.deserialize(portalMap));
        }
    }

    private static class Portal {
        private final Location location;
        private final String command;

        public Portal(Location location, String command) {
            this.location = location;
            this.command = command;
        }

        public boolean isInside(Location loc) {
            return loc.getBlockX() == location.getBlockX() &&
                    loc.getBlockY() == location.getBlockY() &&
                    loc.getBlockZ() == location.getBlockZ();
        }

        public String getCommand() {
            return command;
        }

        public Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();
            map.put("world", location.getWorld().getName());
            map.put("x", location.getBlockX());
            map.put("y", location.getBlockY());
            map.put("z", location.getBlockZ());
            map.put("command", command);
            return map;
        }

        public static Portal deserialize(Map<String, Object> map) {
            Location loc = new Location(
                    Bukkit.getWorld((String) map.get("world")),
                    (int) map.get("x"),
                    (int) map.get("y"),
                    (int) map.get("z")
            );
            String command = (String) map.get("command");
            return new Portal(loc, command);
        }
    }
}
