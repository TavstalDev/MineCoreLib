package io.github.tavstaldev.minecorelib.models.item;

import io.github.tavstaldev.minecorelib.utils.ItemMetaSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.net.URL;
import java.util.Map;
import java.util.UUID;

/**
 * A handler for serializing and deserializing the metadata of a {@link SkullMeta}.
 * This class provides static methods to convert the owner and player profile
 * of a skull item into a storable format and reconstruct them from the stored data.
 */
public class SkullMetaHandler {

    /**
     * Serializes the metadata of a {@link SkullMeta} into a map.
     * If the provided {@link ItemMeta} is not an instance of {@link SkullMeta},
     * the method returns without modifying the map.
     *
     * @param parent   The {@link ItemMetaSerializer} used for additional serialization logic.
     * @param meta     The {@link ItemMeta} to serialize, expected to be a {@link SkullMeta}.
     * @param itemData The map where the serialized data will be stored.
     * @throws Exception If an error occurs during serialization.
     */
    public static void serialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!(meta instanceof SkullMeta skullMeta))
            return;

        if (skullMeta.hasOwner() && skullMeta.getOwningPlayer() != null)
            itemData.put("owner", skullMeta.getOwningPlayer().getUniqueId());

        if (skullMeta.getPlayerProfile() != null) {
            var profile = skullMeta.getPlayerProfile();
            if (profile.hasTextures())
                itemData.put("profileUrl", profile.getTextures().getSkin());
            itemData.put("profile", profile.getId());
        }
    }

    /**
     * Deserializes the metadata from a map into a {@link SkullMeta}.
     * If the provided {@link ItemMeta} is not an instance of {@link SkullMeta},
     * the method returns without modifying the meta.
     *
     * @param parent   The {@link ItemMetaSerializer} used for additional deserialization logic.
     * @param meta     The {@link ItemMeta} to populate, expected to be a {@link SkullMeta}.
     * @param itemData The map containing the serialized metadata.
     * @throws Exception If an error occurs during deserialization.
     */
    public static void deserialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!(meta instanceof SkullMeta skullMeta))
            return;

        if (itemData.containsKey("owner")) {
            var player = Bukkit.getOfflinePlayer((UUID) itemData.get("owner"));
            skullMeta.setOwningPlayer(player);
        }

        // Restore player profile (for custom skins)
        if (itemData.containsKey("profile")) {
            UUID profileUUID = (UUID) itemData.get("profile");
            var profile = Bukkit.createProfile(profileUUID);
            var textures = profile.getTextures();
            if (itemData.containsKey("profileUrl")) {
                textures.setSkin((URL) itemData.get("profileUrl"));
                profile.setTextures(textures);
            }
            skullMeta.setPlayerProfile(profile);
        }
    }
}