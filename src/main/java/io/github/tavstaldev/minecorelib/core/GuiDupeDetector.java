package io.github.tavstaldev.minecorelib.core;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

public class GuiDupeDetector implements Listener {
    private static final String NAMESPACE = "io.github.tavstaldev.minecorelib";
    private static final String KEY = "dupe_protected";
    private static final NamespacedKey NAMESPACED_KEY = new NamespacedKey(NAMESPACE, KEY);
    public static NamespacedKey getDupeProtectedKey() { return NAMESPACED_KEY; }

    private static final Set<String> registeredPlugins = new HashSet<>(); // String is the name of the plugin
    private static final Object lock = new Object();
    private static GuiDupeDetector instance;
    private static Plugin primaryPlugin;

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

    public boolean isDuped(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null)
            return false;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(NAMESPACED_KEY, PersistentDataType.BOOLEAN))
            return false;

        return Boolean.TRUE.equals(container.get(NAMESPACED_KEY, PersistentDataType.BOOLEAN));
    }

    @EventHandler
    public void onItemPickup(InventoryPickupItemEvent event) {
        if (primaryPlugin == null)
            return;
        var itemStack = event.getItem().getItemStack();
        if (!isDuped(itemStack))
            return;

        event.setCancelled(true);
        event.getInventory().remove(itemStack);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (primaryPlugin == null)
            return;
        var itemStack = event.getEntity().getItemStack();
        if (!isDuped(itemStack))
            return;

        event.setCancelled(true);
        event.getEntity().remove();
    }

    // WORKS
    @EventHandler
    public void onItemHover(InventoryClickEvent event) {
        if (primaryPlugin == null)
            return;
        var itemStack = event.getCurrentItem();
        if (itemStack == null)
            return;
        if (!isDuped(itemStack))
            return;

        event.setCancelled(true);
        event.getWhoClicked().getInventory().remove(itemStack);
    }

    // WORKS
    @EventHandler
    public void  onDropItem(PlayerDropItemEvent event) {
        if (primaryPlugin == null)
            return;
        Player player = event.getPlayer();
        if (!isDuped(event.getItemDrop().getItemStack()))
            return;

        event.setCancelled(true);
        event.getItemDrop().remove();
        Bukkit.getScheduler().runTaskLater(primaryPlugin, () -> {
            player.getInventory().remove(event.getItemDrop().getItemStack());
        }, 2L);
    }

    // WORKS
    @EventHandler
    public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
        if (primaryPlugin == null)
            return;
        Player player = event.getPlayer();
        if (!isDuped(event.getItemInHand()))
            return;

        event.setCancelled(true);
        player.getInventory().remove(event.getItemInHand());
    }
}
