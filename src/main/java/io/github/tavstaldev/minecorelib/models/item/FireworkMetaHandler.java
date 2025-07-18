package io.github.tavstaldev.minecorelib.models.item;

import io.github.tavstaldev.minecorelib.utils.ColorUtils;
import io.github.tavstaldev.minecorelib.utils.ItemMetaSerializer;
import io.github.tavstaldev.minecorelib.utils.TypeUtils;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A handler for serializing and deserializing the metadata of a {@link FireworkMeta}.
 * This class provides static methods to convert the firework effects and power of a firework
 * into a storable format and reconstruct them from the stored data.
 */
public class FireworkMetaHandler {

    /**
     * Serializes the metadata of a {@link FireworkMeta} into a map.
     * If the provided {@link ItemMeta} is not an instance of {@link FireworkMeta},
     * the method returns without modifying the map.
     *
     * @param parent   The {@link ItemMetaSerializer} used for additional serialization logic.
     * @param meta     The {@link ItemMeta} to serialize, expected to be a {@link FireworkMeta}.
     * @param itemData The map where the serialized data will be stored.
     * @throws Exception If an error occurs during serialization.
     */
    public static void serialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!(meta instanceof FireworkMeta fireworkMeta))
            return;

        if (fireworkMeta.hasEffects()) {
            List<Map<String, Object>> effects = new ArrayList<>();
            for (var effect : fireworkMeta.getEffects()) {
                Map<String, Object> effectData = new HashMap<>();
                effectData.put("type", effect.getType().name());
                effectData.put("flicker", effect.hasFlicker());
                effectData.put("trail", effect.hasTrail());

                // Colors
                var colorList = effect.getColors();
                List<String> colors = new ArrayList<>();
                for (Color color : colorList) {
                    colors.add(ColorUtils.serializeColor(color));
                }
                effectData.put("colors", colors);

                // FadeColors
                var fadeColor = effect.getFadeColors();
                List<String> fadeColors = new ArrayList<>();
                for (Color color : fadeColor) {
                    fadeColors.add(ColorUtils.serializeColor(color));
                }
                effectData.put("fadeColors", fadeColors);
                effects.add(effectData);
            }
            itemData.put("effects", effects);
        }

        if (fireworkMeta.hasPower()) {
            itemData.put("power", fireworkMeta.getPower());
        }
    }

    /**
     * Deserializes the metadata from a map into a {@link FireworkMeta}.
     * If the provided {@link ItemMeta} is not an instance of {@link FireworkMeta},
     * or if the map does not contain valid firework data, the method returns without modifying the meta.
     *
     * @param parent   The {@link ItemMetaSerializer} used for additional deserialization logic.
     * @param meta     The {@link ItemMeta} to populate, expected to be a {@link FireworkMeta}.
     * @param itemData The map containing the serialized firework metadata.
     * @throws Exception If the firework data is invalid or if an error occurs during deserialization.
     */
    public static void deserialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!(meta instanceof FireworkMeta fireworkMeta))
            return;

        if (itemData.containsKey("effects")) {

            List<Map<String, Object>> effects = TypeUtils.castAsListOfMaps(itemData.get("effects"), null);
            if (effects == null) {
                throw new IllegalArgumentException("Invalid effects data format.");
            }

            for (var effectData : effects) {
                FireworkEffect.Type type = FireworkEffect.Type.valueOf((String) effectData.get("type"));
                boolean flicker = (boolean) effectData.get("flicker");
                boolean trail = (boolean) effectData.get("trail");

                List<Color> colors = new ArrayList<>();
                if (effectData.containsKey("colors")) {
                    List<String> rawColors = TypeUtils.castAsList(effectData.get("colors"), null);
                    if (rawColors != null) {
                        for (String colorData : rawColors) {
                            colors.add(ColorUtils.deserializeColor(colorData));
                        }
                    }
                }

                List<Color> fadeColors = new ArrayList<>();
                if (effectData.containsKey("fadeColors")) {
                    // Ensure fadeColors is a list of strings
                    List<String> rawFadeColors = TypeUtils.castAsList(effectData.get("fadeColors"), null);
                    if (rawFadeColors != null) {
                        for (String colorData : rawFadeColors) {
                            fadeColors.add(ColorUtils.deserializeColor(colorData));
                        }
                    }
                }

                fireworkMeta.addEffect(FireworkEffect.builder().flicker(flicker).trail(trail).with(type).withColor(colors).withFade(fadeColors).build());
            }
        }

        if (itemData.containsKey("power")) {
            fireworkMeta.setPower((int) itemData.get("power"));
        }
    }
}