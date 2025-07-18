package io.github.tavstaldev.minecorelib.models.item;

import io.github.tavstaldev.minecorelib.utils.ItemMetaSerializer;
import io.github.tavstaldev.minecorelib.utils.TypeUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A handler for serializing and deserializing the metadata of a {@link BookMeta}.
 * This class provides static methods to convert the title, author, and pages of a book
 * into a storable format and reconstruct them from the stored data.
 */
public class BookMetaHandler {

    /**
     * Serializes the metadata of a {@link BookMeta} into a map.
     * If the provided {@link ItemMeta} is not an instance of {@link BookMeta},
     * the method returns without modifying the map.
     *
     * @param parent   The {@link ItemMetaSerializer} used for additional serialization logic.
     * @param meta     The {@link ItemMeta} to serialize, expected to be a {@link BookMeta}.
     * @param itemData The map where the serialized data will be stored.
     * @throws Exception If an error occurs during serialization.
     */
    public static void serialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!(meta instanceof BookMeta bookMeta))
            return;

        itemData.put("title", bookMeta.getTitle());
        itemData.put("author", bookMeta.getAuthor());
        List<String> pages = new ArrayList<>();
        for (Component page : bookMeta.pages()) {
            pages.add(GsonComponentSerializer.gson().serialize(page));
        }
        itemData.put("pages", pages);
    }

    /**
     * Deserializes the metadata from a map into a {@link BookMeta}.
     * If the provided {@link ItemMeta} is not an instance of {@link BookMeta},
     * or if the map does not contain valid book data, the method returns without modifying the meta.
     *
     * @param parent   The {@link ItemMetaSerializer} used for additional deserialization logic.
     * @param meta     The {@link ItemMeta} to populate, expected to be a {@link BookMeta}.
     * @param itemData The map containing the serialized book metadata.
     * @throws Exception If the book data is invalid or if an error occurs during deserialization.
     */
    public static void deserialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!(meta instanceof BookMeta bookMeta))
            return;

        if (itemData.containsKey("title"))
            bookMeta.setTitle((String) itemData.get("title"));
        if (itemData.containsKey("author"))
            bookMeta.setAuthor((String) itemData.get("author"));
        if (itemData.containsKey("pages")) {
            var rawPages = itemData.get("pages");
            List<String> pages = TypeUtils.castAsList(rawPages, null);
            if (pages == null) {
                throw new IllegalArgumentException("Invalid pages data: " + rawPages);
            }
            for (String page : pages) {
                bookMeta.addPages(GsonComponentSerializer.gson().deserialize(page));
            }
        }
    }
}