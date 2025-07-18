package io.github.tavstaldev.minecorelib.models.item;

import io.github.tavstaldev.minecorelib.utils.ItemMetaSerializer;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

import java.util.Map;

/**
 * A handler for serializing and deserializing the custom entity type of a {@link SpawnEggMeta}.
 * This class provides static methods to convert the custom entity type of a spawn egg
 * into a storable format and reconstruct it from the stored data.
 */
public class SpawnEggMetaHandler {

    /**
     * Serializes the custom entity type of a {@link SpawnEggMeta} into a map.
     * If the provided {@link ItemMeta} is not an instance of {@link SpawnEggMeta},
     * or if the spawn egg does not have a custom entity type, the method returns without modifying the map.
     *
     * @param parent   The {@link ItemMetaSerializer} used for additional serialization logic.
     * @param meta     The {@link ItemMeta} to serialize, expected to be a {@link SpawnEggMeta}.
     * @param itemData The map where the serialized data will be stored.
     * @throws Exception If an error occurs during serialization.
     */
    public static void serialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!(meta instanceof SpawnEggMeta spawnEggMeta))
            return;

        if (spawnEggMeta.getSpawnedEntity() == null)
            return;

        if (spawnEggMeta.getCustomSpawnedType() == null)
            return;

        itemData.put("customEntityType", spawnEggMeta.getCustomSpawnedType().getKey().getKey());
    }

    /**
     * Deserializes the custom entity type from a map into a {@link SpawnEggMeta}.
     * If the provided {@link ItemMeta} is not an instance of {@link SpawnEggMeta},
     * or if the map does not contain valid custom entity type data, the method returns without modifying the meta.
     *
     * @param parent   The {@link ItemMetaSerializer} used for additional deserialization logic.
     * @param meta     The {@link ItemMeta} to populate, expected to be a {@link SpawnEggMeta}.
     * @param itemData The map containing the serialized custom entity type data.
     * @throws Exception If the custom entity type data is invalid or if an error occurs during deserialization.
     */
    public static void deserialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!(meta instanceof SpawnEggMeta spawnEggMeta))
            return;

        if (!itemData.containsKey("customEntityType")) {
            return;
        }

        String entityType = (String) itemData.get("customEntityType");
        var key = NamespacedKey.fromString(entityType);
        if (key != null)
            spawnEggMeta.setCustomSpawnedType(RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE).get(key));
    }
}