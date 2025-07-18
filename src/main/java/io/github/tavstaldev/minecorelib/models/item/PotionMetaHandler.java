package io.github.tavstaldev.minecorelib.models.item;

import io.github.tavstaldev.minecorelib.utils.ColorUtils;
import io.github.tavstaldev.minecorelib.utils.ItemMetaSerializer;
import io.github.tavstaldev.minecorelib.utils.TypeUtils;
import io.github.tavstaldev.minecorelib.utils.VersionUtils;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

/**
 * A handler for serializing and deserializing the metadata of a {@link PotionMeta}.
 * This class provides static methods to convert the color, base potion type, and custom effects
 * of a potion into a storable format and reconstruct them from the stored data.
 */
public class PotionMetaHandler {

    /**
     * Serializes the metadata of a {@link PotionMeta} into a map.
     * If the provided {@link ItemMeta} is not an instance of {@link PotionMeta},
     * the method returns without modifying the map.
     *
     * @param parent   The {@link ItemMetaSerializer} used for additional serialization logic.
     * @param meta     The {@link ItemMeta} to serialize, expected to be a {@link PotionMeta}.
     * @param itemData The map where the serialized data will be stored.
     * @throws Exception If an error occurs during serialization.
     */
    public static void serialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!(meta instanceof PotionMeta potionMeta))
            return;

        // Custom Potion Name
        if (VersionUtils.isAtLeast(1, 21, 4)) {
            if (potionMeta.hasCustomPotionName()) {
                itemData.put("customPotionName", potionMeta.getCustomPotionName());
            }
        } else {
            //noinspection removal
            if (potionMeta.hasCustomName()) {
                //noinspection removal
                itemData.put("customPotionName", potionMeta.getCustomName());
            }
        }


        // Color
        if (potionMeta.hasColor() && potionMeta.getColor() != null) {
            itemData.put("color", ColorUtils.serializeColor(potionMeta.getColor()));
        }

        // Base Potion Type
        if (potionMeta.getBasePotionType() != null) {
            itemData.put("basePotionType", potionMeta.getBasePotionType().getKey().getKey());
        }

        // Custom Effects
        if (!potionMeta.getCustomEffects().isEmpty()) {
            Map<String, Object> effects = new HashMap<>();
            for (var customEffect : potionMeta.getCustomEffects()) {
                effects.put(customEffect.getType().getKey().getKey(), customEffect.serialize());
            }
            itemData.put("customEffects", effects);
        }
    }

    /**
     * Deserializes the metadata from a map into a {@link PotionMeta}.
     * If the provided {@link ItemMeta} is not an instance of {@link PotionMeta},
     * or if the map does not contain valid potion data, the method returns without modifying the meta.
     *
     * @param parent   The {@link ItemMetaSerializer} used for additional deserialization logic.
     * @param meta     The {@link ItemMeta} to populate, expected to be a {@link PotionMeta}.
     * @param itemData The map containing the serialized potion metadata.
     * @throws Exception If the potion data is invalid or if an error occurs during deserialization.
     */
    public static void deserialize(ItemMetaSerializer parent, ItemMeta meta, Map<String, Object> itemData) throws Exception {
        if (!(meta instanceof PotionMeta potionMeta))
            return;

        // Custom Potion Name
        if (itemData.containsKey("customPotionName")) {
            String customPotionName = (String)itemData.get("customPotionName");
            if (VersionUtils.isAtLeast(1, 21, 4)) {
                potionMeta.setCustomPotionName(customPotionName);
            } else {
                //noinspection removal
                potionMeta.setCustomName(customPotionName);
            }
        }

        // Color
        if (itemData.containsKey("color")) {
            String rawColor = (String) itemData.get("color");
            potionMeta.setColor(ColorUtils.deserializeColor(rawColor));
        }

        // Base Potion Type
        if (itemData.containsKey("basePotionType")) {
            String potion = (String) itemData.get("basePotionType");
            var potionKey = NamespacedKey.fromString(potion);
            if (potionKey != null) {
                potionMeta.setBasePotionType(RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.POTION).get(potionKey));
            }
        }

        // Custom Effects
        if (itemData.containsKey("customEffects")) {
            Map<String, Object> effects = TypeUtils.castAsMap(itemData.get("customEffects"), null);
            if (effects == null)
                return;

            for (var entry : effects.entrySet()) {
                var effectKey = NamespacedKey.fromString(entry.getKey());
                if (effectKey == null)
                    continue;

                Map<String, Object> data = TypeUtils.castAsMap(entry.getValue(), null);
                if (data == null)
                    continue;

                PotionEffectType type = RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.MOB_EFFECT).get(effectKey);
                if (type == null)
                    continue;

                int duration = (int) data.getOrDefault("duration", 200);
                int amplifier = (int) data.getOrDefault("amplifier", 0);
                boolean ambient = (boolean) data.getOrDefault("ambient", false);
                boolean particles = (boolean) data.getOrDefault("particles", true);
                potionMeta.addCustomEffect(new PotionEffect(type, duration, amplifier, ambient, particles), true);
            }
        }
    }
}