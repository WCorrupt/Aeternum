package wcorrupt.aeternum;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class KitTabCompleter implements TabCompleter {

    private final Aeternum plugin;

    public KitTabCompleter(Aeternum plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            Set<String> kits = plugin.getKitsConfig().getConfigurationSection("kits").getKeys(false);
            List<String> kitList = new ArrayList<>(kits);
            kitList.sort(String::compareToIgnoreCase);
            return kitList;
        }
        return null;
    }
}
