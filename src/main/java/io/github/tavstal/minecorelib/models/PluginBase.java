package io.github.tavstal.minecorelib.models;

import io.github.tavstal.minecorelib.core.PluginLogger;
import io.github.tavstal.minecorelib.core.PluginTranslator;
import io.github.tavstal.minecorelib.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public abstract class PluginBase<T extends PluginBase<T>> extends JavaPlugin {
    public static PluginBase<?> Instance;
    private final PluginLogger _logger;
    private final PluginTranslator _translator;
    private final String _projectName;
    private final String _version;
    private final String _author;
    private final String _downloadUrl;

    public PluginBase(String projectName, String version, String author, String downloadUrl, String[] locales) {
        _projectName = projectName;
        _version = version;
        _author = author;
        _downloadUrl = downloadUrl;
        _logger = new PluginLogger(this);
        _translator = new PluginTranslator(this, locales);
        Instance = this;
    }

    public @NotNull PluginLogger getCustomLogger() {
        return _logger;
    }

    public @NotNull PluginTranslator getTranslator() {
        return _translator;
    }

    public @NotNull String getProjectName() {
        return _projectName;
    }

    public @NotNull String getVersion() {
        return _version;
    }

    public @NotNull String getAuthor() {
        return _author;
    }

    public @NotNull String getDownloadUrl() {
        return _downloadUrl;
    }

    /**
     * Replaces placeholders in the given message with actual values.
     *
     * @param message The message containing placeholders to be replaced.
     * @return The message with placeholders replaced by actual values.
     */
    protected String replacePlaceholders(String message) {
        return message.replace("%prefix%", Objects.requireNonNull(getConfig().getString("prefix")));
    }

    /**
     * Sends a colored message to a player.
     *
     * @param player  The player to send the message to.
     * @param message The raw message containing '&' color codes.
     */
    public void sendRichMsg(Player player, String message) {
        player.sendMessage(ChatUtils.translateColors(message, true));
    }

    /**
     * Retrieves a localized message, translates color codes, and sends it to a player.
     *
     * @param player     The player to send the message to.
     * @param key        The localization key.
     */
    public void sendLocalizedMsg(Player player, String key) {
        String rawMessage = _translator.Localize(player, key);
        sendRichMsg(player, rawMessage);
    }

    /**
     * Retrieves a localized message, replaces placeholders with the provided parameters, translates color codes, and sends it to a player.
     *
     * @param player     The player to send the message to.
     * @param key        The localization key.
     * @param parameters The dictionary containing placeholder keys and their corresponding values.
     */
    public void sendLocalizedMsg(Player player, String key, Map<String, Object> parameters) {
        String rawMessage = _translator.Localize(player, key);

        // Get the keys
        var keys = parameters.keySet();
        for (@RegExp var dirKey : keys) {
            @RegExp String finalKey;
            if (dirKey.startsWith("%"))
                finalKey = dirKey;
            else
                finalKey = "%" + dirKey + "%";
            rawMessage = rawMessage.replace(finalKey, parameters.get(dirKey).toString());
        }

        sendRichMsg(player, rawMessage);
    }
}
