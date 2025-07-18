package io.github.tavstaldev.minecorelib.utils;

import org.bukkit.Bukkit;

import java.lang.reflect.Method;

/**
 * Utility class for handling server version-related operations.
 */
public class VersionUtils {
    private static String _version = null;

    /**
     * Retrieves the current server version as a string.
     *
     * @return The server version string, e.g., "1.8.8-R0.1-SNAPSHOT".
     */
    public static String getServerVersion() {
        if (_version != null) {
            return _version;
        }

        String version;
        try {
            Method method = Bukkit.class.getDeclaredMethod("getMinecraftVersion");
            version = (String) method.invoke(null);
        } catch (Exception e) {
            // Fallback for older versions
            version = Bukkit.getServer().getClass().getPackage().getName()
                    .split("\\.")[3]
                    .replace("_", ".")
                    .replaceAll("[A-Z]|[a-z]", "");
        }
        _version = version;
        return version;
    }

    /**
     * Checks if the server version is at least the specified major and minor version.
     *
     * @param major The major version to compare against.
     * @param minor The minor version to compare against.
     * @return True if the server version is at least the specified version, false otherwise.
     */
    public static boolean isAtLeast(int major, int minor) {
        String[] parts = getServerVersion().split("\\.");
        int maj = Integer.parseInt(parts[0]);
        int min = Integer.parseInt(parts[1]);
        return (maj > major) || (maj == major && min >= minor);
    }

    /**
     * Checks if the server version is at least the specified major, minor, and patch version.
     * This method parses the server version string and compares it against the provided version numbers.
     * If the patch version is not specified in the server version, it defaults to 0.
     *
     * @param major The major version to compare against.
     * @param minor The minor version to compare against.
     * @param patch The patch version to compare against.
     * @return True if the server version is at least the specified version, false otherwise.
     */
    public static boolean isAtLeast(int major, int minor, int patch) {
        String[] parts = getServerVersion().split("\\.");
        int maj = Integer.parseInt(parts[0]);
        int min = Integer.parseInt(parts[1]);
        int pat;
        if (parts.length > 2) {
            pat = Integer.parseInt(parts[2]);
        } else {
            pat = 0; // Default to 0 if patch version is not specified
        }
        return maj > major || maj == major && min > minor || maj == major && min == minor && pat >= patch;
    }

    /**
     * Determines if the server version is considered legacy.
     *
     * @return True if the server version is legacy (not at least 1.13), false otherwise.
     */
    public static boolean isLegacy() {
        return !isAtLeast(1, 13); // Legacy if not at least 1.13
    }
}