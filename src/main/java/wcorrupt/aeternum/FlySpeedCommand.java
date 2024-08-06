package wcorrupt.aeternum;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlySpeedCommand implements CommandExecutor {

    private static final String PREFIX = ChatColor.translateAlternateColorCodes('&', "&8(&6&lAeternum&8) ");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(PREFIX + ChatColor.RED + "Usage: /flyspeed <1-10>");
            return true;
        }

        try {
            int speed = Integer.parseInt(args[0]);
            if (speed < 1 || speed > 10) {
                player.sendMessage(PREFIX + ChatColor.RED + "Speed must be between 1 and 10.");
                return true;
            }

            float flySpeed = speed / 10.0f;
            player.setFlySpeed(flySpeed);
            player.sendMessage(PREFIX + ChatColor.GREEN + "Fly speed set to " + speed + ".");
        } catch (NumberFormatException e) {
            player.sendMessage(PREFIX + ChatColor.RED + "Invalid number format.");
        }

        return true;
    }
}
