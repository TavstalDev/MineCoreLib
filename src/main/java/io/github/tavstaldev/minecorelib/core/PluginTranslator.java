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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for handling localization using YAML files.
 */
// TODO: Simplify the code by removing duplicate code blocks
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
        String rawVersion = localize(localValue, "FileVersion");
        if (rawVersion != null && !rawVersion.isEmpty()) {
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
                String rawResourceVersion = localize(resourceYaml, "FileVersion");
                if (rawResourceVersion != null && !rawResourceVersion.isEmpty()) {
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
            return "eng";
        }
    }

    /**
     * Localizes a given key to its corresponding value from the provided locale list.
     *
     * @param localeList The map containing localization data.
     * @param key The key to be localized.
     * @return The localized string, or an empty string if the key is not found or an error occurs.
     */
   private String localize(Map<String, Object> localeList, String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = localeList;
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return "";
                }
            }

            return value.toString();
        }
        catch (Exception ex)
        {
            _logger.warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.error(ex.getMessage());
            return "";
        }
    }

    /**
     * Localizes a given key to its corresponding value.
     *
     * @param key the key to be localized.
     * @return the localized string, or an empty string if the key is not found.
     */
    public String localize(String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value =_localization.get(_defaultLocale);
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return "";
                }
            }

            return value.toString();
        }
        catch (Exception ex)
        {
            _logger.warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.error(ex.getMessage());
            return "";
        }
    }

    /**
     * Localizes a given key to its corresponding list of values.
     *
     * @param key the key to be localized.
     * @return the localized list of strings, or an empty list if the key is not found.
     */
    public List<String> localizeList(String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization.get(_defaultLocale);
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return new ArrayList<>();
                }
            }

            return (List<String>)value;
        }
        catch (Exception ex)
        {
            _logger.warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.error(ex.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Localizes a given key to its corresponding array of values.
     *
     * @param key the key to be localized.
     * @return the localized array of strings, or an empty array if the key is not found.
     */
    public String[] localizeArray(String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization.get(_defaultLocale);
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return new String[0];
                }
            }

            return (String[])value;
        }
        catch (Exception ex)
        {
            _logger.warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.error(ex.getMessage());
            return new String[0];
        }
    }

    /**
     * Localizes a given key to its corresponding value and formats it with the provided arguments.
     *
     * @param key the key to be localized.
     * @param args the arguments to format the localized string.
     * @return the formatted localized string, or an empty string if the key is not found.
     */
    public String localize(String key, Map<String, Object> args) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization.get(_defaultLocale);
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return "";
                }
            }

            // Get the keys
            String result = value.toString();
            var argKeys = args.keySet();
            for (@RegExp var dirKey : argKeys) {
                @RegExp String finalKey;
                if (dirKey.startsWith("%"))
                    finalKey = dirKey;
                else
                    finalKey = "%" + dirKey + "%";
                result  = result.replace(finalKey, args.get(dirKey).toString());
            }

            return result;
        }
        catch (Exception ex)
        {
            _logger.warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.error(ex.getMessage());
            return "";
        }
    }

    /**
     * Localizes a given key to its corresponding list of values and formats it with the provided arguments.
     *
     * @param key The key to be localized.
     * @param args The arguments to format the localized strings.
     * @return A list of formatted localized strings, or an empty list if the key is not found.
     */
    public List<String> localizeList(String key, Map<String, Object> args) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization.get(_defaultLocale);
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.warn(String.format("Failed to get the '%s' translation key.", key));
                    return new ArrayList<>();
                }
            }

            var resultList = (List<String>)value;
            for (int i = 0; i < resultList.size(); i++) {
                String result = resultList.get(i);
                var argKeys = args.keySet();
                for (@RegExp var dirKey : argKeys) {
                    @RegExp String finalKey;
                    if (dirKey.startsWith("%"))
                        finalKey = dirKey;
                    else
                        finalKey = "%" + dirKey + "%";
                    result  = result.replace(finalKey, args.get(dirKey).toString());
                }
                resultList.set(i, result);
            }

            return resultList;
        }
        catch (Exception ex)
        {
            _logger.warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.error(ex.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Localizes a given key to its corresponding array of values and formats it with the provided arguments.
     *
     * @param key The key to be localized.
     * @param args The arguments to format the localized strings.
     * @return A formatted array of localized strings, or an empty array if the key is not found.
     */
    public String[] localizeArray(String key, Map<String, Object> args) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization.get(_defaultLocale);
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.warn(String.format("Failed to get the '%s' translation key.", key));
                    return new String[0];
                }
            }

            var resultList = (String[])value;
            for (int i = 0; i < resultList.length; i++) {
                String result = resultList[i];
                var argKeys = args.keySet();
                for (@RegExp var dirKey : argKeys) {
                    @RegExp String finalKey;
                    if (dirKey.startsWith("%"))
                        finalKey = dirKey;
                    else
                        finalKey = "%" + dirKey + "%";
                    result  = result.replace(finalKey, args.get(dirKey).toString());
                }
                resultList[i] = result;
            }

            return resultList;
        }
        catch (Exception ex)
        {
            _logger.warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.error(ex.getMessage());
            return new String[0];
        }
    }

    /**
     * Localizes a given key to its corresponding value for a specific player.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @return The localized string, or an empty string if the key is not found.
     */
    public String localize(Player player, String key) {
        try
        {
            String[] keys = key.split("\\.");
            String locale = getPlayerLocale(player);
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
        catch (Exception ex)
        {
            _logger.warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.error(ex.getMessage());
            return "";
        }
    }

    /**
     * Localizes a given key to its corresponding list of values for a specific player.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @return The localized list of strings, or an empty list if the key is not found.
     */
    public List<String> localizeList(Player player, String key) {
        try
        {
            String[] keys = key.split("\\.");
            String locale = getPlayerLocale(player);
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

            return (List<String>)value;
        }
        catch (Exception ex)
        {
            _logger.warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.error(ex.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Localizes a given key to its corresponding array of values for a specific player.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @return The localized array of strings, or an empty array if the key is not found.
     */
    public String[] localizeArray(Player player, String key) {
        try
        {
            String[] keys = key.split("\\.");
            String locale = getPlayerLocale(player);
            Object value = _localization.get(locale);
            if (value == null) {
                value = _localization.get(_defaultLocale);
            }
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.warn(String.format("Failed to get the '%s' translation for the '%s' translation key.", locale, key));
                    return new String[0];
                }
            }

            return (String[])value;
        }
        catch (Exception ex)
        {
            _logger.warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.error(ex.getMessage());
            return new String[0];
        }
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
        try {
            String[] keys = key.split("\\.");
            String locale = getPlayerLocale(player);
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

            // Get the keys
            String result = value.toString();
            var argKeys = args.keySet();
            for (@RegExp var dirKey : argKeys) {
                @RegExp String finalKey;
                if (dirKey.startsWith("%"))
                    finalKey = dirKey;
                else
                    finalKey = "%" + dirKey + "%";
                result  = result.replace(finalKey, args.get(dirKey).toString());
            }

            return result;
        } catch (Exception ex) {
            _logger.warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.error(ex.getMessage());
            return "";
        }

    }

    /**
     * Localizes a given key to its corresponding list of values for a specific player and formats it with the provided arguments.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @param args The arguments to format the localized strings.
     * @return A list of formatted localized strings, or an empty list if the key is not found.
     */
    public List<String> localizeList(Player player, String key, Map<String, Object> args) {
        try
        {
            String[] keys = key.split("\\.");
            String locale = getPlayerLocale(player);
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

            var resultList = (List<String>)value;
            for (int i = 0; i < resultList.size(); i++) {
                String result = resultList.get(i);
                var argKeys = args.keySet();
                for (@RegExp var dirKey : argKeys) {
                    @RegExp String finalKey;
                    if (dirKey.startsWith("%"))
                        finalKey = dirKey;
                    else
                        finalKey = "%" + dirKey + "%";
                    result  = result.replace(finalKey, args.get(dirKey).toString());
                }
                resultList.set(i, result);
            }

            return resultList;
        }
        catch (Exception ex)
        {
            _logger.warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.error(ex.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Localizes a given key to its corresponding array of values for a specific player and formats it with the provided arguments.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @param args The arguments to format the localized strings.
     * @return A formatted array of localized strings, or an empty array if the key is not found.
     */
    public String[] localizeArray(Player player, String key, Map<String, Object> args) {
        try
        {
            String[] keys = key.split("\\.");
            String locale = getPlayerLocale(player);
            Object value = _localization.get(locale);
            if (value == null) {
                value = _localization.get(_defaultLocale);
            }
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.warn(String.format("Failed to get the '%s' translation for the '%s' translation key.", locale, key));
                    return new String[0];
                }
            }

            var resultList = (String[])value;
            for (int i = 0; i < resultList.length; i++) {
                String result = resultList[i];
                var argKeys = args.keySet();
                for (@RegExp var dirKey : argKeys) {
                    @RegExp String finalKey;
                    if (dirKey.startsWith("%"))
                        finalKey = dirKey;
                    else
                        finalKey = "%" + dirKey + "%";
                    result  = result.replace(finalKey, args.get(dirKey).toString());
                }
                resultList[i] = result;
            }

            return resultList;
        }
        catch (Exception ex)
        {
            _logger.warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.error(ex.getMessage());
            return new String[0];
        }
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
