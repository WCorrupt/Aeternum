package wcorrupt.aeternum;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KitTabCompleter implements TabCompleter {

    private final Aeternum plugin;

    public KitTabCompleter(Aeternum plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            Set<String> kitNames = plugin.getKitsConfig().getConfigurationSection("kits").getKeys(false);
            return new ArrayList<>(kitNames).stream()
                    .filter(kit -> kit.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
