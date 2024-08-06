package wcorrupt.aeternum;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ComboTracker implements Listener, CommandExecutor {
    private final JavaPlugin plugin;
    private boolean comboTrackingEnabled = true;
    private final Map<UUID, UUID> lastAttacker = new HashMap<>();
    private final Map<String, Integer> comboCounts = new HashMap<>();

    public ComboTracker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!comboTrackingEnabled) return;

        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            UUID attackerId = attacker.getUniqueId();
            UUID victimId = victim.getUniqueId();

            // Reset the attacker's combo on the victim if the victim attacks back
            if (lastAttacker.get(attackerId) != null && lastAttacker.get(attackerId).equals(victimId)) {
                String comboKey = attackerId.toString() + "-" + victimId.toString();
                comboCounts.put(comboKey, 0);
                lastAttacker.remove(attackerId);
                attacker.sendMessage(ChatColor.RED + "Your combo on " + victim.getName() + " has been reset!");
            }

            lastAttacker.put(victimId, attackerId);

            String comboKey = attackerId.toString() + "-" + victimId.toString();
            comboCounts.put(comboKey, comboCounts.getOrDefault(comboKey, 0) + 1);
            int comboCount = comboCounts.get(comboKey);

            if (comboCount % 5 == 0) {
                Bukkit.broadcastMessage(ChatColor.GOLD + attacker.getName() + " has a combo of " + comboCount + " on " + victim.getName() + "!");
            }

            event.setDamage(0); // Prevents health reduction but keeps knockback
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("togglecombos")) {
            comboTrackingEnabled = !comboTrackingEnabled;
            sender.sendMessage(ChatColor.YELLOW + "Combo tracking is now " + (comboTrackingEnabled ? "enabled" : "disabled") + ".");
            return true;
        }
        return false;
    }
}
