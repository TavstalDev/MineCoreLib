package io.github.tavstaldev.minecorelib.core;

import io.github.tavstaldev.minecorelib.PluginBase;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Utility class for handling localization using YAML files.
 */
public class PluginTranslator {
    private final PluginBase _plugin;
    private final PluginLogger _logger;
    private final String[] _locales;
    private String _defaultLocale = "eng";
    private Map<String, Map<String, Object>> _localization;

    /**
     * Constructs a new PluginTranslator instance.
     *
     * @param plugin The plugin instance associated with this translator.
     * @param locales The array of supported locales for translation.
     */
    public PluginTranslator(PluginBase plugin, String[] locales) {
        _plugin = plugin;
        _locales = locales;
        _logger = plugin.getCustomLogger();
    }

    /**
     * Loads the localization file based on the locale specified in the plugin's config.
     *
     * @return true if the localization file was successfully loaded, false otherwise.
     */
    public Boolean load() {
        InputStream inputStream;
        _localization = new HashMap<>();
        _defaultLocale = _plugin.getConfig().getString("locale");

        _logger.debug("Checking lang directory...");
        Path dirPath = Paths.get(_plugin.getDataFolder().getPath(), "lang");
        if (!Files.exists(dirPath) || isDirectoryEmpty(dirPath)) {
            try {
                _logger.debug("Creating lang directory...");
                Files.createDirectory(dirPath);

                for (String locale : _locales) {
                    _logger.debug("Creating lang file...");
                    Path filePath = Paths.get(dirPath.toString(), locale + ".yml");
                    if (Files.exists(filePath))
                        continue;

                    try {
                        inputStream = _plugin.getResource("lang/" + locale + ".yml");
                        if (inputStream == null) {
                            _logger.debug(String.format("Failed to get localization file for locale '%s'.", locale));
                        } else
                            Files.copy(inputStream, filePath);
                    } catch (IOException ex) {
                        _logger.warn(String.format("Failed to create lang file for locale '%s'.", locale));
                        _logger.error(ex.getMessage());
                    }
                }
            } catch (IOException ex) {
                _logger.warn("Failed to create lang directory.");
                _logger.error(ex.getMessage());
                return false;
            }
        }

        _logger.debug("Reading lang directory...");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            _logger.debug("Reading lang files...");
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString();
                _logger.debug("Reading file: " + fileName);
                if (!(fileName.endsWith(".yml") || fileName.endsWith(".yaml")))
                    continue;

                try
                {
                    inputStream = new FileInputStream(entry.toFile());
                }
                catch (FileNotFoundException ex)
                {
                    _logger.error(String.format("Failed to get localization file. Path: %s", entry));
                    continue;
                }
                catch (Exception ex)
                {
                    _logger.warn("Unknown error happened while reading locale file.");
                    _logger.error(ex.getMessage());
                    continue;
                }

                _logger.debug("Loading yaml file...");
                DumperOptions dumperOptions = new DumperOptions();
                dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // Forces multi-line formatting
                dumperOptions.setIndent(2);
                LoaderOptions loaderOptions = new LoaderOptions();
                loaderOptions.setMaxAliasesForCollections(200); // Should be enough
                Constructor constructor = new Constructor(loaderOptions);
                Representer representer = new Representer(dumperOptions);
                Yaml yaml = new Yaml(constructor, representer);
                Object yamlObject = yaml.load(inputStream);
                if (!(yamlObject instanceof Map))
                {
                    _logger.error("Failed to cast the yamlObject after reading the localization.");
                    continue;
                }

                _logger.debug("Casting yamlObject to Map...");
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> localValue = (Map<String, Object>) yamlObject;
                    String lang = fileName.split("\\.")[0];
                    _localization.put(lang, localValue); // Warning fix
                    // TODO: Test it
                    Map<String, Object> updatedLocale = updateLocalization(lang);
                    if (updatedLocale == null)
                        continue;

                    _localization.put(lang, updatedLocale);
                    Path filePath = Paths.get(dirPath.toString(), lang + ".yml");
                    try (FileWriter writer = new FileWriter(filePath.toString())) {
                        yaml.dump(updatedLocale, writer);
                    }
                } catch (Exception ex) {
                    _logger.warn("Failed to cast the yamlObject to Map.");
                    _logger.error(ex.getMessage());
                }
            }
        } catch (IOException ex) {
            _logger.warn("Failed to read the lang directory.");
            _logger.error(ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Updates the localization for a given language.
     *
     * @param lang The language code for which the localization should be updated.
     * @return A map containing the updated localization values, or null if an error occurs.
     */
    private Map<String, Object> updateLocalization(@NotNull String lang) {
        if (!_localization.containsKey(lang))
            return null;
        Map<String, Object> localValue = _localization.get(lang);
        if (localValue == null || localValue.isEmpty())
            return null;

        int fileVersion = 0;
        String rawVersion = getVersion(localValue);
        if (!rawVersion.isEmpty()) {
            try {
                fileVersion = Integer.parseInt(rawVersion);
            } catch (NumberFormatException ex) {
                _logger.warn("Failed to parse the file version.");
                _logger.error(ex.getMessage());
                return null;
            }
        }

        InputStream inputStream;
        try {
            inputStream = _plugin.getResource("lang/" + lang + ".yml");
            if (inputStream == null) {
                _logger.debug(String.format("Failed to get localization file for locale '%s'.", lang));
                return null;
            }

            _logger.debug("Loading yaml file...");
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // Forces multi-line formatting
            options.setIndent(2);
            Yaml yaml = new Yaml(options);
            Object yamlObject = yaml.load(inputStream);
            if (!(yamlObject instanceof Map))
            {
                _logger.error("Failed to cast the yamlObject after reading the localization.");
                return null;
            }

            _logger.debug("Casting yamlObject to Map...");
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> resourceYaml = (Map<String, Object>) yamlObject;
                int resourceVersion = 0;
                String rawResourceVersion = getVersion(resourceYaml);
                if (!rawResourceVersion.isEmpty()) {
                    try {
                        resourceVersion = Integer.parseInt(rawResourceVersion);
                    } catch (NumberFormatException ex) {
                        _logger.warn("Failed to parse the file version.");
                        _logger.error(ex.getMessage());
                        return null;
                    }
                }

                if (resourceVersion <= fileVersion)
                    return null;

                for (String key : localValue.keySet()) {
                    if (key.equals("FileVersion") || key.equals("file-version"))
                        continue;

                    if (!resourceYaml.containsKey(key))
                        continue;

                    resourceYaml.put(key, localValue.get(key));
                }
                return resourceYaml;
            } catch (Exception ex) {
                _logger.warn("Failed to cast the yamlObject to Map.");
                _logger.error(ex.getMessage());
            }
        } catch (Exception ex) {
            _logger.warn(String.format("Failed to create lang file for locale '%s'.", lang));
            _logger.error(ex.getMessage());
        }
        return null;
    }

    /**
     * Retrieves the player's locale in ISO 639-3 language code format.
     *
     * @param player The player whose locale is to be retrieved.
     * @return The ISO 639-3 language code of the player's locale, or "en" if an error occurs.
     */
    private @NotNull String getPlayerLocale(@NotNull Player player) {
        try {
            if (!_plugin.getConfig().getBoolean("usePlayerLocale"))
                return _defaultLocale;
            return player.locale().getISO3Language();
        }
        catch (Exception ex) {
            _logger.warn("Failed to get the player's locale.");
            _logger.error(ex.getMessage());
            return _defaultLocale;
        }
    }

    /**
     * Retrieves a localized string for the default locale.
     *
     * @param key The key for the localization string.
     * @return The localized string.
     */
    public @NotNull String localize(@NotNull String key) {
        return getLocalization(_defaultLocale, key);
    }

    /**
     * Retrieves a localized string for the default locale and replaces placeholders with arguments.
     *
     * @param key The key for the localization string.
     * @param args The arguments to replace placeholders in the string.
     * @return The localized string with placeholders replaced.
     */
    public @NotNull String localize(@NotNull String key, @NotNull Map<String, Object> args) {
        String result = getLocalization(_defaultLocale, key);
        if (result.isEmpty())
            return "";
        return replaceArgs(result, args);
    }

    /**
     * Retrieves a localized string for a specific player.
     *
     * @param player The player whose locale is used.
     * @param key The key for the localization string.
     * @return The localized string.
     */
    public @NotNull String localize(@NotNull Player player, @NotNull String key) {
        String locale = getPlayerLocale(player);
        if (!_localization.containsKey(locale))
            return getLocalization(_defaultLocale, key);
        return getLocalization(locale, key);
    }

    /**
     * Retrieves a localized string for a specific player and replaces placeholders with arguments.
     *
     * @param player The player whose locale is used.
     * @param key The key for the localization string.
     * @param args The arguments to replace placeholders in the string.
     * @return The localized string with placeholders replaced.
     */
    public @NotNull String localize(@NotNull Player player, @NotNull String key, @NotNull Map<String, Object> args) {
        String result = localize(player, key);
        if (result.isEmpty())
            return "";
        return replaceArgs(result, args);
    }

    /**
     * Retrieves a localized list of strings for the default locale.
     *
     * @param key The key for the localization list.
     * @return The localized list of strings.
     */
    public @NotNull List<String> localizeList(@NotNull String key) {
        return getLocalizationList(_defaultLocale, key);
    }

    /**
     * Retrieves a localized list of strings for the default locale and replaces placeholders with arguments.
     *
     * @param key The key for the localization list.
     * @param args The arguments to replace placeholders in the list.
     * @return The localized list of strings with placeholders replaced.
     */
    public @NotNull List<String> localizeList(@NotNull String key, @NotNull Map<String, Object> args) {
        List<String> resultList = getLocalizationList(_defaultLocale, key);
        if (resultList.isEmpty())
            return new ArrayList<>();
        return replaceArgs(resultList, args);
    }

    /**
     * Retrieves a localized list of strings for a specific player.
     *
     * @param player The player whose locale is used.
     * @param key The key for the localization list.
     * @return The localized list of strings.
     */
    public @NotNull List<String> localizeList(@NotNull Player player, @NotNull String key) {
        String locale = getPlayerLocale(player);
        if (!_localization.containsKey(locale))
            return getLocalizationList(_defaultLocale, key);
        return getLocalizationList(locale, key);
    }

    /**
     * Retrieves a localized list of strings for a specific player and replaces placeholders with arguments.
     *
     * @param player The player whose locale is used.
     * @param key The key for the localization list.
     * @param args The arguments to replace placeholders in the list.
     * @return The localized list of strings with placeholders replaced.
     */
    public @NotNull List<String> localizeList(@NotNull Player player, @NotNull String key, @NotNull Map<String, Object> args) {
        List<String> resultList = localizeList(player, key);
        if (resultList.isEmpty())
            return new ArrayList<>();
        return replaceArgs(resultList, args);
    }

    /**
     * Retrieves the version of the localization file from the map.
     *
     * @param localeMap The map containing localization data.
     * @return The version of the localization file as a string.
     */
    private @NotNull String getVersion(Map<String, Object> localeMap) {
        try {
            if (!localeMap.containsKey("FileVersion"))
                return "1";
            return localeMap.get("FileVersion").toString();
        }
        catch (Exception ex) {
            return "1";
        }
    }

    /**
     * Retrieves a localized string for a specific locale and key.
     *
     * @param locale The locale to use.
     * @param key The key for the localization string.
     * @return The localized string.
     */
    private @NotNull String getLocalization(@NotNull String locale, @NotNull String key) {
        try {
            String[] keys = key.split("\\.");
            Object value = _localization.get(locale);
            if (value == null) {
                value = _localization.get(_defaultLocale);
            }
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.warn(String.format("Failed to get the '%s' translation for the '%s' translation key.", locale, key));
                    return "";
                }
            }

            return value.toString();
        }
        catch (Exception ex) {
            _logger.warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.error(ex.getMessage());
            return "";
        }
    }

    /**
     * Retrieves a localized list of strings for a specific locale and key.
     *
     * @param locale The locale to use.
     * @param key The key for the localization list.
     * @return The localized list of strings.
     */
    private @NotNull List<String> getLocalizationList(@NotNull String locale, @NotNull String key) {
        try {
            String[] keys = key.split("\\.");
            Object value = _localization.get(locale);
            if (value == null) {
                value = _localization.get(_defaultLocale);
            }
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.warn(String.format("Failed to get the '%s' translation for the '%s' translation key.", locale, key));
                    return new ArrayList<>();
                }
            }

            if (value instanceof List<?>) {
                return new ArrayList<>((List<String>) value);
            } else {
                _logger.warn(String.format("The value for key '%s' is not a list.", key));
                return new ArrayList<>();
            }
        }
        catch (Exception ex) {
            _logger.warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.error(ex.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Replaces placeholders in a string with the provided arguments.
     *
     * @param text The string containing placeholders.
     * @param args The arguments to replace placeholders.
     * @return The string with placeholders replaced.
     */
    private @NotNull String replaceArgs(@NotNull String text, @NotNull Map<String, Object> args) {
        String result = text;
        Set<String> argKeys = args.keySet();
        for (@RegExp String dirKey : argKeys) {
            @RegExp String finalKey;
            if (dirKey.startsWith("%"))
                finalKey = dirKey;
            else
                finalKey = "%" + dirKey + "%";
            result = result.replace(finalKey, args.get(dirKey).toString());
        }
        return result;
    }

    /**
     * Replaces placeholders in a list of strings with the provided arguments.
     *
     * @param texts The list of strings containing placeholders.
     * @param args The arguments to replace placeholders.
     * @return The list of strings with placeholders replaced.
     */
    private @NotNull List<String> replaceArgs(@NotNull List<String> texts, @NotNull Map<String, Object> args) {
        List<String> resultList = new ArrayList<>();
        for (String text : texts) {
            resultList.add(replaceArgs(text, args));
        }
        return resultList;
    }

    /**
     * Checks if a directory is empty.
     *
     * @param dirPath The path to the directory.
     * @return true if the directory is empty, false otherwise.
     */
    private boolean isDirectoryEmpty(Path dirPath){
        if (Files.notExists(dirPath)) {
            return true;
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dirPath)) {
            return !directoryStream.iterator().hasNext();
        } catch (IOException ex) {
            _logger.error("Failed to check if directory is empty: ");
            _logger.error(ex.getMessage());
            return false;
        }
    }
}