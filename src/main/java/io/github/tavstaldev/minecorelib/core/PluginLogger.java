package io.github.tavstaldev.minecorelib.core;

import io.github.tavstaldev.minecorelib.PluginBase;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for logging messages with different severity levels.
 */
public class PluginLogger {
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

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
    public PluginLogger withModule(String module) {
        return new PluginLogger(_plugin, module);
    }

    /**
     * Creates a new PluginLogger instance with the specified module.
     *
     * @param module The class whose name will be used as the module name in log messages.
     * @return A new PluginLogger instance with the specified module.
     */
    public PluginLogger withModule(@NotNull Class<?> module) {
        return new PluginLogger(_plugin,module.getSimpleName());
    }

    /**
     * Logs a message with the specified severity level.
     *
     * @param level The severity level of the log message.
     * @param text  The message to log.
     */
    private void log(@NotNull Level level, @NotNull String text) {
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
    private void logRich(@NotNull Level level, @NotNull String text, @NotNull String color) {
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
    private @NotNull String getString(@NotNull Object text) {
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
    public void info(@NotNull Object text) {
        logRich(Level.INFO, getString(text), ANSI_CYAN);
    }

    /**
     * Logs a success message with a green color code.
     * This method uses rich text formatting to prepend a green color code
     * to the log message, indicating a successful operation.
     *
     * @param text The message to log, which can be any object.
     *             The object will be converted to a string representation.
     */
    public void ok(@NotNull Object text) {
        logRich(Level.INFO, getString(text), ANSI_GREEN);
    }

    /**
     * Logs a warning message.
     *
     * @param text The message to log.
     */
    public void warn(@NotNull Object text) {
        logRich(Level.WARNING, getString(text), ANSI_YELLOW);
    }

    /**
     * Logs an error message.
     *
     * @param text The message to log.
     */
    public void error(@NotNull Object text) {
        logRich(Level.SEVERE, getString(text), ANSI_RED);
    }

    /**
     * Logs a debug message if debugging is enabled in the configuration.
     *
     * @param text The message to log.
     */
    public void debug(@NotNull Object text) {
        // noinspection ConstantConditions
        if (_plugin.getConfig() == null)
            return;

        if (_plugin.getConfig().getBoolean("debug", false))
            logRich(Level.INFO, getString(text), ANSI_PURPLE);
    }
}
