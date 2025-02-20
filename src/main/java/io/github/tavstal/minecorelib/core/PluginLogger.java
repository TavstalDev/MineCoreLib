package io.github.tavstal.minecorelib.core;

import io.github.tavstal.minecorelib.models.PluginBase;
import io.github.tavstal.openkits.OpenKits;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Utility class for logging messages with different severity levels.
 */
public class PluginLogger {
    private final PluginBase<?> _plugin;
    private final Logger _logger;

    public PluginLogger(PluginBase<?> plugin) {
        _plugin = plugin;
        _logger = Logger.getLogger(plugin.getProjectName());
    }

    /**
     * Logs an informational message.
     *
     * @param text the message to log.
     */
    public void LogInfo(String text) {
        _logger.log(java.util.logging.Level.INFO, String.format("%s", text));
    }

    /**
     * Logs a warning message.
     *
     * @param text the message to log.
     */
    public void LogWarning(String text) {
        _logger.log(java.util.logging.Level.WARNING, String.format("%s", text));
    }

    /**
     * Logs an error message.
     *
     * @param text the message to log.
     */
    public void LogError(String text) {
        _logger.log(java.util.logging.Level.SEVERE, String.format("%s", text));
    }

    /**
     * Logs a debug message if debugging is enabled in the configuration.
     *
     * @param text the message to log.
     */
    public void LogDebug(String text) {
        if (_plugin.getConfig().getBoolean("debug"))
            _logger.log(java.util.logging.Level.INFO, String.format("%s", text));
    }
}
