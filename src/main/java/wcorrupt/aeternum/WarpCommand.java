package wcorrupt.aeternum;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpCommand implements CommandExecutor {

    private final Aeternum plugin;

    public WarpCommand(Aeternum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("Usage: /warp <name>");
            return true;
        }

        Player player = (Player) sender;
        String warpName = args[0];

        if (!plugin.getWarpsConfig().contains("warps." + warpName)) {
            player.sendMessage("Warp '" + warpName + "' does not exist.");
            return true;
        }

        World world = Bukkit.getWorld(plugin.getWarpsConfig().getString("warps." + warpName + ".world"));
        double x = plugin.getWarpsConfig().getDouble("warps." + warpName + ".x");
        double y = plugin.getWarpsConfig().getDouble("warps." + warpName + ".y");
        double z = plugin.getWarpsConfig().getDouble("warps." + warpName + ".z");
        float yaw = (float) plugin.getWarpsConfig().getDouble("warps." + warpName + ".yaw");
        float pitch = (float) plugin.getWarpsConfig().getDouble("warps." + warpName + ".pitch");

        if (world == null) {
            player.sendMessage("The world '" + plugin.getWarpsConfig().getString("warps." + warpName + ".world") + "' does not exist.");
            return true;
        }

        Location warpLocation = new Location(world, x, y, z, yaw, pitch);
        player.teleport(warpLocation);
        player.sendMessage("Warped to '" + warpName + "'!");

        return true;
    }
}
