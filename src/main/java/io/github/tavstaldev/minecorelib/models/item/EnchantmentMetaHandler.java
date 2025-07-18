package io.github.tavstaldev.minecorelib.models.item;

import io.github.tavstaldev.minecorelib.utils.ItemMetaSerializer;
import io.github.tavstaldev.minecorelib.utils.TypeUtils;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * A handler for serializing and deserializing the enchantments of an {@link ItemMeta}.
 * This class provides static methods to convert the enchantments of an item into a storable format
 * and reconstruct them from the stored data.
 */
public class EnchantmentMetaHandler {

    /**
     * Serializes the enchantments of an {@link ItemMeta} into a map.
     * If the provided {@link ItemMeta} does not have any enchantments, the method returns without modifying the map.
     *
     * @param parent   The {@link ItemMetaSerializer} used for additional serialization logic.
     * @param meta     The {@link ItemMeta} to serialize, expected to have enchantments.
     * @param itemData The map where the serialized enchantment data will be stored.
     * @throws Exception If an error occurs during serialization.
     */
    public static void serialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!meta.hasEnchants())
            return;

        Map<String, Integer> enchantments = new HashMap<>();
        for (var entry : meta.getEnchants().entrySet()) {
            enchantments.put(entry.getKey().getKey().toString(), entry.getValue());  // Store enchantment names and levels
        }
        itemData.put("enchantments", enchantments);
    }

    /**
     * Deserializes the enchantments from a map into an {@link ItemMeta}.
     * If the provided map does not contain valid enchantment data, the method returns without modifying the meta.
     *
     * @param parent   The {@link ItemMetaSerializer} used for additional deserialization logic.
     * @param meta     The {@link ItemMeta} to populate with enchantments.
     * @param itemData The map containing the serialized enchantment data.
     * @throws Exception If the enchantment data is invalid or if an error occurs during deserialization.
     */
    public static void deserialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!itemData.containsKey("enchantments"))
            return;

        Map<String, Integer> enchantments = TypeUtils.castAsMap(itemData.get("enchantments"), null);
        if (enchantments == null || enchantments.isEmpty())
            return;
        for (var entry : enchantments.entrySet()) {
            var namespacedKey = NamespacedKey.fromString(entry.getKey());
            if (namespacedKey == null)
                continue;

            Enchantment enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(namespacedKey);
            if (enchantment == null)
                continue;

            meta.addEnchant(enchantment, entry.getValue(), true);
        }
    }
}