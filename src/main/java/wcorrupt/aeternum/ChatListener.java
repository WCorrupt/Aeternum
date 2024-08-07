package wcorrupt.aeternum;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatListener implements Listener {

    private final JavaPlugin plugin;
    private final LuckPerms luckPerms;
    private final String[] customFont;

    public ChatListener(JavaPlugin plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
        this.customFont = new String[]{"ᴀ", "ʙ", "ᴄ", "ᴅ", "ᴇ", "ꜰ", "ɢ", "ʜ", "ɪ", "ᴊ", "ᴋ", "ʟ", "ᴍ", "ɴ", "ᴏ", "ᴘ", "ǫ", "ʀ", "ꜱ", "ᴛ", "ᴜ", "ᴠ", "ᴡ", "x", "ʏ", "ᴢ"};
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        User user = luckPerms.getUserManager().getUser(event.getPlayer().getUniqueId());
        if (user == null) {
            return;
        }

        QueryOptions queryOptions = luckPerms.getContextManager().getQueryOptions(user).orElse(null);
        String prefix = user.getCachedData().getMetaData(queryOptions).getPrefix();
        if (prefix == null) {
            prefix = "";
        }

        prefix = sanitize(prefix);

        String customName = convertToCustomFont(event.getPlayer().getName());
        String displayName = ChatColor.WHITE + customName;

        String messageColor = event.getPlayer().hasPermission("aeternum.whitechat") ? ChatColor.WHITE.toString() : ChatColor.GRAY.toString();
        String messageFormat = String.format("%s%s%s: %s%s", translateHexColorCodes(prefix), displayName, ChatColor.RESET, messageColor, event.getMessage());

        event.setFormat(messageFormat);
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
        message = message.replaceAll("(?i)&#([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])",
                "§x§$1§$2§$3§$4§$5§$6");
        message = message.replaceAll("&([0-9a-fA-Fk-oK-OrR])", "§$1");
        return message;
    }

    private String sanitize(String input) {
        return input.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "").replaceAll("[\\x00-\\x1F\\x7F]", "");
    }
}
