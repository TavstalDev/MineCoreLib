package io.github.tavstaldev.minecorelib.models.item;

import io.github.tavstaldev.minecorelib.utils.ItemMetaSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

/**
 * A handler for serializing and deserializing the charged projectiles of a {@link CrossbowMeta}.
 * This class provides static methods to convert the charged projectiles of a crossbow
 * into a storable format and reconstruct them from the stored data.
 */
public class CrossbowMetaHandler {

    /**
     * Serializes the charged projectiles of a {@link CrossbowMeta} into a map.
     * If the provided {@link ItemMeta} is not an instance of {@link CrossbowMeta} or
     * if there are no charged projectiles, the method returns without modifying the map.
     *
     * @param parent   The {@link ItemMetaSerializer} used for serializing the projectiles.
     * @param meta     The {@link ItemMeta} to serialize, expected to be a {@link CrossbowMeta}.
     * @param itemData The map where the serialized data will be stored.
     * @throws Exception If an error occurs during serialization.
     */
    public static void serialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!(meta instanceof CrossbowMeta crossbowMeta))
            return;

        if (!crossbowMeta.hasChargedProjectiles())
            return;

        List<ItemStack> projectiles = crossbowMeta.getChargedProjectiles();
        var projectileData = parent.serializeItemStackListToBytes(projectiles);
        itemData.put("projectiles", projectileData);
    }

    /**
     * Deserializes the charged projectiles from a map into a {@link CrossbowMeta}.
     * If the provided {@link ItemMeta} is not an instance of {@link CrossbowMeta} or
     * if the map does not contain valid projectile data, the method returns without modifying the meta.
     *
     * @param parent   The {@link ItemMetaSerializer} used for deserializing the projectiles.
     * @param meta     The {@link ItemMeta} to populate, expected to be a {@link CrossbowMeta}.
     * @param itemData The map containing the serialized projectile data.
     * @throws Exception If the projectile data is not a byte array or if an error occurs during deserialization.
     */
    public static void deserialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!(meta instanceof CrossbowMeta crossbowMeta))
            return;

        if (!itemData.containsKey("projectiles"))
            return;

        var rawProjectiles = itemData.get("projectiles");
        if (!(rawProjectiles instanceof byte[])) {
            throw new Exception("Expected projectiles data to be a byte array.");
        }

        List<ItemStack> projectiles = parent.deserializeItemStackListFromBytes((byte[])rawProjectiles);
        crossbowMeta.setChargedProjectiles(projectiles);
    }
}