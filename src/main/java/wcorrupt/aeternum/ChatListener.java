package wcorrupt.aeternum;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChatListener implements Listener, CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final LuckPerms luckPerms;
    private final String[] customFont;
    private final Map<UUID, Boolean> staffChatToggle;

    public ChatListener(JavaPlugin plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
        this.customFont = new String[]{"ᴀ", "ʙ", "ᴄ", "ᴅ", "ᴇ", "ꜰ", "ɢ", "ʜ", "ɪ", "ᴊ", "ᴋ", "ʟ", "ᴍ", "ɴ", "ᴏ", "ᴘ", "ǫ", "ʀ", "ꜱ", "ᴛ", "ᴜ", "ᴠ", "ᴡ", "x", "ʏ", "ᴢ"};
        this.staffChatToggle = new HashMap<>();
        plugin.getCommand("staffchat").setExecutor(this);
        plugin.getCommand("staffchat").setTabCompleter(this);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Handle staff chat
        if (staffChatToggle.getOrDefault(playerId, false)) {
            if (player.hasPermission("staff.chat")) {
                event.setCancelled(true);
                broadcastStaffChatMessage(player, event.getMessage());
                return;
            }
        }

        // Regular chat formatting
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) {
            return;
        }

        QueryOptions queryOptions = luckPerms.getContextManager().getQueryOptions(user).orElse(null);
        String prefix = user.getCachedData().getMetaData(queryOptions).getPrefix();
        if (prefix == null) {
            prefix = "";
        }

        String customName = convertToCustomFont(player.getName());
        String displayName = ChatColor.WHITE + customName;

        String messageColor = player.hasPermission("aeternum.whitechat") ? ChatColor.WHITE.toString() : ChatColor.GRAY.toString();
        String finalMessage = String.format("%s%s%s: %s%s", translateHexColorCodes(prefix), displayName, ChatColor.RESET, messageColor, event.getMessage());

        event.setCancelled(true);
        Bukkit.broadcastMessage(finalMessage);
    }

    private void broadcastStaffChatMessage(Player player, String message) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return;
        }

        QueryOptions queryOptions = luckPerms.getContextManager().getQueryOptions(user).orElse(null);
        String prefix = user.getCachedData().getMetaData(queryOptions).getPrefix();
        if (prefix == null) {
            prefix = "";
        }

        String customName = convertToCustomFont(player.getName());
        String displayName = ChatColor.WHITE + customName;

        String staffMessage = String.format("%s%s %s%s%s: %s%s", ChatColor.BLUE, "StaffChat", translateHexColorCodes(prefix), displayName, ChatColor.RESET, ChatColor.WHITE, message);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("staff.chat")) {
                onlinePlayer.sendMessage(staffMessage);
            }
        }
    }

    private String convertToCustomFont(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toLowerCase().toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                result.append(customFont[c - 'a']);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private String translateHexColorCodes(String message) {
        // Convert hex color codes (&#AABBCC) to Minecraft's internal format (§x§A§B§B§C§C)
        message = message.replaceAll("(?i)&#([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])",
                "§x§$1§$2§$3§$4§$5§$6");
        // Convert standard color codes (&1, &f, etc.) to Minecraft's internal format (§1, §f, etc.)
        message = message.replaceAll("&([0-9a-fA-Fk-oK-OrR])", "§$1");
        return message;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (args.length == 0) {
            boolean newToggleState = !staffChatToggle.getOrDefault(playerId, false);
            staffChatToggle.put(playerId, newToggleState);
            player.sendMessage(ChatColor.GREEN + "Staff Chat toggled " + (newToggleState ? "On" : "Off"));
            return true;
        }

        if (args.length >= 1) {
            if (!player.hasPermission("staff.chat")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
            String message = String.join(" ", args);
            broadcastStaffChatMessage(player, message);
            return true;
        }

        player.sendMessage(ChatColor.RED + "Invalid usage. Use /staffchat [message] or just /staffchat to toggle.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Stream.of("on", "off").filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        }
        return null;
    }
}
