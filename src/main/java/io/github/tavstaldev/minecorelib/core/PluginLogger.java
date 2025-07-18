package io.github.tavstaldev.minecorelib.core;

import io.github.tavstaldev.minecorelib.PluginBase;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for logging messages with different severity levels.
 */
public class PluginLogger {
    private final PluginBase _plugin;
    private final String _module;
    private final Logger _logger;

    /**
     * Constructs a new PluginLogger instance.
     *
     * @param plugin The plugin instance associated with this logger.
     */
    public PluginLogger(PluginBase plugin) {
        _plugin = plugin;
        _module = null;
        _logger = Logger.getLogger(plugin.getProjectName());
    }

    /**
     * Constructs a new PluginLogger instance with a specified module.
     *
     * @param plugin The plugin instance associated with this logger.
     * @param module The module name to be used in log messages.
     */
    public PluginLogger(PluginBase plugin, String module) {
        _plugin = plugin;
        _module = module;
        _logger = Logger.getLogger(plugin.getProjectName());
    }

    /**
     * Creates a new PluginLogger instance with the specified module.
     *
     * @param module The module name to be used in log messages.
     * @return A new PluginLogger instance with the specified module.
     */
    public PluginLogger WithModule(String module) {
        return new PluginLogger(_plugin, module);
    }

    /**
     * Creates a new PluginLogger instance with the specified module.
     *
     * @param module The class whose name will be used as the module name in log messages.
     * @return A new PluginLogger instance with the specified module.
     */
    public PluginLogger WithModule(@NotNull Class<?> module) {
        return new PluginLogger(_plugin,module.getSimpleName());
    }

    /**
     * Logs a message with the specified severity level.
     *
     * @param level The severity level of the log message.
     * @param text  The message to log.
     */
    private void Log(@NotNull Level level, @NotNull String text) {
        String moduleText = "";
        if (_module != null)
            moduleText = String.format(" [%s]", _module);

        _logger.log(level, String.format("%s: %s", moduleText, text));
    }

    /**
     * Logs a message with the specified severity level, module name, and color.
     * This method allows for rich text logging by including a color code in the log message.
     *
     * @param level The severity level of the log message.
     * @param text  The message to log.
     * @param color The color code to prepend to the log message for rich text formatting.
     */
    private void LogRich(@NotNull Level level, @NotNull String text, @NotNull String color) {
        String moduleText = "";
        if (_module != null)
            moduleText = String.format(" [%s]", _module);

        _logger.log(level, String.format("%s%s: %s\u001B[0m", color, moduleText, text));
    }

    /**
     * Converts the given object to a string representation.
     *
     * @param text The object to convert.
     * @return The string representation of the object.
     */
    private @NotNull String GetString(@NotNull Object text) {
        if (text instanceof Exception ex) {
            return ex.getMessage();
        }
        if (text instanceof String str) {
            return str;
        }
        return text.toString();
    }

    /**
     * Logs an informational message.
     *
     * @param text The message to log.
     */
    public void Info(@NotNull Object text) {
        Log(Level.INFO, GetString(text));
    }

    /**
     * Logs a success message with a green color code.
     * This method uses rich text formatting to prepend a green color code
     * to the log message, indicating a successful operation.
     *
     * @param text The message to log, which can be any object.
     *             The object will be converted to a string representation.
     */
    public void Ok(@NotNull Object text) {
        LogRich(Level.INFO, GetString(text), "\u001B[32m");
    }

    /**
     * Logs a warning message.
     *
     * @param text The message to log.
     */
    public void Warn(@NotNull Object text) {
        Log(Level.WARNING, GetString(text));
    }

    /**
     * Logs an error message.
     *
     * @param text The message to log.
     */
    public void Error(@NotNull Object text) {
        Log(Level.SEVERE, GetString(text));
    }

    /**
     * Logs a debug message if debugging is enabled in the configuration.
     *
     * @param text The message to log.
     */
    public void Debug(@NotNull Object text) {
        if (_plugin.getConfig().getBoolean("debug"))
            Log(Level.INFO, GetString(text));
    }
}
