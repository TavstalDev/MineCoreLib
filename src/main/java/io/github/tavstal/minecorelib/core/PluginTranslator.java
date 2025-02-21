package io.github.tavstal.minecorelib.core;

import io.github.tavstal.minecorelib.PluginBase;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Boolean Load() {
        InputStream inputStream;
        _localization = new HashMap<>();
        _defaultLocale = _plugin.getConfig().getString("locale");

        _logger.Debug("Checking lang directory...");
        Path dirPath = Paths.get(_plugin.getDataFolder().getPath(), "lang");
        if (!Files.exists(dirPath) || isDirectoryEmpty(dirPath)) {
            try {
                _logger.Debug("Creating lang directory...");
                Files.createDirectory(dirPath);

                for (String locale : _locales) {
                    _logger.Debug("Creating lang file...");
                    Path filePath = Paths.get(dirPath.toString(), locale + ".yml");
                    if (Files.exists(filePath))
                        continue;

                    try {
                        inputStream = _plugin.getResource("lang/" + locale + ".yml");
                        if (inputStream == null) {
                            _logger.Debug(String.format("Failed to get localization file for locale '%s'.", locale));
                        } else
                            Files.copy(inputStream, filePath);
                    } catch (IOException ex) {
                        _logger.Warn(String.format("Failed to create lang file for locale '%s'.", locale));
                        _logger.Error(ex.getMessage());
                        return false;
                    }
                }
            } catch (IOException ex) {
                _logger.Warn("Failed to create lang directory.");
                _logger.Error(ex.getMessage());
                return false;
            }
        }

        _logger.Debug("Reading lang directory...");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            _logger.Debug("Reading lang files...");
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString();
                _logger.Debug("Reading file: " + fileName);
                if (!(fileName.endsWith(".yml") || fileName.endsWith(".yaml")))
                    continue;

                try
                {
                    inputStream = new FileInputStream(entry.toFile());
                }
                catch (FileNotFoundException ex)
                {
                    _logger.Error(String.format("Failed to get localization file. Path: %s", entry));
                    return false;
                }
                catch (Exception ex)
                {
                    _logger.Warn("Unknown error happened while reading locale file.");
                    _logger.Error(ex.getMessage());
                    return false;
                }

                _logger.Debug("Loading yaml file...");
                Yaml yaml = new Yaml();
                Object yamlObject = yaml.load(inputStream);
                if (!(yamlObject instanceof Map))
                {
                    _logger.Error("Failed to cast the yamlObject after reading the localization.");
                    return false;
                }

                _logger.Debug("Casting yamlObject to Map...");
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> localValue = (Map<String, Object>) yamlObject;
                    _localization.put(fileName.split("\\.")[0], localValue); // Warning fix
                } catch (Exception ex) {
                    _logger.Warn("Failed to cast the yamlObject to Map.");
                    _logger.Error(ex.getMessage());
                }
            }
        } catch (IOException ex) {
            _logger.Warn("Failed to read the lang directory.");
            _logger.Error(ex.getMessage());
            return false;
        }
        return true;
    }


    /**
     * Retrieves the player's locale in ISO 639-3 language code format.
     *
     * @param player The player whose locale is to be retrieved.
     * @return The ISO 639-3 language code of the player's locale, or "en" if an error occurs.
     */
    private @NotNull String GetPlayerLocale(@NotNull Player player) {
        try {
            if (!_plugin.getConfig().getBoolean("usePlayerLocale"))
                return _defaultLocale;
            return player.locale().getISO3Language();
        }
        catch (Exception ex) {
            _logger.Warn("Failed to get the player's locale.");
            _logger.Error(ex.getMessage());
            return "eng";
        }
    }

    /**
     * Localizes a given key to its corresponding value.
     *
     * @param key the key to be localized.
     * @return the localized string, or an empty string if the key is not found.
     */
    public String Localize(String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization;
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return "";
                }
            }

            return value.toString();
        }
        catch (Exception ex)
        {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
            return "";
        }
    }

    /**
     * Localizes a given key to its corresponding list of values.
     *
     * @param key the key to be localized.
     * @return the localized list of strings, or an empty list if the key is not found.
     */
    public List<String> LocalizeList(String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization;
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return new ArrayList<>();
                }
            }

            return (List<String>)value;
        }
        catch (Exception ex)
        {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Localizes a given key to its corresponding array of values.
     *
     * @param key the key to be localized.
     * @return the localized array of strings, or an empty array if the key is not found.
     */
    public String[] LocalizeArray(String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization;
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return new String[0];
                }
            }

            return (String[])value;
        }
        catch (Exception ex)
        {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
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
    public String Localize(String key, Object... args) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization;
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return "";
                }
            }

            return MessageFormat.format(value.toString(), args);
        }
        catch (Exception ex)
        {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
            return "";
        }
    }

    /**
     * Localizes a given key to its corresponding value for a specific player.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @return The localized string, or an empty string if the key is not found.
     */
    public String Localize(Player player, String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization.get(GetPlayerLocale(player));
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return "";
                }
            }

            return value.toString();
        }
        catch (Exception ex)
        {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
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
    public List<String> LocalizeList(Player player, String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization.get(GetPlayerLocale(player));
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return new ArrayList<>();
                }
            }

            return (List<String>)value;
        }
        catch (Exception ex)
        {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
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
    public String[] LocalizeArray(Player player,String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization.get(GetPlayerLocale(player));
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return new String[0];
                }
            }

            return (String[])value;
        }
        catch (Exception ex)
        {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
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
    public String Localize(Player player,String key, Object... args) {
        try {
            String[] keys = key.split("\\.");
            Object value = _localization.get(GetPlayerLocale(player));
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return "";
                }
            }

            return MessageFormat.format(value.toString(), args);
        } catch (Exception ex) {
            _logger.Warn(String.format("Unknown error happened while translating '%s'.", key));
            _logger.Error(ex.getMessage());
            return "";
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
            _logger.Error("Failed to check if directory is empty: ");
            _logger.Error(ex.getMessage());
            return false;
        }
    }
}
