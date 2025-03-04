package io.github.tavstal.minecorelib;

import io.github.tavstal.minecorelib.core.PluginLogger;
import io.github.tavstal.minecorelib.core.PluginTranslator;
import io.github.tavstal.minecorelib.utils.ChatUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract base class for a Minecraft plugin.
 * Provides common functionality such as logging, translation, and version checking.
 *
 */
public abstract class PluginBase extends JavaPlugin {
    private final PluginLogger _logger;
    private final PluginTranslator _translator;
    private final String _projectName;
    private final String _version;
    private final String _author;
    private final String _downloadUrl;

    /**
     * Constructs a new PluginBase instance.
     *
     * @param projectName The name of the project.
     * @param version The version of the plugin.
     * @param author The author of the plugin.
     * @param downloadUrl The URL to check for updates.
     * @param locales The supported locales for translation.
     */
    public PluginBase(String projectName, String version, String author, String downloadUrl, String[] locales) {
        _projectName = projectName;
        _version = version;
        _author = author;
        _downloadUrl = downloadUrl;
        _logger = new PluginLogger(this);
        _translator = new PluginTranslator(this, locales);
    }

    /**
     * Gets the custom logger for the plugin.
     *
     * @return The custom logger.
     */
    public @NotNull PluginLogger getCustomLogger() {
        return _logger;
    }

    /**
     * Gets the translator for the plugin.
     *
     * @return The translator.
     */
    public @NotNull PluginTranslator getTranslator() {
        return _translator;
    }

    /**
     * Gets the project name.
     *
     * @return The project name.
     */
    public @NotNull String getProjectName() {
        return _projectName;
    }

    /**
     * Gets the version of the plugin.
     *
     * @return The version.
     */
    public @NotNull String getVersion() {
        return _version;
    }

    /**
     * Gets the author of the plugin.
     *
     * @return The author.
     */
    public @NotNull String getAuthor() {
        return _author;
    }

    /**
     * Gets the download URL for checking updates.
     *
     * @return The download URL.
     */
    public @NotNull String getDownloadUrl() {
        return _downloadUrl;
    }

    /**
     * Reloads the plugin configuration and localizations.
     */
    public void reload() {
        _logger.Info(String.format("Reloading %s...", _projectName));
        _logger.Debug("Reloading localizations...");
        if (_translator.Load())
            _logger.Debug("Localizations reloaded.");
        else
            _logger.Error("Failed to reload localizations.");
        _logger.Debug("Reloading configuration...");
        this.reloadConfig();
        _logger.Debug("Configuration reloaded.");
    }

    /**
     * Checks if the plugin is up to date by comparing the current version with the latest release version.
     * @return true if the plugin is up to date, false otherwise.
     */
    public boolean isUpToDate() {
        String version;
        _logger.Debug("Checking for updates...");
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            _logger.Debug("Sending request to GitHub...");
            HttpGet request = new HttpGet(_downloadUrl);
            HttpResponse response = httpClient.execute(request);
            _logger.Debug("Received response from GitHub.");
            String jsonResponse = EntityUtils.toString(response.getEntity());
            _logger.Debug("Parsing response...");
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonResponse);
            _logger.Debug("Parsing release version...");
            version = jsonObject.get("tag_name").toString();
        } catch (IOException e) {
            _logger.Error("Failed to check for updates.");
            return false;
        } catch (ParseException e) {
            _logger.Error("Failed to parse release version.");
            return false;
        }

        _logger.Debug("Current version: " + _version);
        _logger.Debug("Latest version: " + version);
        return version.equalsIgnoreCase(_version);
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
        player.sendMessage(ChatUtils.translateColors(replacePlaceholders(message), true));
    }

    /**
     * Sends a colored message to a player with placeholders replaced by actual values from the provided parameters.
     *
     * @param player     The player to send the message to.
     * @param message    The raw message containing '&' color codes and placeholders.
     * @param parameters The dictionary containing placeholder keys and their corresponding values.
     */
    public void sendRichMsg(Player player, String message, Map<String, Object> parameters) {
        String rawMessage = message;

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

        sendRichMsg(player, replacePlaceholders(rawMessage));
    }

    /**
     * Retrieves a localized message, translates color codes, and sends it to a player.
     *
     * @param player     The player to send the message to.
     * @param key        The localization key.
     */
    public void sendLocalizedMsg(Player player, String key) {
        String rawMessage = _translator.Localize(player, key);
        sendRichMsg(player, replacePlaceholders(rawMessage));
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

        sendRichMsg(player, replacePlaceholders(rawMessage));
    }

    /**
     * Localizes a given key to its corresponding value.
     *
     * @param key the key to be localized.
     * @return the localized string, or an empty string if the key is not found.
     */
    public String Localize(String key) {
        return getTranslator().Localize(key);
    }

    /**
     * Localizes a given key to its corresponding list of values.
     *
     * @param key the key to be localized.
     * @return the localized list of strings, or an empty list if the key is not found.
     */
    public List<String> LocalizeList(String key) {
        return getTranslator().LocalizeList(key);
    }

    /**
     * Localizes a given key to its corresponding array of values.
     *
     * @param key the key to be localized.
     * @return the localized array of strings, or an empty array if the key is not found.
     */
    public String[] LocalizeArray(String key) {
       return getTranslator().LocalizeArray(key);
    }

    /**
     * Localizes a given key to its corresponding value and formats it with the provided arguments.
     *
     * @param key the key to be localized.
     * @param args the arguments to format the localized string.
     * @return the formatted localized string, or an empty string if the key is not found.
     */
    public String Localize(String key, Map<String, Object> args) {
        return getTranslator().Localize(key, args);
    }

    /**
     * Localizes a given key to its corresponding value for a specific player.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @return The localized string, or an empty string if the key is not found.
     */
    public String Localize(Player player, String key) {
        return getTranslator().Localize(player, key);
    }

    /**
     * Localizes a given key to its corresponding list of values for a specific player.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @return The localized list of strings, or an empty list if the key is not found.
     */
    public List<String> LocalizeList(Player player, String key) {
        return getTranslator().LocalizeList(player, key);
    }

    /**
     * Localizes a given key to its corresponding array of values for a specific player.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @return The localized array of strings, or an empty array if the key is not found.
     */
    public String[] LocalizeArray(Player player,String key) {
        return getTranslator().LocalizeArray(player, key);
    }

    /**
     * Localizes a given key to its corresponding value for a specific player and formats it with the provided arguments.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @param args The arguments to format the localized string.
     * @return The formatted localized string, or an empty string if the key is not found.
     */
    public String Localize(Player player,String key, Map<String, Object> args) {
        return getTranslator().Localize(player, key, args);
    }
}
