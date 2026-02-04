package io.github.tavstaldev.minecorelib.utils;

import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;

import java.util.Map;

/**
 * Utility class for time-related operations in the SkyBlockCore plugin.
 */
public class TimeUtil {
    /**
     * Formats a duration given in milliseconds into a localized human-readable string.
     * This method delegates to {@link #formatDurationSeconds(PluginBase, long)}
     * after converting milliseconds to seconds.
     *
     * @param plugin the plugin instance used to access the translator for localization
     * @param millis the duration in milliseconds to be formatted
     * @return a localized human-readable string representing the duration
     */
    public static String formatDurationMillis(final PluginBase plugin, final long millis) {
        return formatDurationSeconds(plugin, millis / 1000);
    }

    /**
     * Formats a duration given in seconds into a localized human-readable string.
     * The duration is broken down into days, hours, minutes, and seconds, and each
     * unit is localized using the plugin's translator.
     *
     * @param plugin the plugin instance used to access the translator for localization
     * @param seconds the duration in seconds to be formatted
     * @return a localized human-readable string representing the duration
     */
    public static String formatDurationSeconds(final PluginBase plugin, final long seconds) {
        final PluginTranslator translator = plugin.getTranslator();
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(translator.localize("Time.Days", Map.of("time", String.valueOf(days)))).append(" ");
        }
        if (hours > 0) {
            sb.append(translator.localize("Time.Hours", Map.of("time", String.valueOf(hours)))).append(" ");
        }
        if (minutes > 0) {
            sb.append(translator.localize("Time.Minutes", Map.of("time", String.valueOf(minutes)))).append(" ");
        }
        if (secs > 0 || days == 0 && hours == 0 && minutes == 0) {
            sb.append(translator.localize("Time.Seconds", Map.of("time", String.valueOf(secs)))).append(" ");
        }

        return sb.toString().trim();
    }

    /**
     * Formats a countdown given in seconds into a "MM:SS" format if minutes are greater than 0,
     * otherwise returns the seconds as a plain number.
     *
     * @param seconds the countdown duration in seconds
     * @return a string representing the countdown in "MM:SS" format or plain seconds
     */
    public static String formatCountdownMMSS(final long seconds) {
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (minutes > 0) {
            sb.append(String.format("%02d", minutes)).append(":");
            sb.append(String.format("%02d", secs));
        } else {
            sb.append(secs);
        }

        return sb.toString();
    }
}