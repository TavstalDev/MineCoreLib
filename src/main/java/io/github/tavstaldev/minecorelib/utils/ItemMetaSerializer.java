package io.github.tavstaldev.minecorelib.utils;

import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.models.item.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class for serializing and deserializing {@link ItemMeta} data.
 * This class provides methods to handle YAML parsing and logging for item metadata operations.
 */
public class ItemMetaSerializer {
    private final PluginLogger _logger;
    private final Yaml _yamlParser;

    /**
     * Constructs an {@link ItemMetaSerializer} instance.
     * Initializes the logger and configures the YAML parser with custom options.
     *
     * @param plugin The {@link PluginBase} instance used to retrieve the custom logger.
     */
    public ItemMetaSerializer(PluginBase plugin) {
        _logger = plugin.getCustomLogger().withModule(this.getClass());

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // Forces multi-line formatting
        options.setIndent(2);
        _yamlParser = new Yaml(options);
    }

    //#region Core Methods
    /**
     * Serializes an {@link ItemStack} into a map representation.
     * The map contains the material, amount, and metadata (e.g., display name, lore, durability, etc.) of the item.
     * If the item has specific metadata (e.g., enchantments, books, potions), it delegates serialization to the appropriate handlers.
     *
     * @param item The {@link ItemStack} to serialize.
     * @return A map containing the serialized data of the {@link ItemStack}.
     */
    public Map<String, Object> serializeItemStack(@NotNull ItemStack item) {
        Map<String, Object> itemData = new HashMap<>();
        _logger.debug("Serializing item: " + item.getType().name());
        itemData.put("material", item.getType().toString());  // Store the material (type) of the item
        itemData.put("amount", item.getAmount());  // Store the amount of the item


        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {

                // Add item meta data like display name, lore, etc. (Optional)
                var displayName = meta.displayName();
                if (displayName != null)
                    itemData.put("name", GsonComponentSerializer.gson().serialize(displayName));
                var metaLore = meta.lore();
                if (metaLore != null) {
                    List<String> lore = new ArrayList<>();
                    for (Component line : metaLore) {
                        lore.add(GsonComponentSerializer.gson().serialize(line));
                    }

                    itemData.put("lore", lore);
                }

                // Add durability
                if (meta instanceof Damageable) {
                    itemData.put("durability", ((Damageable) meta).getDamage());
                }

                // Add persistent data
                if (item.getPersistentDataContainer() != null) {
                    Map<String, Object> persistentData = new HashMap<>();
                    var data = item.getPersistentDataContainer();
                    for (var key : data.getKeys()) {
                        if (data.has(key, PersistentDataType.STRING)) {
                            persistentData.put(key.asString(),
                                    Map.of("type", "STRING",
                                   "value", data.get(key, PersistentDataType.STRING)
                                    )
                            );
                            continue;
                        }

                        if (data.has(key, PersistentDataType.INTEGER)) {
                            persistentData.put(key.asString(),
                                    Map.of("type", "INTEGER",
                                            "value", data.get(key, PersistentDataType.INTEGER)
                                    )
                            );
                            continue;
                        }

                        if (data.has(key, PersistentDataType.DOUBLE)) {
                            persistentData.put(key.asString(),
                                    Map.of("type", "DOUBLE",
                                            "value", data.get(key, PersistentDataType.DOUBLE)
                                    )
                            );
                            continue;
                        }

                        if (data.has(key, PersistentDataType.FLOAT)) {
                            persistentData.put(key.asString(),
                                    Map.of("type", "FLOAT",
                                            "value", data.get(key, PersistentDataType.FLOAT)
                                    )
                            );
                            continue;
                        }

                        if (data.has(key, PersistentDataType.LONG)) {
                            persistentData.put(key.asString(),
                                    Map.of("type", "LONG",
                                            "value", data.get(key, PersistentDataType.LONG)
                                    )
                            );
                            continue;
                        }

                        if (data.has(key, PersistentDataType.BYTE)) {
                            persistentData.put(key.asString(),
                                    Map.of("type", "BYTE",
                                            "value", data.get(key, PersistentDataType.BYTE)
                                    )
                            );
                            continue;
                        }

                        if (data.has(key, PersistentDataType.BYTE_ARRAY)) {
                            persistentData.put(key.asString(),
                                    Map.of("type", "BYTE_ARRAY",
                                            "value", data.get(key, PersistentDataType.BYTE_ARRAY)
                                    )
                            );
                            continue;
                        }

                        if (data.has(key, PersistentDataType.INTEGER_ARRAY)) {
                            persistentData.put(key.asString(),
                                    Map.of("type", "INTEGER_ARRAY",
                                            "value", data.get(key, PersistentDataType.INTEGER_ARRAY)
                                    )
                            );
                            continue;
                        }

                        if (data.has(key, PersistentDataType.LONG_ARRAY)) {
                            persistentData.put(key.asString(),
                                    Map.of("type", "LONG_ARRAY",
                                            "value", data.get(key, PersistentDataType.LONG_ARRAY)
                                    )
                            );
                            continue;
                        }

                        if (data.has(key, PersistentDataType.SHORT)) {
                            persistentData.put(key.asString(),
                                    Map.of("type", "SHORT",
                                            "value", data.get(key, PersistentDataType.SHORT)
                                    )
                            );
                            continue;
                        }

                        if (data.has(key, PersistentDataType.BOOLEAN)) {
                            persistentData.put(key.asString(),
                                    Map.of("type", "BOOLEAN",
                                            "value", data.get(key, PersistentDataType.BOOLEAN)
                                    )
                            );
                            continue;
                        }
                    }
                    if (!persistentData.isEmpty()) {
                        itemData.put("persistent-data", persistentData);
                    }
                }

                // Add nbt tags
                if (VersionUtils.isAtLeast(1,21, 5)) {
                    if (meta.hasCustomModelDataComponent()) {
                        _logger.debug("Not implemented yet: customModelDataComponent");
                        // TODO: Implement customModelDataComponent handling
                        // When more information is available, this can be implemented.
                        /*var customModelData = meta.getCustomModelDataComponent();
                        Map<String, List<?>> customModelDataMap = new HashMap<>();
                        customModelDataMap.put("colors", customModelData.getColors());
                        customModelDataMap.put("flags", customModelData.getFlags());
                        customModelDataMap.put("floats", customModelData.getFloats());
                        customModelDataMap.put("strings", customModelData.getStrings());
                        itemData.put("customModelDataComponent", customModelDataMap);*/
                    }
                }
                else {
                    //noinspection deprecation
                    if (meta.hasCustomModelData()) {
                        //noinspection deprecation
                        var customModelData = meta.getCustomModelData();
                        itemData.put("customModelData", customModelData);
                    }
                }

                String messageBase = "An error occurred while serializing %s meta: %s";
                //#region Enchants
                try {
                    EnchantmentMetaHandler.serialize(this, meta, itemData);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "enchant", metaEx.getMessage()));
                }
                //#endregion
                //#region Enchantment Storage
                try {
                    EnchantmentStorageMetaHandler.serialize(this, meta, itemData);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "enchantment storage", metaEx.getMessage()));
                }
                //#endregion
                //#region Books
                try {
                    BookMetaHandler.serialize(this, meta, itemData);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "book", metaEx.getMessage()));
                }
                //#endregion
                //#region Crossbow
                try {
                    CrossbowMetaHandler.serialize(this, meta, itemData);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "crossbow", metaEx.getMessage()));
                }
                //#endregion
                //#region Firework Effect
                try {
                    FireworkEffectMetaHandler.serialize(this, meta, itemData);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "firework effect", metaEx.getMessage()));
                }
                //#endregion
                //#region Fireworks
                try {
                    FireworkMetaHandler.serialize(this, meta, itemData);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "firework", metaEx.getMessage()));
                }
                //#endregion
                //#region Leather Armor
                try {
                    LeatherArmorMetaHandler.serialize(this, meta, itemData);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "leather armor", metaEx.getMessage()));
                }
                //#endregion
                //#region Potions
                try {
                    PotionMetaHandler.serialize(this, meta, itemData);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "potion", metaEx.getMessage()));
                }
                //#endregion
                //#region Skulls
                try {
                    SkullMetaHandler.serialize(this, meta, itemData);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "skull", metaEx.getMessage()));
                }
                //#endregion
                //#region Spawn Eggs
                try {
                    SpawnEggMetaHandler.serialize(this, meta, itemData);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "spawn egg", metaEx.getMessage()));
                }
                //#endregion
            }
        }

        return itemData;
    }

    /**
     * Deserializes an {@link ItemStack} from a map representation.
     * The map should contain the material, amount, and optionally metadata (e.g., display name, lore, durability, etc.).
     * If the map contains specific metadata (e.g., enchantments, books, potions), it delegates deserialization to the appropriate handlers.
     *
     * @param itemMap A map containing the serialized data of an {@link ItemStack}.
     * @return The deserialized {@link ItemStack}, or null if deserialization fails.
     */
    public @Nullable ItemStack deserializeItemStack(@NotNull Map<String, Object> itemMap) {
        ItemStack itemResult = null;
        try {
            String materialString = (String) itemMap.get("material");
            Material material = Material.getMaterial(materialString);  // Convert material string to Material enum
            int amount;
            if (itemMap.containsKey("amount")) {
                amount = (int) itemMap.get("amount");  // Get the amount of the item
            } else {
                amount = 1;  // Default amount if not specified
            }

            if (material != null) {
                ItemStack item = new ItemStack(material, amount);
                ItemMeta meta = item.getItemMeta();

                // if there is no name, then it does not have metadata.
                if (itemMap.containsKey("name")) {
                    var rawName = (String) itemMap.get("name");
                    if (rawName.contains("{") && rawName.contains("}")) {
                        meta.displayName(GsonComponentSerializer.gson().deserialize(rawName));
                    } else {
                        meta.displayName(ChatUtils.translateColors(rawName, true));
                    }
                }

                // Lore
                if (itemMap.containsKey("lore")) {
                    var loreData = itemMap.get("lore");
                    if (loreData instanceof List) {
                        List<String> rawRoles = TypeUtils.castAsList(loreData, null);
                        List<Component> loreList = new ArrayList<>();
                        if (rawRoles != null) {
                            for (String line : rawRoles) {
                                if (line.contains("{") && line.contains("}")) {
                                    loreList.add(GsonComponentSerializer.gson().deserialize(line));
                                } else {
                                    loreList.add(ChatUtils.translateColors(line, true));
                                }
                            }
                        }
                        meta.lore(loreList);
                    }
                }

                // Durability
                if (itemMap.containsKey("durability")) {
                    if (meta instanceof Damageable damageableMeta) {
                        int durability = 0;
                        try {
                            durability = (int) itemMap.get("durability");  // Get the durability of the item
                        } catch (ClassCastException ignored){}
                        damageableMeta.setDamage(durability);
                    }
                }

                if (itemMap.containsKey("persistent-data")) {
                    var dataContainer = meta.getPersistentDataContainer();
                    Map<String, Object> dataMap = TypeUtils.castAsMap(itemMap.get("persistent-data"), null);
                    if (dataMap != null) {
                        for (var entry : dataMap.entrySet()) {
                            var key = entry.getKey();
                            NamespacedKey namespacedKey = NamespacedKey.fromString(key);
                            if (namespacedKey == null)
                                continue;
                            var valueMap = TypeUtils.castAsMap(entry.getValue(), null);
                            if (valueMap == null || !valueMap.containsKey("type") || !valueMap.containsKey("value"))
                                continue;

                            var type = (String) valueMap.get("type");
                            var value = valueMap.get("value");
                            switch (type) {
                                case "STRING" -> {
                                    if (value instanceof String stringValue) {
                                        dataContainer.set(namespacedKey, PersistentDataType.STRING, stringValue);
                                    }
                                }
                                case "INTEGER" -> {
                                    if (value instanceof Integer intValue) {
                                        dataContainer.set(namespacedKey, PersistentDataType.INTEGER, intValue);
                                    }
                                }
                                case "DOUBLE" -> {
                                    if (value instanceof Double doubleValue) {
                                        dataContainer.set(namespacedKey, PersistentDataType.DOUBLE, doubleValue);
                                    }
                                }
                                case "FLOAT" -> {
                                    if (value instanceof Float floatValue) {
                                        dataContainer.set(namespacedKey, PersistentDataType.FLOAT, floatValue);
                                    }
                                }
                                case "LONG" -> {
                                    if (value instanceof Long longValue) {
                                        dataContainer.set(namespacedKey, PersistentDataType.LONG, longValue);
                                    }
                                }
                                case "BYTE" -> {
                                    if (value instanceof Byte byteValue) {
                                        dataContainer.set(namespacedKey, PersistentDataType.BYTE, byteValue);
                                    }
                                }
                                case "BYTE_ARRAY" -> {
                                    if (value instanceof byte[] byteArrayValue) {
                                        dataContainer.set(namespacedKey, PersistentDataType.BYTE_ARRAY, byteArrayValue);
                                    }
                                }
                                case "INTEGER_ARRAY" -> {
                                    if (value instanceof int[] intArrayValue) {
                                        dataContainer.set(namespacedKey, PersistentDataType.INTEGER_ARRAY, intArrayValue);
                                    }
                                }
                                case "LONG_ARRAY" -> {
                                    if (value instanceof long[] longArrayValue) {
                                        dataContainer.set(namespacedKey, PersistentDataType.LONG_ARRAY, longArrayValue);
                                    }
                                }
                                case "SHORT" -> {
                                    if (value instanceof Short shortValue) {
                                        dataContainer.set(namespacedKey, PersistentDataType.SHORT, shortValue);
                                    }
                                }
                                case "BOOLEAN" -> {
                                    if (value instanceof Boolean booleanValue) {
                                        dataContainer.set(namespacedKey, PersistentDataType.BOOLEAN, booleanValue);
                                    }
                                }
                            }
                        }
                    }
                }

                // customModelData
                if (itemMap.containsKey("customModelData")) {
                    if (VersionUtils.isAtLeast(1,21, 5)) {
                        _logger.debug("Not implemented yet: customModelDataComponent");
                        // TODO: Implement customModelDataComponent handling
                        // When more information is available, this can be implemented.
                        /*var customModelData = CustomModelData.customModelData()
                                .addColors()
                                .addFlags()
                                .addFloats()
                                .addStrings()
                                .build();
                        meta.setCustomModelDataComponent(customModelData);*/
                    }
                    else {
                        //noinspection deprecation
                        meta.setCustomModelData((int) itemMap.get("customModelData"));
                    }
                }

                String messageBase = "An error occurred while deserializing %s meta: %s";
                //#region Enchants
                try {
                    EnchantmentMetaHandler.deserialize(this, meta, itemMap);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "enchant", metaEx.getMessage()));
                }
                //#endregion
                //#region Books
                try {
                    BookMetaHandler.deserialize(this, meta, itemMap);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "book", metaEx.getMessage()));
                }
                //#endregion
                //#region Enchantment Storage
                try {
                    EnchantmentStorageMetaHandler.deserialize(this, meta, itemMap);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "enchantment storage", metaEx.getMessage()));
                }
                //#endregion
                //#region Crossbow
                try {
                    CrossbowMetaHandler.deserialize(this, meta, itemMap);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "crossbow", metaEx.getMessage()));
                }
                //#endregion
                //#region Firework Effect
                try {
                    FireworkEffectMetaHandler.deserialize(this, meta, itemMap);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "firework effect", metaEx.getMessage()));
                }
                //#endregion
                //#region Fireworks
                try {
                    FireworkMetaHandler.deserialize(this, meta, itemMap);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "firework", metaEx.getMessage()));
                }
                //#endregion
                //#region Leather Armor
                try {
                    LeatherArmorMetaHandler.deserialize(this, meta, itemMap);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "leather armor", metaEx.getMessage()));
                }
                //#endregion
                //#region Potions
                try {
                    PotionMetaHandler.deserialize(this, meta, itemMap);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "potion", metaEx.getMessage()));
                }
                //#endregion
                //#region Skulls
                try {
                    SkullMetaHandler.deserialize(this, meta, itemMap);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "skull", metaEx.getMessage()));
                }
                //#endregion
                //#region Spawn Eggs
                try {
                    SpawnEggMetaHandler.deserialize(this, meta, itemMap);
                }
                catch (Exception metaEx) {
                    _logger.error(String.format(messageBase, "spawn egg", metaEx.getMessage()));
                }
                //#endregion

                item.setItemMeta(meta);  // Set the meta data to the item
                itemResult = item;
            }
        } catch (Exception ex) {
            _logger.error("An error occurred while deserializing items: " + ex);
        }
        return itemResult;
    }
    //#endregion

    //#region Serialization Methods
    /**
     * Serializes a list of {@link ItemStack} objects into a list of maps.
     * Each {@link ItemStack} is converted into a map representation containing its data.
     *
     * @param items A list of {@link ItemStack} objects to serialize.
     * @return A list of maps, where each map represents the serialized data of an {@link ItemStack}.
     */
    public List<Map<String, Object>> serializeItemStackList(@NotNull List<ItemStack> items) {
        List<Map<String, Object>> itemList = new ArrayList<>();
        for (ItemStack item : items) {
            Map<String, Object> itemData = serializeItemStack(item);
            if (itemData != null) {
                itemList.add(itemData);
            }
        }
        return itemList;
    }

    /**
     * Serializes a single {@link ItemStack} into its YAML representation.
     * The YAML string contains the serialized data of the {@link ItemStack}.
     *
     * @param item The {@link ItemStack} to serialize.
     * @return A YAML string representing the serialized {@link ItemStack}, or null if serialization fails.
     */
    public @Nullable String serializeItemStackToYaml(@NotNull ItemStack item) {
        Map<String, Object> itemData = serializeItemStack(item);
        if (itemData != null) {
            return _yamlParser.dump(itemData);
        }
        return null;
    }

    /**
     * Serializes a list of {@link ItemStack} objects into a YAML representation.
     * The YAML string contains the serialized data of the list of {@link ItemStack} objects.
     *
     * @param items A list of {@link ItemStack} objects to serialize.
     * @return A YAML string representing the serialized list of {@link ItemStack} objects, or null if serialization fails.
     */
    public @Nullable String serializeItemStackListToYaml(@NotNull List<ItemStack> items) {
        List<Map<String, Object>> itemList = serializeItemStackList(items);
        if (itemList != null && !itemList.isEmpty()) {
            return _yamlParser.dump(itemList);
        }
        return null;
    }

    /**
     * Serializes a single {@link ItemStack} into a byte array.
     * The byte array contains the serialized data of the {@link ItemStack}.
     *
     * @param item The {@link ItemStack} to serialize.
     * @return A byte array representing the serialized {@link ItemStack}, or an empty array if serialization fails.
     */
    public byte[] serializeItemStackToBytes(@NotNull ItemStack item) {
        Map<String, Object> itemData = serializeItemStack(item);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(itemData);
        } catch (IOException ex) {
            _logger.error("An error occurred while serializing item: " + ex.getMessage());
        }
        return new byte[0];  // Return empty byte array if serialization fails
    }

    /**
     * Serializes a list of {@link ItemStack} objects into a byte array.
     * The byte array contains the serialized data of the list of {@link ItemStack} objects.
     *
     * @param items A list of {@link ItemStack} objects to serialize.
     * @return A byte array representing the serialized list of {@link ItemStack} objects, or an empty array if serialization fails.
     */
    public byte[] serializeItemStackListToBytes(@NotNull List<ItemStack> items) {
        var itemDataList = serializeItemStackList(items);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(itemDataList);
        } catch (IOException ex) {
            _logger.error("An error occurred while serializing items: " + ex.getMessage());
        }
        return new byte[0];  // Return empty byte array if serialization fails
    }
    //#endregion

    //#region Deserialization Methods
    /**
     * Deserializes a list of item data maps into a list of {@link ItemStack} objects.
     * Each map in the input list represents the serialized data of an item.
     *
     * @param itemList A list of maps containing serialized item data.
     * @return A list of {@link ItemStack} objects deserialized from the input data.
     */
    public List<ItemStack> deserializeItemStackList(@NotNull List<Map<String, Object>> itemList) {
        List<ItemStack> items = new ArrayList<>();
        for (Map<String, Object> itemData : itemList) {
            ItemStack item = deserializeItemStack(itemData);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Deserializes a single {@link ItemStack} from its YAML representation.
     *
     * @param yaml A YAML string containing the serialized item data.
     * @return The deserialized {@link ItemStack}, or null if deserialization fails.
     */
    public @Nullable ItemStack deserializeItemStackFromYaml(@NotNull String yaml) {
        Map<String, Object> itemData = _yamlParser.load(yaml);
        if (itemData != null) {
            return deserializeItemStack(itemData);
        }
        return null;
    }

    /**
     * Deserializes a list of {@link ItemStack} objects from their YAML representation.
     *
     * @param yaml A YAML string containing a list of serialized item data.
     * @return A list of {@link ItemStack} objects deserialized from the input YAML.
     */
    public List<ItemStack> deserializeItemStackListFromYaml(@NotNull String yaml) {
        List<Map<String, Object>> itemList = _yamlParser.load(yaml);
        if (itemList != null && !itemList.isEmpty()) {
            return deserializeItemStackList(itemList);
        }
        return new ArrayList<>();
    }

    /**
     * Deserializes a single {@link ItemStack} from a byte array.
     * The byte array must contain serialized item data in a compatible format.
     *
     * @param data A byte array containing the serialized item data.
     * @return The deserialized {@link ItemStack}, or null if deserialization fails.
     */
    public ItemStack deserializeItemStackFromBytes(byte @NotNull [] data) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
             ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {

            var streamObject = objectStream.readObject();
            if (!(streamObject instanceof Map)) {
                _logger.error("Deserialized object is not a Map.");
                return null;
            }

            Map<String, Object> itemMap = TypeUtils.castAsMap(streamObject, null);
            if (itemMap == null) {
                _logger.error("Deserialized object is not a Map<String, Object>.");
                return null;
            }

            return deserializeItemStack(itemMap);
        } catch (IOException | ClassNotFoundException ex) {
            _logger.error("An error occurred while deserializing items: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Deserializes a list of {@link ItemStack} objects from a byte array.
     * The byte array must contain serialized item data in a compatible format.
     *
     * @param data A byte array containing the serialized list of item data.
     * @return A list of {@link ItemStack} objects deserialized from the input data.
     */
    public List<ItemStack> deserializeItemStackListFromBytes(byte @NotNull [] data) {
        List<ItemStack> items = new ArrayList<>();
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
             ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {

            var streamObject = objectStream.readObject();
            if (!(streamObject instanceof List)) {
                _logger.error("Deserialized object is not a List.");
                return items;
            }

            List<Map<String, Object>> itemDataList = TypeUtils.castAsListOfMaps(streamObject, null);
            if (itemDataList == null) {
                _logger.error("Deserialized object is not a List<Map<String, Object>>.");
                return items;
            }

            items = deserializeItemStackList(itemDataList);
        } catch (IOException | ClassNotFoundException ex) {
            _logger.error("An error occurred while deserializing items: " + ex.getMessage());
        }
        return items;
    }
    //#endregion
}
