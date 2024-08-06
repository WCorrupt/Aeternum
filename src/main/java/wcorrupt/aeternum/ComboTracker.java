package wcorrupt.aeternum;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ComboTracker implements Listener, CommandExecutor {
    private final JavaPlugin plugin; // Reference to the main plugin
    private boolean comboTrackingEnabled = true;
    private final Map<UUID, UUID> lastAttacker = new HashMap<>();
    private final List<ComboData> comboList = new ArrayList<>();
    private final File comboFile;
    private YamlConfiguration comboConfig;

    public ComboTracker(JavaPlugin plugin) {
        this.plugin = plugin;
        this.comboFile = new File(plugin.getDataFolder(), "combosave.yml");
        loadCombos();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!comboTrackingEnabled) return;

        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            UUID attackerId = attacker.getUniqueId();
            UUID victimId = victim.getUniqueId();

            ComboData comboData = comboList.stream()
                    .filter(combo -> combo.getAttackerUUID().equals(attackerId) && combo.getVictimUUID().equals(victimId))
                    .findFirst()
                    .orElseGet(() -> {
                        ComboData newCombo = new ComboData(attackerId, victimId, 0);
                        comboList.add(newCombo);
                        return newCombo;
                    });

            if (lastAttacker.get(attackerId) != null && lastAttacker.get(attackerId).equals(victimId)) {
                if (comboData.getComboCount() >= 20) {
                    Bukkit.broadcast(Component.text(attacker.getName() + " has lost a combo of " + comboData.getComboCount() + " on " + victim.getName() + "!").color(TextColor.color(0xFF5555)));
                }
                comboData.resetCombo();
                lastAttacker.remove(attackerId);
            } else {
                comboData.incrementCombo();
                lastAttacker.put(victimId, attackerId);

                if (comboData.getComboCount() % 20 == 0) {
                    ItemStack item = attacker.getInventory().getItemInMainHand();
                    String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString();
                    String itemDescription = generateItemDescription(item);

                    String coloredItemName = translateHexColorCodes(itemName);
                    TextComponent itemComponent = Component.text(coloredItemName)
                            .hoverEvent(HoverEvent.showText(Component.text(itemDescription)));
                    TextComponent playerComponent = Component.text(attacker.getName())
                            .hoverEvent(HoverEvent.showText(Component.text("Highest Combo\n" + getHighestCombo(attackerId))));

                    TextComponent message = Component.text()
                            .append(playerComponent)
                            .append(Component.text(" is on a combo of " + comboData.getComboCount() + " with "))
                            .append(itemComponent)
                            .append(Component.text("!"))
                            .build();

                    Bukkit.broadcast(message);
                } else if (comboData.getComboCount() % 5 == 0) {
                    attacker.sendMessage(ChatColor.GOLD + "You are on a combo of " + comboData.getComboCount() + "!");
                }
            }

            //event.setDamage(0); // Prevents health reduction but keeps knockback
            addHitCounter(attacker.getInventory().getItemInMainHand());

            saveCombos();
        }
    }

    private void addHitCounter(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            int hitCount = 0;

            boolean found = false;
            if (lore != null && !lore.isEmpty()) {
                for (int i = 0; i < lore.size(); i++) {
                    String line = lore.get(i);
                    if (line.startsWith(ChatColor.GRAY + "[") && line.endsWith("]")) {
                        try {
                            hitCount = Integer.parseInt(line.substring(ChatColor.GRAY.toString().length() + 1, line.length() - 1));
                            hitCount++;
                            lore.set(i, ChatColor.GRAY + "[" + hitCount + "]");
                            found = true;
                            break;
                        } catch (NumberFormatException e) {
                            // Handle the case where the lore format is not as expected
                        }
                    }
                }
            }

            if (!found) {
                return; // Do not add a new hit counter if one is not already present
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("togglecombos")) {
            comboTrackingEnabled = !comboTrackingEnabled;
            sender.sendMessage(ChatColor.YELLOW + "Combo tracking is now " + (comboTrackingEnabled ? "enabled" : "disabled") + ".");
            return true;
        } else if (label.equalsIgnoreCase("comboleaderboard")) {
            showLeaderboard(sender);
            return true;
        } else if (label.equalsIgnoreCase("addhitcounter")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item != null && item.getType() != Material.AIR) {
                    addHitCounter(item);
                    player.sendMessage(ChatColor.GREEN + "Hit counter added or updated on your item!");
                } else {
                    player.sendMessage(ChatColor.RED + "You must hold an item in your main hand.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            }
            return true;
        }
        return false;
    }

    private void showLeaderboard(CommandSender sender) {
        List<ComboData> topCombos = comboList.stream()
                .sorted(Comparator.comparingInt(ComboData::getComboCount).reversed())
                .limit(10)
                .collect(Collectors.toList());

        sender.sendMessage(ChatColor.GREEN + "Top Combos:");
        for (int i = 0; i < topCombos.size(); i++) {
            ComboData data = topCombos.get(i);
            String attackerName = getPlayerName(data.getAttackerUUID());
            String victimName = getPlayerName(data.getVictimUUID());

            sender.sendMessage(ChatColor.GOLD + "" + (i + 1) + ". " + attackerName + " on " + victimName + ": " + data.getComboCount());
        }
    }

    private String getPlayerName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return player.getName();
        } else {
            return Bukkit.getOfflinePlayer(uuid).getName();
        }
    }

    private void saveCombos() {
        comboConfig = new YamlConfiguration();
        for (int i = 0; i < comboList.size(); i++) {
            ComboData data = comboList.get(i);
            String key = "combo" + i;
            comboConfig.set(key + ".attackerUUID", data.getAttackerUUID().toString());
            comboConfig.set(key + ".victimUUID", data.getVictimUUID().toString());
            comboConfig.set(key + ".count", data.getComboCount());
        }
        try {
            comboConfig.save(comboFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCombos() {
        if (comboFile.exists()) {
            comboConfig = YamlConfiguration.loadConfiguration(comboFile);
            for (String key : comboConfig.getKeys(false)) {
                UUID attackerUUID = UUID.fromString(comboConfig.getString(key + ".attackerUUID"));
                UUID victimUUID = UUID.fromString(comboConfig.getString(key + ".victimUUID"));
                int count = comboConfig.getInt(key + ".count");
                comboList.add(new ComboData(attackerUUID, victimUUID, count));
            }
        }
    }

    private String generateItemDescription(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.hasDisplayName() ? meta.getDisplayName() : item.getType().toString();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            StringBuilder description = new StringBuilder(displayName);
            if (!lore.isEmpty()) {
                description.append("\n").append(String.join("\n", lore));
            }
            return description.toString();
        }
        return "";
    }

    private String translateHexColorCodes(String message) {
        // Convert hex color codes (&#AABBCC) to Minecraft's internal format (§x§A§B§B§C§C)
        message = message.replaceAll("(?i)&#([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])",
                "§x§$1§$2§$3§$4§$5§$6");
        // Convert standard color codes (&1, &f, etc.) to Minecraft's internal format (§1, §f, etc.)
        message = message.replaceAll("&([0-9a-fA-Fk-oK-OrR])", "§$1");
        return message;
    }

    private int getHighestCombo(UUID playerUUID) {
        return comboList.stream()
                .filter(combo -> combo.getAttackerUUID().equals(playerUUID))
                .mapToInt(ComboData::getComboCount)
                .max()
                .orElse(0);
    }

    private static class ComboData {
        private final UUID attackerUUID;
        private final UUID victimUUID;
        private int comboCount;

        public ComboData(UUID attackerUUID, UUID victimUUID, int comboCount) {
            this.attackerUUID = attackerUUID;
            this.victimUUID = victimUUID;
            this.comboCount = comboCount;
        }

        public UUID getAttackerUUID() {
            return attackerUUID;
        }

        public UUID getVictimUUID() {
            return victimUUID;
        }

        public int getComboCount() {
            return comboCount;
        }

        public void incrementCombo() {
            comboCount++;
        }

        public void resetCombo() {
            comboCount = 0;
        }
    }
}
