package io.github.tavstaldev.minecorelib.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for serializing and deserializing Bukkit Location objects.
 */
public class WorldUtils {

    /**
     * Serializes a Bukkit Location object into a string representation.
     * The format of the serialized string is: "worldName,x,y,z,yaw,pitch".
     *
     * @param location The Location object to serialize. If null, an empty string is returned.
     * @return A string representation of the Location object, or an empty string if the location is null.
     */
    public static String serializeLocation(Location location) {
        if (location == null) {
            return "";
        }
        return location.getWorld().getName() + "," +
                location.getX() + "," +
                location.getY() + "," +
                location.getZ() + "," +
                location.getYaw() + "," +
                location.getPitch();
    }

    /**
     * Deserializes a string representation of a Location object back into a Bukkit Location.
     * The input string must be in the format: "worldName,x,y,z,yaw,pitch".
     *
     * @param str The string to deserialize. If null or empty, null is returned.
     * @return The deserialized Location object, or null if the input string is null or empty.
     * @throws IllegalArgumentException If the input string is not in the correct format.
     */
    public static @Nullable Location deserializeLocation(String str) throws IllegalArgumentException {
        if (str == null || str.isEmpty()) {
            return null;
        }
        String[] parts = str.split(",");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid location string: " + str);
        }
        String worldName = parts[0];
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = Float.parseFloat(parts[4]);
        float pitch = Float.parseFloat(parts[5]);

        return new Location(
                Bukkit.getWorld(worldName),
                x, y, z, yaw, pitch
        );
    }
}