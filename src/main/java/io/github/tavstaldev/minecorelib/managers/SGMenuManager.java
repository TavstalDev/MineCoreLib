package io.github.tavstaldev.minecorelib.managers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.samjakob.spigui.SpiGUI;
import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.models.gui.MenuBase;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class SGMenuManager extends MenuManagerBase {
    private final PluginLogger _logger = plugin.getCustomLogger().withModule(this.getClass());
    private final SpiGUI _spiGUI;
    private final HashMap<String, MenuBase> _registeredMenus = new HashMap<>();
    private final Cache<UUID, Map<String, SGMenu>> _openMenus = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(10_000)
            .weakValues()
            .build();
    private final HashSet<UUID> _playersWithOpenMenus = new HashSet<>();

    public SGMenuManager(PluginBase plugin) {
        super(plugin);
        _spiGUI = new SpiGUI(plugin);
    }

    @Override
    public void open(Player player, String menuId) {
        UUID playerId = player.getUniqueId();
        Map<String, SGMenu> playerMenus = _openMenus.getIfPresent(playerId);
        if (playerMenus != null && playerMenus.containsKey(menuId)) {
            SGMenu existingMenu = playerMenus.get(menuId);
            player.openInventory(existingMenu.getInventory());
            _playersWithOpenMenus.add(playerId);
            return;
        }
        else if (playerMenus == null) {
            playerMenus = new HashMap<>();
        }

        MenuBase menuBase = _registeredMenus.get(menuId);
        if (menuBase == null) {
            return;
        }
        SGMenu menu = menuBase.create(player);
        if (menu == null) {
            _logger.error("Failed to create menu with ID: " + menuId + " for player: " + player.getName());
            return;
        }
        playerMenus.put(menuId, menu);
        _openMenus.put(playerId, playerMenus);
        player.openInventory(menu.getInventory());
        _playersWithOpenMenus.add(playerId);
    }

    @Override
    public void close(Player player, boolean openingAnotherMenu) {
        player.closeInventory();
        if (openingAnotherMenu)
            return;
        _playersWithOpenMenus.remove(player.getUniqueId());
    }

    @Override
    public void refresh(Player player, String menuId) {
        UUID playerId = player.getUniqueId();
        Map<String, SGMenu> playerMenus = _openMenus.getIfPresent(playerId);
        if (playerMenus == null || !playerMenus.containsKey(menuId))
            return;

        SGMenu menu = playerMenus.get(menuId);
        MenuBase menuBase = _registeredMenus.get(menuId);
        if (menuBase == null)
            return;

        menuBase.refresh(player, menu);
    }

    @Override
    public boolean hasMenuOpen(Player player) {
        return _playersWithOpenMenus.contains(player.getUniqueId());
    }
}
