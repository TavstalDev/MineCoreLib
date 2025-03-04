package io.github.tavstal.minecorelib.models;

import io.github.tavstal.minecorelib.PluginBase;
import io.github.tavstal.minecorelib.core.PluginLogger;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for configuration management.
 */
public abstract class ConfigBase {
    private final PluginBase _plugin;
    private final PluginLogger _logger;
    private final String _resourcePath;
    private final Path _configFilePath;
    private final Path _configDir;
    private  Map<String, Object> _data;

    /**
     * Constructor for ConfigBase.
     *
     * @param plugin The plugin instance.
     * @param loggerModule The logger module class.
     * @param path The path to the configuration file.
     * @param resourcePath The path to the resource file.
     */
    public ConfigBase(@NotNull PluginBase plugin, @NotNull Class<?> loggerModule, @NotNull String path, @NotNull String resourcePath) {
        _plugin = plugin;
        _logger = plugin.getCustomLogger().WithModule(loggerModule);
        _resourcePath = resourcePath;
        _configFilePath = Paths.get(_plugin.getDataFolder().getPath(), path);
        _configDir = _configFilePath.getParent();
    }

    /**
     * Gets the name of the configuration class.
     *
     * @return The simple name of the class.
     */
    private String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Creates a new Yaml instance.
     *
     * @return A new Yaml instance.
     */
    protected Yaml createYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // Forces multi-line formatting
        options.setIndent(2);
        return new Yaml(options);
    }

    /**
     * Loads the configuration from the file.
     *
     * @return True if the configuration was loaded successfully, false otherwise.
     */
    public Boolean Load() {
        InputStream inputStream;
        _data = new HashMap<>();

        if (_configDir != null && !Files.exists(_configDir)) {
            try {
                Files.createDirectory(_configDir);
            } catch (IOException ex) {
                _logger.Warn("Failed to create config directory.");
                _logger.Error(ex.getMessage());
                return false;
            }
        }

        if (!Files.exists(_configFilePath)) {
            try {
                inputStream = _plugin.getResource(_resourcePath);
                if (inputStream == null) {
                    _logger.Debug(String.format("Failed to get config file from resources: %s", _resourcePath));
                } else
                    Files.copy(inputStream, _configFilePath);

            } catch (IOException ex) {
                _logger.Warn("Failed to create default config file.");
                _logger.Error(ex.getMessage());
                return false;
            }
        }

        _logger.Debug("Reading config directory...");
        try {
            inputStream = new FileInputStream(_configFilePath.toFile());
        } catch (FileNotFoundException ex) {
            _logger.Error("Failed to read config file.");
            return false;
        } catch (Exception ex) {
            _logger.Warn("Unknown error happened while reading config file.");
            _logger.Error(ex.getMessage());
            return false;
        }

        _logger.Debug("Loading yaml file...");
        Yaml yaml = createYaml();
        Object yamlObject = yaml.load(inputStream);
        if (!(yamlObject instanceof Map)) {
            _logger.Error("Failed to cast the yamlObject after reading the config file.");
            return false;
        }

        _logger.Debug("Casting yamlObject to Map...");
        try {
            //noinspection unchecked
            _data = (Map<String, Object>) yamlObject;
            Map<String, Object> updatedConfig = checkContent();
            if (updatedConfig == null)
                return true;

            _data = updatedConfig;
            if (!Save())
                _logger.Warn("Failed to auto update the config file.");
        } catch (Exception ex) {
            _logger.Warn("Failed to cast the yamlObject to Map.");
            _logger.Error(ex.getMessage());
        }
        return true;
    }

    /**
     * Checks the content of the configuration and updates it if necessary.
     *
     * @return The updated configuration map, or null if no update is needed.
     */
    protected Map<String, Object> checkContent() {
        Integer fileVersion = GetInt("FileVersion");
        if (fileVersion == null)
            fileVersion = 0;
        Map<String, Object> localValue = _data;
        InputStream inputStream;
        try {
            inputStream = _plugin.getResource(_resourcePath);
            if (inputStream == null) {
                _logger.Debug("Failed to get config resource file.'");
                return null;
            }

            _logger.Debug("Loading yaml file...");
            Yaml yaml = createYaml();
            Object yamlObject = yaml.load(inputStream);
            if (!(yamlObject instanceof Map))
            {
                _logger.Error("Failed to cast the yamlObject after reading the localization.");
                return null;
            }

            _logger.Debug("Casting yamlObject to Map...");
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> resourceYaml = (Map<String, Object>) yamlObject;
                int resourceVersion = 0;
                String rawResourceVersion = (String) GetValue(resourceYaml, "FileVersion");
                if (rawResourceVersion != null && !rawResourceVersion.isEmpty()) {
                    try {
                        resourceVersion = Integer.parseInt(rawResourceVersion);
                    } catch (NumberFormatException ex) {
                        _logger.Warn("Failed to parse the file version.");
                        _logger.Error(ex.getMessage());
                        return null;
                    }
                }

                if (resourceVersion <= fileVersion)
                    return null;

                for (String key : localValue.keySet()) {
                    if (key.equals("FileVersion"))
                        continue;

                    if (!resourceYaml.containsKey(key))
                        continue;

                    resourceYaml.put(key, localValue.get(key));
                }
                return resourceYaml;
            } catch (Exception ex) {
                _logger.Warn("Failed to cast the yamlObject to Map.");
                _logger.Error(ex.getMessage());
            }
        } catch (Exception ex) {
            _logger.Warn("Failed to create config file.");
            _logger.Error(ex.getMessage());
        }
        return null;
    }

    /**
     * Saves the configuration to the file.
     *
     * @return True if the configuration was saved successfully, false otherwise.
     */
    public Boolean Save() {
        Yaml yaml = createYaml();
        try (FileWriter writer = new FileWriter(_configFilePath.toString())) {
            yaml.dump(_data, writer);
        }
        catch (Exception ex) {
            _logger.Warn("Failed to save the config file.");
            _logger.Error(ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Gets the value associated with the specified key from the given map.
     *
     * @param map The map to search.
     * @param key The key to look for.
     * @return The value associated with the key, or null if not found.
     */
    public Object GetValue(Map<String, Object> map, String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = map;
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    _logger.Warn(String.format("Failed to get the value for the '%s' conf key.", key));
                    return null;
                }
            }

            return value;
        }
        catch (Exception ex)
        {
            _logger.Warn(String.format("Unknown error happened while getting the value of '%s'.", key));
            _logger.Error(ex.getMessage());
            return null;
        }
    }

    /**
     * Gets the value associated with the specified key from the configuration data.
     *
     * @param key The key to look for.
     * @return The value associated with the key, or null if not found.
     */
    public Object GetValue(String key) {
        return GetValue(_data, key);
    }

    /**
     * Gets the string value associated with the specified key from the configuration data.
     *
     * @param key The key to look for.
     * @return The string value associated with the key, or null if not found.
     */
    public String GetString(String key) {
        Object value = GetValue(key);
        if (value == null)
            return null;

        return value.toString();
    }

    /**
     * Gets the list of strings associated with the specified key from the configuration data.
     *
     * @param key The key to look for.
     * @return The list of strings associated with the key, or null if not found.
     */
    public List<String> GetStringList(String key) {
        Object value = GetValue(key);
        if (value == null)
            return null;

        if (!(value instanceof List<?> result))
            return null;

        return (List<String>)result;
    }

    /**
     * Gets the boolean value associated with the specified key from the configuration data.
     *
     * @param key The key to look for.
     * @return The boolean value associated with the key, or null if not found.
     */
    public Boolean GetBool(String key) {
        Object value = GetValue(key);
        if (value == null)
            return null;

        if (!(value instanceof Boolean result))
            return null;

        return result;
    }

    /**
     * Gets the integer value associated with the specified key from the configuration data.
     *
     * @param key The key to look for.
     * @return The integer value associated with the key, or null if not found.
     */
    public Integer GetInt(String key) {
        Object value = GetValue(key);
        if (value == null)
            return null;

        if (!(value instanceof Integer result))
            return null;

        return result;
    }

    /**
     * Gets the double value associated with the specified key from the configuration data.
     *
     * @param key The key to look for.
     * @return The double value associated with the key, or null if not found.
     */
    public Double GetDouble(String key) {
        Object value = GetValue(key);
        if (value == null)
            return null;

        if (!(value instanceof Double result))
            return null;

        return result;
    }

    /**
     * Gets the map associated with the specified key from the configuration data.
     *
     * @param key The key to look for.
     * @return The map associated with the key, or null if not found.
     */
    public Map<String, Object> GetMap(String key) {
        Object value = GetValue(key);
        if (value == null)
            return null;

        if (!(value instanceof Map<?, ?> result))
            return null;

        return (Map<String, Object>)result;
    }
}
