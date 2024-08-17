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
            return; // No user data available
        }

        // Fetch the prefix if available
        QueryOptions queryOptions = luckPerms.getContextManager().getQueryOptions(user).orElse(null);
        String prefix = user.getCachedData().getMetaData(queryOptions).getPrefix();
        if (prefix == null) {
            prefix = ""; // No prefix set, default to empty
        }

        // Convert display name to custom font and set color to grey
        String customName = convertToCustomFont(event.getPlayer().getName());
        String displayName = ChatColor.WHITE + customName;

        // Determine message color based on permission
        String messageColor = event.getPlayer().hasPermission("aeternum.whitechat") ? ChatColor.WHITE.toString() : ChatColor.GRAY.toString();

        // Apply word filtering
        String message = filterMessage(event.getMessage());

        String messageFormat = String.format("%s%s%s: %s%s", translateHexColorCodes(prefix), displayName, ChatColor.RESET, messageColor, message);

        // Set formatted message
        event.setFormat(messageFormat);
    }

    private String convertToCustomFont(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toLowerCase().toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                result.append(customFont[c - 'a']);
            } else {
                result.append(c); // Keep non-alphabet characters unchanged
            }
        }
        return result.toString();
    }

    private String filterMessage(String message) {
        // Replace the list of bad words with §7《§4§lBAD WORD§7》§r
        String[] badWords = {"retard", "fuck", "faggot", "twat", "piss", "shit", "bitch", "slut", "pussy", "anal", "dick", "cum", "cock", "wanker", "nigga", "niggas", "cunt", "niggers", "ngga", "ngger", "n1gger", "n1gg3r", "n1iggers", "n1gg3rs", "kys", "KYS", "penis", "niggwe", "nigger"};
        for (String word : badWords) {
            message = message.replaceAll("(?i)" + word, "§7《§4§lBAD WORD§7》§r");
        }

        // Replace the list of spaced out bad words with §7《§4§lBAD WORD§7》§r
        String[] spacedBadWords = {"fu ck", "fuc k", "f u c k", "f uck", "f uc k", "k y s", "p e n i s", "n i g g w e", "f u ck"};
        for (String word : spacedBadWords) {
            message = message.replaceAll("(?i)" + word, "§7《§4§lBAD WORD§7》§r");
        }

        // Replace the list of spaced out and altered bad words with §7《§4§lBADWORD§7》§r
        String[] otherBadWords = {"sh it", "s h i t", "shi t", "s h it", "s hit", "sh i t", "shite", "b i t c h", "bitc h", "bit ch", "b itch", "bit c h", "b i t ch"};
        for (String word : otherBadWords) {
            message = message.replaceAll("(?i)" + word, "§7《§4§lBADWORD§7》§r");
        }

        return message;
    }

    private String translateHexColorCodes(String message) {
        // Convert hex color codes (&#AABBCC) to Minecraft's internal format (§x§A§B§B§C§C)
        message = message.replaceAll("(?i)&#([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])",
                "§x§$1§$2§$3§$4§$5§$6");
        // Convert standard color codes (&1, &f, etc.) to Minecraft's internal format (§1, §f, etc.)
        message = message.replaceAll("&([0-9a-fA-Fk-oK-OrR])", "§$1");
        return message;
    }
}
