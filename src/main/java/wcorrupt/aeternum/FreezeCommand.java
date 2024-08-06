package wcorrupt.aeternum;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;

public class FreezeCommand implements CommandExecutor, Listener {

    private final Aeternum plugin;
    private final Set<Player> frozenPlayers = new HashSet<>();

    public FreezeCommand(Aeternum plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <player>");
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (frozenPlayers.contains(target)) {
            frozenPlayers.remove(target);
            target.sendMessage(ChatColor.GRAY + "(" + ChatColor.GOLD + ChatColor.BOLD + "Aeternum" + ChatColor.GRAY + ") " + ChatColor.WHITE + "You have been unfrozen.");
            sender.sendMessage(ChatColor.GRAY + "(" + ChatColor.GOLD + ChatColor.BOLD + "Aeternum" + ChatColor.GRAY + ") " + ChatColor.WHITE + "You have unfrozen " + target.getName() + ".");
        } else {
            frozenPlayers.add(target);
            target.sendMessage(ChatColor.GRAY + "(" + ChatColor.GOLD + ChatColor.BOLD + "Aeternum" + ChatColor.GRAY + ") " + ChatColor.WHITE + "You have been frozen.");
            sender.sendMessage(ChatColor.GRAY + "(" + ChatColor.GOLD + ChatColor.BOLD + "Aeternum" + ChatColor.GRAY + ") " + ChatColor.WHITE + "You have frozen " + target.getName() + ".");
        }

        return true;
    }

    public boolean isPlayerFrozen(Player player) {
        return frozenPlayers.contains(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (isPlayerFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
