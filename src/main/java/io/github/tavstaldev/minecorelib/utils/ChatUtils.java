package io.github.tavstaldev.minecorelib.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ChatUtils {
    /**
     * Builds a Component message with buttons by replacing placeholders with the provided parameters.
     *
     * @param message    The raw message containing placeholders.
     * @param parameters The dictionary containing placeholder keys and their corresponding Component values.
     * @return The Component message with buttons.
     */
    public static Component buildWithButtons(String message, Map<String, Component> parameters) {
        Component result = translateColors(message, true);
        // Get the keys
        var keys = parameters.keySet();
        for (@RegExp var dirKey : keys) {
            Component dirElem = parameters.get(dirKey);
            @RegExp String key;
            if (dirKey.startsWith("%"))
                key = dirKey;
            else
                key = "%" + dirKey + "%";
            if (!message.contains(key))
                continue;

            result = result.replaceText(TextReplacementConfig.builder()
                    .match(key)
                    .replacement(dirElem)
                    .build());
        }
        return result.decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Translates color codes using MiniMessage format.
     *
     * @param message The raw message using MiniMessage syntax.
     * @return The translated Component message.
     */
    public static Component translateColors(@NotNull String message, boolean checkLegacy) {
        if (!checkLegacy)
            return MiniMessage.miniMessage().deserialize(message).decoration(TextDecoration.ITALIC, false);

        // Convert '&' to '§' first (since ChatColor.stripColor requires '§')
        String legacyColor = translateAlternateColorCodes(message);
        return MiniMessage.miniMessage().deserialize(legacyToMiniMessage(legacyColor)).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Translates alternate color codes ('&' followed by a color code character) in a given string to the Minecraft color code character '§'.
     *
     * @param textToTranslate The string containing the alternate color codes to be translated.
     * @return The translated string with Minecraft color codes.
     */
    private static @NotNull String translateAlternateColorCodes(@NotNull String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    /**
     * Converts legacy '§' codes to MiniMessage tags.
     */
    private static String legacyToMiniMessage(String message) {
        return message
                .replace("§0", "<black>")
                .replace("§1", "<dark_blue>")
                .replace("§2", "<dark_green>")
                .replace("§3", "<dark_aqua>")
                .replace("§4", "<dark_red>")
                .replace("§5", "<dark_purple>")
                .replace("§6", "<gold>")
                .replace("§7", "<gray>")
                .replace("§8", "<dark_gray>")
                .replace("§9", "<blue>")
                .replace("§a", "<green>")
                .replace("§b", "<aqua>")
                .replace("§c", "<red>")
                .replace("§d", "<light_purple>")
                .replace("§e", "<yellow>")
                .replace("§f", "<white>")
                .replace("§l", "<bold>")
                .replace("§o", "<italic>")
                .replace("§n", "<underlined>")
                .replace("§m", "<strikethrough>")
                .replace("§k", "<obfuscated>")
                .replace("§r", "<reset>");
    }
}
