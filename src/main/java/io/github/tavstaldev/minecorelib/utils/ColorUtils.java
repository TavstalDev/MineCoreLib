package io.github.tavstaldev.minecorelib.utils;

import org.bukkit.Color;

/**
 * Utility class for serializing and deserializing {@link Color} objects.
 * Provides methods to convert a {@link Color} to a string representation
 * and reconstruct a {@link Color} from its string representation.
 */
public class ColorUtils {

    /**
     * Serializes a {@link Color} object into a string representation.
     * The format of the string is "alpha;red;green;blue".
     *
     * @param color The {@link Color} object to serialize.
     * @return A string representation of the color in the format "alpha;red;green;blue".
     */
    public static String serializeColor(Color color) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        int alpha = color.getAlpha();
        return String.format("%s;%s;%s;%s",
                alpha,
                red,
                green,
                blue
        );
    }

    /**
     * Deserializes a string representation of a color into a {@link Color} object.
     * The input string must be in the format "alpha;red;green;blue".
     *
     * @param colorData The string representation of the color.
     * @return A {@link Color} object reconstructed from the string representation.
     * @throws NumberFormatException If the string contains invalid numeric values.
     * @throws ArrayIndexOutOfBoundsException If the string does not contain exactly four components.
     */
    public static Color deserializeColor(String colorData) {
        String[] color = colorData.split(";");
        return Color.fromARGB(
                Integer.parseInt(color[0]),
                Integer.parseInt(color[1]),
                Integer.parseInt(color[2]),
                Integer.parseInt(color[3])
        );
    }
}