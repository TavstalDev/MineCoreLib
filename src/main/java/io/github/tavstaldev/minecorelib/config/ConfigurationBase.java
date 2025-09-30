package io.github.tavstaldev.minecorelib.config;

import io.github.tavstaldev.minecorelib.PluginBase;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * An abstract base class for managing YAML configuration files in a Bukkit plugin.
 * Provides methods for loading, saving, and resolving configuration values.
 */
public abstract class ConfigurationBase extends YamlConfiguration {
    private final PluginBase plugin; // The plugin instance associated with this configuration.
    private final File _file; // The file where the configuration is stored.

    /**
     * Constructs a new ConfigurationBase instance.
     * If the configuration file does not exist, it is created and optionally populated with a resource.
     *
     * @param plugin       The plugin instance.
     * @param relativePath The relative path to the configuration file.
     * @param resourcePath The path to the resource file to copy, or null if no resource should be used.
     */
    public ConfigurationBase(PluginBase plugin, String relativePath, @Nullable String resourcePath) {
        this.plugin = plugin;
        Path filePath = Paths.get(plugin.getDataFolder().getPath(), relativePath);
        _file = filePath.toFile();
        if (!_file.exists()) {
            try {
                if (resourcePath != null) {
                    var resourceStream = plugin.getResource(relativePath);
                    if (resourceStream == null) {
                        if (!_file.isDirectory()) {
                            //noinspection ResultOfMethodCallIgnored
                            _file.getParentFile().mkdirs();
                        }
                        //noinspection ResultOfMethodCallIgnored
                        _file.createNewFile();
                    } else {
                        Files.copy(resourceStream, filePath);
                    }
                } else {
                    if (!_file.isDirectory()) {
                        //noinspection ResultOfMethodCallIgnored
                        _file.getParentFile().mkdirs();
                    }
                    //noinspection ResultOfMethodCallIgnored
                    _file.createNewFile();
                }
            } catch (Exception ex) {
                plugin.getCustomLogger().error("Could not create configuration file: " + filePath);
                plugin.getCustomLogger().error(ex.getMessage());
            }
        }
        load();
    }

    /**
     * Loads the configuration file and applies default values.
     * This method must be implemented by subclasses to define default values.
     */
    protected abstract void loadDefaults();

    /**
     * Loads the configuration file from disk.
     * If the file cannot be loaded, an error is logged.
     */
    public void load() {
        try {
            this.load(_file);
        } catch (Exception ex) {
            plugin.getCustomLogger().error("Could not load configuration file: " + _file.getPath());
            plugin.getCustomLogger().error(ex.getMessage());
        }
        loadDefaults();
        save();
    }

    /**
     * Saves the configuration file to disk.
     * If the file cannot be saved, an error is logged.
     */
    public void save() {
        try {
            this.save(_file);
        } catch (Exception ex) {
            plugin.getCustomLogger().error("Could not save configuration file: " + _file.getPath());
            plugin.getCustomLogger().error(ex.getMessage());
        }
    }

    /**
     * Resolves a configuration path by setting a default value if the path does not exist.
     *
     * @param path  The configuration path.
     * @param value The default value to set if the path does not exist.
     */
    public void resolve(String path, Object value) {
        if (!this.contains(path)) {
            this.set(path, value);
        }
    }

    /**
     * Resolves a configuration path by getting its value or setting a default value if it does not exist.
     *
     * @param path  The configuration path.
     * @param value The default value to set if the path does not exist.
     * @param <T>   The type of the value.
     * @return The existing or default value.
     */
    public <T> T resolveGet(String path, T value) {
        try {
            @SuppressWarnings("unchecked") T local = (T) this.get(path);
            if (local == null) {
                this.set(path, value);
                return (T) value;
            }
            return local;
        } catch (Exception ignored) {
            return value;
        }
    }

    /**
     * Resolves a configuration path by setting comments if none exist.
     *
     * @param path     The configuration path.
     * @param comments The comments to set.
     */
    public void resolveComment(String path, List<String> comments) {
        if (this.getComments(path).isEmpty())
            this.setComments(path, comments);
    }
}