package wcorrupt.aeternum;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateWarpCommand implements CommandExecutor {

    private final Aeternum plugin;

    public CreateWarpCommand(Aeternum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can set warps.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("Usage: /createwarp <name>");
            return true;
        }

        Player player = (Player) sender;
        Location loc = player.getLocation();
        String warpName = args[0];

        plugin.getWarpsConfig().set("warps." + warpName + ".world", loc.getWorld().getName());
        plugin.getWarpsConfig().set("warps." + warpName + ".x", loc.getX());
        plugin.getWarpsConfig().set("warps." + warpName + ".y", loc.getY());
        plugin.getWarpsConfig().set("warps." + warpName + ".z", loc.getZ());
        plugin.getWarpsConfig().set("warps." + warpName + ".yaw", loc.getYaw());
        plugin.getWarpsConfig().set("warps." + warpName + ".pitch", loc.getPitch());
        plugin.saveWarpsConfig();

        player.sendMessage("Warp '" + warpName + "' created!");
        return true;
    }
}
