package wcorrupt.aeternum;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {

    private static final String PREFIX = ChatColor.translateAlternateColorCodes('&', "&8(&6&lAeternum&8) ");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0) {
            if (!player.hasPermission("aeternum.fly.others")) {
                player.sendMessage(PREFIX + ChatColor.RED + "You don't have permission to toggle fly mode for others.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(PREFIX + ChatColor.RED + "Player not found.");
                return true;
            }

            toggleFlyMode(target);
            player.sendMessage(PREFIX + ChatColor.GREEN + "Toggled fly mode for " + target.getName() + ".");
        } else {
            toggleFlyMode(player);
            player.sendMessage(PREFIX + ChatColor.GREEN + "Toggled fly mode for yourself.");
        }

        return true;
    }

    private void toggleFlyMode(Player player) {
        player.setAllowFlight(!player.getAllowFlight());
        player.setFlying(player.getAllowFlight());
        player.sendMessage(PREFIX + ChatColor.YELLOW + "Fly mode " + (player.getAllowFlight() ? "enabled" : "disabled") + ".");
    }
}
