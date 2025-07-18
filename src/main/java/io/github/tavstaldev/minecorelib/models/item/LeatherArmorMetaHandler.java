package io.github.tavstaldev.minecorelib.models.item;

import io.github.tavstaldev.minecorelib.utils.ColorUtils;
import io.github.tavstaldev.minecorelib.utils.ItemMetaSerializer;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Map;

/**
 * A handler for serializing and deserializing the color metadata of a {@link LeatherArmorMeta}.
 * This class provides static methods to convert the color of leather armor into a storable format
 * and reconstruct it from the stored data.
 */
public class LeatherArmorMetaHandler {

    /**
     * Serializes the color of a {@link LeatherArmorMeta} into a map.
     * If the provided {@link ItemMeta} is not an instance of {@link LeatherArmorMeta},
     * the method returns without modifying the map.
     *
     * @param parent   The {@link ItemMetaSerializer} used for additional serialization logic.
     * @param meta     The {@link ItemMeta} to serialize, expected to be a {@link LeatherArmorMeta}.
     * @param itemData The map where the serialized data will be stored.
     * @throws Exception If an error occurs during serialization.
     */
    public static void serialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!(meta instanceof LeatherArmorMeta leatherArmorMeta))
            return;

        var color = leatherArmorMeta.getColor();
        itemData.put("color", ColorUtils.serializeColor(color));
    }

    /**
     * Deserializes the color metadata from a map into a {@link LeatherArmorMeta}.
     * If the provided {@link ItemMeta} is not an instance of {@link LeatherArmorMeta},
     * or if the map does not contain valid color data, the method returns without modifying the meta.
     *
     * @param parent   The {@link ItemMetaSerializer} used for additional deserialization logic.
     * @param meta     The {@link ItemMeta} to populate, expected to be a {@link LeatherArmorMeta}.
     * @param itemData The map containing the serialized color data.
     * @throws Exception If the color data is invalid or if an error occurs during deserialization.
     */
    public static void deserialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!(meta instanceof LeatherArmorMeta leatherArmorMeta))
            return;

        if (!itemData.containsKey("color"))
            return;

        String colorData = (String) itemData.get("color");
        leatherArmorMeta.setColor(ColorUtils.deserializeColor(colorData));
    }
}