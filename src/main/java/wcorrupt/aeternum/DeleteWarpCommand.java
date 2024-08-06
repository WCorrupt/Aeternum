package wcorrupt.aeternum;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DeleteWarpCommand implements CommandExecutor {

    private final Aeternum plugin;

    public DeleteWarpCommand(Aeternum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /deletewarp <name>");
            return true;
        }

        String warpName = args[0];

        if (!plugin.getWarpsConfig().contains("warps." + warpName)) {
            sender.sendMessage("Warp '" + warpName + "' does not exist.");
            return true;
        }

        plugin.getWarpsConfig().set("warps." + warpName, null);
        plugin.saveWarpsConfig();

        sender.sendMessage("Warp '" + warpName + "' deleted!");
        return true;
    }
}
