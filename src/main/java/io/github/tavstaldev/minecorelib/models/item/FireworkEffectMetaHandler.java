package io.github.tavstaldev.minecorelib.models.item;

import io.github.tavstaldev.minecorelib.utils.ColorUtils;
import io.github.tavstaldev.minecorelib.utils.ItemMetaSerializer;
import io.github.tavstaldev.minecorelib.utils.TypeUtils;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A handler for serializing and deserializing the firework effect metadata of a {@link FireworkEffectMeta}.
 * This class provides static methods to convert the firework effect of an item into a storable format
 * and reconstruct it from the stored data.
 */
public class FireworkEffectMetaHandler {

    /**
     * Serializes the firework effect metadata of a {@link FireworkEffectMeta} into a map.
     * If the provided {@link ItemMeta} is not an instance of {@link FireworkEffectMeta},
     * or if the firework effect is null, the method returns without modifying the map.
     *
     * @param parent   The {@link ItemMetaSerializer} used for additional serialization logic.
     * @param meta     The {@link ItemMeta} to serialize, expected to be a {@link FireworkEffectMeta}.
     * @param itemData The map where the serialized data will be stored.
     * @throws Exception If an error occurs during serialization.
     */
    public static void serialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!(meta instanceof FireworkEffectMeta fireworkEffectMeta))
            return;

        if (!fireworkEffectMeta.hasEffect() || fireworkEffectMeta.getEffect() == null)
            return;

        var effect = fireworkEffectMeta.getEffect();
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

        itemData.put("effect", effectData);
    }

    /**
     * Deserializes the firework effect metadata from a map into a {@link FireworkEffectMeta}.
     * If the provided {@link ItemMeta} is not an instance of {@link FireworkEffectMeta},
     * or if the map does not contain valid effect data, the method returns without modifying the meta.
     *
     * @param parent   The {@link ItemMetaSerializer} used for additional deserialization logic.
     * @param meta     The {@link ItemMeta} to populate, expected to be a {@link FireworkEffectMeta}.
     * @param itemData The map containing the serialized firework effect data.
     * @throws Exception If the effect data is invalid or if an error occurs during deserialization.
     */
    public static void deserialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!(meta instanceof FireworkEffectMeta fireworkEffectMeta))
            return;

        if (!itemData.containsKey("effect"))
            return;

        Map<String, Object> effectData = TypeUtils.castAsMap(itemData.get("effect"), null);
        if (effectData == null || effectData.isEmpty())
            throw new Exception("Invalid effect data for FireworkEffectMeta");

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
            List<String> rawColors = TypeUtils.castAsList(effectData.get("fadeColors"), null);
            if (rawColors != null) {
                for (String colorData : rawColors) {
                    fadeColors.add(ColorUtils.deserializeColor(colorData));
                }
            }
        }

        fireworkEffectMeta.setEffect(
                FireworkEffect.builder()
                        .flicker(flicker)
                        .trail(trail)
                        .with(type)
                        .withColor(colors)
                        .withFade(fadeColors)
                        .build()
        );
    }
}