package io.github.tavstaldev.minecorelib.core;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

/**
 * A listener class that detects and prevents duplication of items in a Minecraft server.
 * It uses a persistent data key to mark items as "dupe protected" and cancels events
 * involving such items.
 */
public class GuiDupeDetector implements Listener {
    private static final String NAMESPACE = "io.github.tavstaldev.minecorelib";
    private static final String KEY = "dupe_protected";
    private static final NamespacedKey NAMESPACED_KEY = new NamespacedKey(NAMESPACE, KEY);

    /**
     * Retrieves the NamespacedKey used to mark items as "dupe protected".
     *
     * @return The NamespacedKey for dupe protection.
     */
    public static NamespacedKey getDupeProtectedKey() { return NAMESPACED_KEY; }

    private static final Set<String> registeredPlugins = new HashSet<>(); // String is the name of the plugin
    private static final Object lock = new Object();
    private static GuiDupeDetector instance;
    private static Plugin primaryPlugin;

    /**
     * Registers a plugin with the GuiDupeDetector.
     * If the plugin is already registered or another plugin is the primary plugin, registration fails.
     *
     * @param plugin The plugin to register.
     * @return True if registration is successful, false otherwise.
     */
    @SuppressWarnings("UnstableApiUsage")
    public static boolean register(Plugin plugin) {
        synchronized (lock) {
            if (primaryPlugin != null && primaryPlugin.isEnabled()) {
                if (registeredPlugins.contains(plugin.getPluginMeta().getName())) {
                    return false;
                }
                registeredPlugins.add(plugin.getPluginMeta().getName());
                return false;
            }

            if (instance != null && (primaryPlugin == null || !primaryPlugin.isEnabled())) {
                registeredPlugins.add(plugin.getPluginMeta().getName());
                primaryPlugin = plugin;
                plugin.getServer().getPluginManager().registerEvents(instance, plugin);
                return true;
            }

            instance = new GuiDupeDetector();
            primaryPlugin = plugin;
            plugin.getServer().getPluginManager().registerEvents(instance, plugin);
            registeredPlugins.add(plugin.getPluginMeta().getName());
            return true;
        }
    }

    /**
     * Unregisters a plugin from the GuiDupeDetector.
     * If the plugin is the primary plugin, attempts to reassign the primary plugin to another registered plugin.
     *
     * @param plugin The plugin to unregister.
     * @return True if unregistration is successful, false otherwise.
     */
    @SuppressWarnings({"UnstableApiUsage", "UnusedReturnValue"})
    public static boolean unregister(Plugin plugin) {
        synchronized (lock) {
            if (!registeredPlugins.contains(plugin.getPluginMeta().getName())) {
                return false;
            }
            registeredPlugins.remove(plugin.getPluginMeta().getName());

            if (plugin == primaryPlugin) {
                for (Plugin registeredPlugin : plugin.getServer().getPluginManager().getPlugins()) {
                    if (!registeredPlugin.isEnabled())
                        continue;

                    if (registeredPlugin.getPluginMeta().getName().equals(plugin.getPluginMeta().getName()))
                        continue;

                    if (!registeredPlugins.contains(registeredPlugin.getPluginMeta().getName()))
                        continue;

                    primaryPlugin = null;
                    if (register(registeredPlugin))
                        break;
                }
            }
            return true;
        }
    }

    /**
     * Checks if an item is marked as "dupe protected".
     *
     * @param itemStack The item to check.
     * @return True if the item is dupe protected, false otherwise.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isDuped(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null)
            return false;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(NAMESPACED_KEY, PersistentDataType.BOOLEAN))
            return false;

        return Boolean.TRUE.equals(container.get(NAMESPACED_KEY, PersistentDataType.BOOLEAN));
    }

    /**
     * Event handler for when an inventory picks up an item.
     * Cancels the event if the item is dupe protected.
     *
     * @param event The InventoryPickupItemEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onItemPickup(InventoryPickupItemEvent event) {
        if (primaryPlugin == null)
            return;
        ItemStack itemStack = event.getItem().getItemStack();
        if (itemStack.getType().isAir()) {
            return;
        }

        if (!isDuped(itemStack))
            return;

        event.setCancelled(true);
        event.getInventory().remove(itemStack);
    }

    /**
     * Event handler for when an item spawns in the world.
     * Cancels the event if the item is dupe protected.
     *
     * @param event The ItemSpawnEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (primaryPlugin == null)
            return;
        ItemStack itemStack = event.getEntity().getItemStack();
        if (itemStack.getType().isAir()) {
            return;
        }

        if (!isDuped(itemStack))
            return;

        event.setCancelled(true);
        event.getEntity().remove();
    }

    /**
     * Event handler for when a player hovers over an item in their inventory.
     * Cancels the event if the item is dupe protected.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onItemHover(InventoryClickEvent event) {
        if (primaryPlugin == null)
            return;
        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null || itemStack.getType().isAir()) {
            return;
        }
        if (!isDuped(itemStack))
            return;

        event.setCancelled(true);
        event.getWhoClicked().getInventory().remove(itemStack);
    }

    /**
     * Event handler for when a player drops an item.
     * Cancels the event if the item is dupe protected.
     *
     * @param event The PlayerDropItemEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDropItem(PlayerDropItemEvent event) {
        if (primaryPlugin == null)
            return;

        ItemStack itemStack = event.getItemDrop().getItemStack();
        if (itemStack.getType().isAir()) {
            return;
        }

        if (!isDuped(itemStack))
            return;

        event.getItemDrop().remove();
    }

    /**
     * Event handler for when a player places a block.
     * Cancels the event if the block is dupe protected.
     *
     * @param event The BlockPlaceEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (primaryPlugin == null)
            return;
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItemInHand();
        if (itemStack.getType().isAir()) {
            return;
        }

        if (!isDuped(itemStack))
            return;

        event.setCancelled(true);
        player.getInventory().remove(itemStack);
    }
}
