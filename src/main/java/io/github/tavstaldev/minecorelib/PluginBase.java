package io.github.tavstaldev.minecorelib;

import com.google.gson.*;
import io.github.tavstaldev.minecorelib.config.ConfigurationBase;
import io.github.tavstaldev.minecorelib.core.GuiDupeDetector;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract base class for a Minecraft plugin.
 * Provides common functionality such as logging, translation, and version checking.
 *
 */
public abstract class PluginBase extends JavaPlugin {
    protected final PluginLogger _logger;
    protected ConfigurationBase _config;
    protected PluginTranslator _translator;
    private final HttpClient _httpClient;
    private final String _downloadUrl;
    private final boolean _hasGui;

    /**
     * Constructs a new instance of the PluginBase class.
     *
     * @param hasGui      Indicates whether the plugin has a GUI.
     * @param downloadUrl The URL used for downloading updates.
     */
    public PluginBase(boolean hasGui, String downloadUrl) {
        _hasGui = hasGui;
        _downloadUrl = downloadUrl;
        _logger = new PluginLogger(this);
        _httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2) // Prefer HTTP/2 if available
                .followRedirects(HttpClient.Redirect.NORMAL) // Follow redirects
                .connectTimeout(Duration.ofSeconds(120)) // Connection timeout
                .build();
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
     * Returns the plugin's configuration instance.
     *
     * @return the {@link ConfigurationBase} object representing the plugin configuration.
     */
    public @NotNull ConfigurationBase getConfig() {
        return _config;
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
        return this.getPluginMeta().getName();
    }

    /**
     * Gets the version of the plugin.
     *
     * @return The version.
     */
    public @NotNull String getVersion() {
        return this.getPluginMeta().getVersion();
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
     * Retrieves the HTTP client used by the plugin.
     * The HTTP client is pre-configured with HTTP/2 support,
     * normal redirect following, and a connection timeout of 120 seconds.
     *
     * @return The configured {@link HttpClient} instance.
     */
    public @NotNull HttpClient getHttpClient() { return _httpClient; }

    /**
     * Called when the plugin is enabled.
     * If the plugin has a GUI, registers the plugin to the GUI dupe detector.
     */
    @Override
    public void onEnable() {
        if (_hasGui) {
            GuiDupeDetector.register(this);
        }
    }

    /**
     * Called when the plugin is disabled.
     * If the plugin has a GUI, unregisters the plugin from the GUI dupe detector.
     */
    @Override
    public void onDisable() {
        if (_hasGui) {
            GuiDupeDetector.unregister(this);
        }
    }

    /**
     * Checks if the plugin is up-to-date by comparing the current version with the latest version
     * retrieved from the specified download URL.
     * <br>
     * This method performs an asynchronous HTTP GET request to fetch the latest version information
     * in JSON format. It parses the response and compares the "tag_name" field with the current version.
     * Logs debug, warning, and error messages during the process.
     *
     * @return {@code true} if the plugin is up-to-date, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> isUpToDate() {
        _logger.debug("Checking for updates...");
        // Build the HTTP request to fetch the latest version information
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_downloadUrl))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        // Send the request asynchronously and process the response
        return _httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    // Check HTTP status code
                    if (response.statusCode() != 200) {
                        _logger.debug("GET request failed for " + _downloadUrl + ". Status: " + response.statusCode());
                        throw new RuntimeException("API returned non-200 status: " + response.statusCode());
                    }
                    return response.body();
                })
                .thenApply(jsonBody -> {
                    try {
                        _logger.debug("Parsing JSON response from " + _downloadUrl);
                        return JsonParser.parseString(jsonBody);
                    } catch (JsonSyntaxException e) {
                        _logger.debug("Failed to parse JSON response from " + _downloadUrl + ": " + e.getMessage());
                        throw new RuntimeException("JSON parsing error", e);
                    }
                })
                .thenApply(jsonElement -> {
                    if (jsonElement == null) {
                        _logger.warn("Failed to retrieve the latest version information from " + _downloadUrl);
                        return false; // Treat as not up-to-date or an error occurred
                    }

                    if (!jsonElement.isJsonObject()) {
                        _logger.warn("Expected a JSON object from " + _downloadUrl + ", but got: " + jsonElement);
                        return false; // Treat as not up-to-date or an error occurred
                    }

                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    String latestVersion = jsonObject.get("tag_name").getAsString();
                    String currentVersion = getVersion();
                    _logger.debug("Current version: " + currentVersion);
                    _logger.debug("Latest version: " + latestVersion);

                    return currentVersion.equalsIgnoreCase(latestVersion) || ("v" + currentVersion).equalsIgnoreCase(latestVersion);
                })
                .exceptionally(ex -> {
                    _logger.error("Error during update check: " + ex.getMessage());
                    /*getServer().getScheduler().runTask(this, () -> {
                        _logger.Error("An error occurred while checking for updates: " + ex.getMessage());
                    });*/
                    return false; // If an error occurs, assume not up-to-date or handle as appropriate
                });
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
        String rawMessage = _translator.localize(player, key);
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
        String rawMessage = _translator.localize(player, key);

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
     * Sends a localized command reply to the sender.
     * If the sender is a player, the message is sent using the player's locale.
     * Otherwise, the message is logged to the plugin's custom logger.
     *
     * @param sender The command sender (can be a Player or Console).
     * @param key    The localization key for the message to be sent.
     */
    public void sendCommandReply(CommandSender sender, String key) {
        if (sender instanceof Player player) {
            // Send a localized message to the player
            sendLocalizedMsg(player, key);
            return;
        }
        // Log the localized message to the console or other non-player sender
        getCustomLogger().info(localize(key));
    }

    /**
     * Sends a localized command reply to the sender with placeholders replaced by provided parameters.
     * If the sender is a player, the message is sent using the player's locale.
     * Otherwise, the message is logged to the plugin's custom logger.
     *
     * @param sender     The command sender (can be a Player or Console).
     * @param key        The localization key for the message to be sent.
     * @param parameters A map of placeholder keys and their corresponding values to replace in the message.
     */
    public void sendCommandReply(CommandSender sender, String key, Map<String, Object> parameters) {
        if (sender instanceof Player player) {
            // Send a localized message with parameters to the player
            sendLocalizedMsg(player, key, parameters);
            return;
        }
        // Log the localized message with parameters to the console or other non-player sender
        getCustomLogger().info(localize(key, parameters));
    }


    /**
     * Localizes a given key to its corresponding value.
     *
     * @param key the key to be localized.
     * @return the localized string, or an empty string if the key is not found.
     */
    public String localize(String key) {
        return getTranslator().localize(key);
    }

    /**
     * Localizes a given key to its corresponding list of values.
     *
     * @param key the key to be localized.
     * @return the localized list of strings, or an empty list if the key is not found.
     */
    public List<String> localizeList(String key) {
        return getTranslator().localizeList(key);
    }

    /**
     * Localizes a given key to its corresponding array of values.
     *
     * @param key the key to be localized.
     * @return the localized array of strings, or an empty array if the key is not found.
     */
    public String[] localizeArray(String key) {
       return getTranslator().localizeArray(key);
    }

    /**
     * Localizes a given key to its corresponding value and formats it with the provided arguments.
     *
     * @param key the key to be localized.
     * @param args the arguments to format the localized string.
     * @return the formatted localized string, or an empty string if the key is not found.
     */
    public String localize(String key, Map<String, Object> args) {
        return getTranslator().localize(key, args);
    }

    /**
     * Localizes a given key to its corresponding value for a specific player.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @return The localized string, or an empty string if the key is not found.
     */
    public String localize(Player player, String key) {
        return getTranslator().localize(player, key);
    }

    /**
     * Localizes a given key to its corresponding list of values for a specific player.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @return The localized list of strings, or an empty list if the key is not found.
     */
    public List<String> localizeList(Player player, String key) {
        return getTranslator().localizeList(player, key);
    }

    /**
     * Localizes a given key to its corresponding array of values for a specific player.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @return The localized array of strings, or an empty array if the key is not found.
     */
    public String[] localizeArray(Player player, String key) {
        return getTranslator().localizeArray(player, key);
    }

    /**
     * Localizes a given key to its corresponding value for a specific player and formats it with the provided arguments.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @param args The arguments to format the localized string.
     * @return The formatted localized string, or an empty string if the key is not found.
     */
    public String localize(Player player, String key, Map<String, Object> args) {
        return getTranslator().localize(player, key, args);
    }
}
