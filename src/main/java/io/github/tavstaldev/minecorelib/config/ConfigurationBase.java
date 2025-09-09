package io.github.tavstaldev.minecorelib.config;

import io.github.tavstaldev.minecorelib.PluginBase;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class ConfigurationBase extends YamlConfiguration {
    private final PluginBase plugin;
    private final File _file;

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
                    }
                    else {
                        Files.copy(resourceStream, filePath);
                    }
                }
                else {
                    if (!_file.isDirectory()) {
                        //noinspection ResultOfMethodCallIgnored
                        _file.getParentFile().mkdirs();
                    }
                    //noinspection ResultOfMethodCallIgnored
                    _file.createNewFile();
                }
            }
            catch (Exception ex) {
                plugin.getCustomLogger().Error("Could not create configuration file: " + filePath);
                plugin.getCustomLogger().Error(ex.getMessage());
            }
        }
        load();
    }

    protected abstract void loadDefaults();

    public void load() {
        try {
            this.load(_file);
        }
        catch (Exception ex) {
            plugin.getCustomLogger().Error("Could not load configuration file: " + _file.getPath());
            plugin.getCustomLogger().Error(ex.getMessage());
        }
        loadDefaults();
        save();
    }

    public void save(){
        try {
            this.save(_file);
        }
        catch (Exception ex) {
            plugin.getCustomLogger().Error("Could not save configuration file: " + _file.getPath());
            plugin.getCustomLogger().Error(ex.getMessage());
        }
    }

    public void resolve(String path, Object value) {
        if (!this.contains(path)) {
            this.set(path, value);
        }
    }

    public <T> T resolveGet(String path, T value) {
        // Get the existing value from the specified path
        try {
            @SuppressWarnings("unchecked") T local = (T) this.get(path);

            // If the existing value is null, or if the path doesn't contain a value,
            // set the new value and return it.
            if (local == null) {
                this.set(path, value);
                return (T) value;
            }

            // Otherwise, return the existing value.
            return local;
        }
        catch (Exception ignored) {
            //this.set(path, value);
            return value;
        }
    }

    public void resolveComment(String path, List<String> comments) {
        if (this.getComments(path).isEmpty())
            this.setComments(path, comments);
    }
}
