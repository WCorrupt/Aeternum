package wcorrupt.aeternum;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DeleteKitCommand implements CommandExecutor {

    private final Aeternum plugin;

    public DeleteKitCommand(Aeternum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /deletekit <name>");
            return true;
        }

        String kitName = args[0];
        if (!plugin.getKitsConfig().contains("kits." + kitName)) {
            sender.sendMessage("Kit '" + kitName + "' does not exist.");
            return true;
        }

        plugin.getKitsConfig().set("kits." + kitName, null);
        plugin.saveKitsConfig();
        plugin.reloadKitsConfig(); // Reload the kits configuration

        sender.sendMessage("Kit '" + kitName + "' has been deleted.");
        return true;
    }
}
