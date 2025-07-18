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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * A handler for serializing and deserializing the metadata of a {@link PotionMeta}.
 * This class provides static methods to convert the color, base potion type, and custom effects
 * of a potion into a storable format and reconstruct them from the stored data.
 */
public class PotionMetaHandler {
    private static boolean _initialized;
    private static Method hasCustomNameMethod;
    private static Method getCustomNameMethod;
    private static Method setCustomNameMethod;
    private static boolean doesCustomNameMethodWork = true;

    /**
     * Ensures that the reflection methods for handling custom potion names are initialized.
     * Depending on the server version, it uses different method names for compatibility.
     */
    private static void checkMethods() {
        if (_initialized) return;

        try {
            boolean isNewVersion = VersionUtils.isAtLeast(1, 21, 4);
            if (isNewVersion) {
                hasCustomNameMethod = PotionMeta.class.getMethod("hasCustomPotionName");
                getCustomNameMethod = PotionMeta.class.getMethod("getCustomPotionName");
                setCustomNameMethod = PotionMeta.class.getMethod("setCustomPotionName", String.class);
            }
            else {
                hasCustomNameMethod = PotionMeta.class.getMethod("hasCustomName");
                getCustomNameMethod = PotionMeta.class.getMethod("getCustomName");
                setCustomNameMethod = PotionMeta.class.getMethod("setCustomName", String.class);
            }
        } catch (NoSuchMethodException ignored) {
            doesCustomNameMethodWork = false;
        }

        _initialized = true;
    }

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

        // Ensure methods are initialized
        checkMethods();

        // Custom Potion Name
        if (doesCustomNameMethodWork) {
            if ((boolean) hasCustomNameMethod.invoke(potionMeta)) {
                String customPotionName = (String) getCustomNameMethod.invoke(potionMeta);
                if (customPotionName != null && !customPotionName.isEmpty()) {
                    itemData.put("customPotionName", customPotionName);
                }
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

        // Ensure methods are initialized
        checkMethods();

        // Custom Potion Name
        if (itemData.containsKey("customPotionName") && doesCustomNameMethodWork) {
            String customPotionName = (String)itemData.get("customPotionName");
            setCustomNameMethod.invoke(potionMeta, customPotionName);
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