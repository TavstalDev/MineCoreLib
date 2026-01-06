package io.github.tavstaldev.minecorelib.utils;

import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;

import java.util.Map;

/**
 * Utility class for time-related operations in the SkyBlockCore plugin.
 */
public class TimeUtil {

    /**
     * Formats a timestamp into a human-readable, localized string.
     * The formatted string includes days, hours, minutes, and seconds, if applicable.
     *
     * @param timestamp The timestamp in milliseconds to be formatted.
     * @return A localized string representing the formatted time.
     */
    public static String formatTimestamp(final PluginBase plugin, final long timestamp) {
        final PluginTranslator translator = plugin.getTranslator();
        final long seconds = timestamp / 1000L;
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
     * Formats a countdown duration into a human-readable string.
     * The formatted string is in the format "MM:SS" if minutes are present,
     * or just seconds if less than a minute.
     *
     * @param milliseconds The duration in milliseconds to be formatted.
     * @return A string representing the formatted countdown time.
     */
    public static String formatCountdown(final long milliseconds) {
        final long seconds = milliseconds / 1000L;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (minutes > 0) {
            sb.append(String.format("%02d", minutes)).append(":");
            sb.append(String.format("%02d", secs));
        }
        else {
            sb.append(secs);
        }

        return sb.toString();
    }
}