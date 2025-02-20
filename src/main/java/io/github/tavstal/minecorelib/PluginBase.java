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
import java.util.Map;
import java.util.Objects;

/**
 * Abstract base class for a Minecraft plugin.
 * Provides common functionality such as logging, translation, and version checking.
 *
 */
public abstract class PluginBase<T extends PluginBase<T>> extends JavaPlugin {
    public static PluginBase<?> Instance;
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
        Instance = this;
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
        _logger.LogInfo(String.format("Reloading %s...", _projectName));
        _logger.LogDebug("Reloading localizations...");
        if (_translator.Load())
            _logger.LogDebug("Localizations reloaded.");
        else
            _logger.LogError("Failed to reload localizations.");
        _logger.LogDebug("Reloading configuration...");
        this.reloadConfig();
        _logger.LogDebug("Configuration reloaded.");
    }

    /**
     * Checks if the plugin is up to date by comparing the current version with the latest release version.
     * @return true if the plugin is up to date, false otherwise.
     */
    public boolean isUpToDate() {
        String version;
        _logger.LogDebug("Checking for updates...");
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            _logger.LogDebug("Sending request to GitHub...");
            HttpGet request = new HttpGet(_downloadUrl);
            HttpResponse response = httpClient.execute(request);
            _logger.LogDebug("Received response from GitHub.");
            String jsonResponse = EntityUtils.toString(response.getEntity());
            _logger.LogDebug("Parsing response...");
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonResponse);
            _logger.LogDebug("Parsing release version...");
            version = jsonObject.get("tag_name").toString();
        } catch (IOException e) {
            _logger.LogError("Failed to check for updates.");
            return false;
        } catch (ParseException e) {
            _logger.LogError("Failed to parse release version.");
            return false;
        }

        _logger.LogDebug("Current version: " + _version);
        _logger.LogDebug("Latest version: " + version);
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
