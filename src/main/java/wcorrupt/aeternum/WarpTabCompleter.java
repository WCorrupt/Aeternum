package wcorrupt.aeternum;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WarpTabCompleter implements TabCompleter {

    private final Aeternum plugin;

    public WarpTabCompleter(Aeternum plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            Set<String> warps = plugin.getWarpsConfig().getConfigurationSection("warps").getKeys(false);
            List<String> warpList = new ArrayList<>(warps);
            warpList.sort(String::compareToIgnoreCase);
            return warpList;
        }
        return null;
    }
}
