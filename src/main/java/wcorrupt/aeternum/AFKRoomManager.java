package wcorrupt.aeternum;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class AFKRoomManager implements CommandExecutor {
    private final JavaPlugin plugin;
    private ItemStack afkItem;
    private final Set<Player> afkPlayers = new HashSet<>();
    private File afkFile;
    private FileConfiguration afkConfig;

    public AFKRoomManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.afkFile = new File(plugin.getDataFolder(), "afk.yml");
        if (!afkFile.exists()) {
            try {
                afkFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.afkConfig = YamlConfiguration.loadConfiguration(afkFile);
        loadAFKItem();
        startAFKRewardTask();
        startAFKCheckTask();
    }

    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            throw new RuntimeException("WorldGuard plugin not found!");
        }
        return (WorldGuardPlugin) plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must hold an item in your hand to set it as the AFK reward.");
            return true;
        }

        afkItem = itemInHand.clone();
        afkItem.setAmount(1); // Ignore stack size
        saveAFKItem();
        player.sendMessage(ChatColor.GREEN + "AFK reward item set to the item in your hand.");
        return true;
    }

    private void saveAFKItem() {
        afkConfig.set("afkItem", afkItem);
        try {
            afkConfig.save(afkFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAFKItem() {
        afkItem = afkConfig.getItemStack("afkItem", new ItemStack(Material.APPLE)); // Default item
    }

    private void startAFKRewardTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (isInAFKRegion(player)) {
                            player.getInventory().addItem(afkItem);
                        }
                    }
                    Bukkit.broadcastMessage(ChatColor.GREEN + "All players have gotten an AFK reward!");
                });
            }
        }.runTaskTimer(plugin, 0L, 2400L); // Run every 3 minutes (3600 ticks)
    }

    private boolean isInAFKRegion(Player player) {
        WorldGuardPlugin wgPlugin = getWorldGuard();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        if (container == null) {
            return false;
        }

        RegionManager regionManager = container.get(BukkitAdapter.adapt(player.getWorld()));
        if (regionManager == null) {
            return false;
        }

        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
        for (ProtectedRegion region : set) {
            if (region.getId().equalsIgnoreCase("afk")) {
                return true;
            }
        }
        return false;
    }

    private void startAFKCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (isInAFKRegion(player)) {
                        afkPlayers.add(player);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 105, 0, false, false, false));
                    } else {
                        afkPlayers.remove(player);
                        //player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 100L); // Run every 5 seconds (100 ticks)
    }
}
