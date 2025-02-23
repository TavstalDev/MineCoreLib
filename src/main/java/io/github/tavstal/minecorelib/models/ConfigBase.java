package io.github.tavstal.minecorelib.models;

import io.github.tavstal.minecorelib.PluginBase;
import io.github.tavstal.minecorelib.core.PluginLogger;

import java.util.Map;

public abstract class ConfigBase {
    private final PluginBase _plugin;
    private final PluginLogger _logger;
    private  Map<String, Object> _data;

    public ConfigBase(PluginBase plugin, Class<?> loggerModule) {
        _plugin = plugin;
        _logger = plugin.getCustomLogger().WithModule(loggerModule);
    }


    // TODO: Implement the ConfigBase class
    public Boolean Load() {
        return false;
    }

    public Boolean Save() {
        return false;
    }

    public Boolean Reload() {
        return false;
    }

    public Boolean Reset() {
        return false;
    }

    public Boolean CheckUpdate() {
        return false;
    }
}
