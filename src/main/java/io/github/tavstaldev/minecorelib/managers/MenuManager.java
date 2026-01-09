package io.github.tavstaldev.minecorelib.managers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.samjakob.spigui.SpiGUI;
import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.models.gui.MenuBase;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;

public class MenuManager {
    private final PluginBase plugin;
    private final PluginLogger logger;
    private final SpiGUI _spiGUI;
    public SpiGUI getSpiGUI() {
        return _spiGUI;
    }
    private final HashMap<String, MenuBase> _registeredMenus = new HashMap<>();
    private final Cache<UUID, Map<String, SGMenu>> _menuCache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(10_000)
            .weakValues()
            .build();
    private final HashMap<UUID, String> _playersWithOpenMenus = new HashMap<>();

    public MenuManager(PluginBase plugin) {
        this.plugin = plugin;
        this.logger = plugin.getCustomLogger().withModule(this.getClass());
        _spiGUI = new SpiGUI(plugin);
    }

    public void register(String id, MenuBase menuBase) {
        menuBase.load();
        _registeredMenus.put(id, menuBase);
        logger.debug("Registered menu with ID: " + id);
    }

    public void unregister(String id) {
        _registeredMenus.remove(id);
        logger.debug("Unregistered menu with ID: " + id);
    }

    public void open(Player player, String menuId) {
        UUID playerId = player.getUniqueId();
        MenuBase menuBase = _registeredMenus.get(menuId);
        if (menuBase == null)
            return;

        Map<String, SGMenu> playerMenus = _menuCache.getIfPresent(playerId);
        if (playerMenus != null && playerMenus.containsKey(menuId)) {
            SGMenu existingMenu = playerMenus.get(menuId);
            player.openInventory(existingMenu.getInventory());
            menuBase.onOpen(player);
            _playersWithOpenMenus.put(playerId, menuId);
            return;
        }
        else if (playerMenus == null) {
            playerMenus = new HashMap<>();
        }

        SGMenu menu = menuBase.create(player);
        if (menu == null) {
            logger.error("Failed to create menu with ID: " + menuId + " for player: " + player.getName());
            return;
        }
        playerMenus.put(menuId, menu);
        _menuCache.put(playerId, playerMenus);
        player.openInventory(menu.getInventory());
        menuBase.onOpen(player);
        _playersWithOpenMenus.put(playerId, menuId);
    }

    public void close(Player player, boolean openingAnotherMenu) {
        player.closeInventory();
        String openedId = _playersWithOpenMenus.get(player.getUniqueId());
        if (openedId != null) {
            MenuBase menuBase = _registeredMenus.get(openedId);
            if (menuBase != null) {
                menuBase.onClose(player);
            }
        }

        if (openingAnotherMenu)
            return;
        _playersWithOpenMenus.remove(player.getUniqueId());
    }

    public void closeAll() {
        for (UUID playerId : _playersWithOpenMenus.keySet()) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                close(player, false);
            }
        }
        _playersWithOpenMenus.clear();
    }

    public void invalidateCache(Player player) {
        _menuCache.invalidate(player.getUniqueId());
    }

    public void invalidateAllCache() {
        _menuCache.invalidateAll();
    }

    public void refresh(Player player, String menuId) {
        UUID playerId = player.getUniqueId();
        Map<String, SGMenu> playerMenus = _menuCache.getIfPresent(playerId);
        if (playerMenus == null || !playerMenus.containsKey(menuId))
            return;

        SGMenu menu = playerMenus.get(menuId);
        MenuBase menuBase = _registeredMenus.get(menuId);
        if (menuBase == null)
            return;

        menuBase.refresh(player, menu);
    }

    public @Nullable SGMenu getMenu(Player player, String menuId) {
        Map<String, SGMenu> playerMenus = _menuCache.getIfPresent(player.getUniqueId());
        if (playerMenus != null && playerMenus.containsKey(menuId)) {
            return playerMenus.get(menuId);
        }
        return null;
    }

    public boolean hasMenuOpen(Player player) {
        return _playersWithOpenMenus.containsKey(player.getUniqueId());
    }

    public void executeCommand(Player player, String menuId, String command) {
        MenuBase menuBase = _registeredMenus.get(menuId);
        if (menuBase == null)
            return;
        menuBase.executeCommand(player, command);
    }
}
