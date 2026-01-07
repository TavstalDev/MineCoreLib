package io.github.tavstaldev.minecorelib.models.gui;

import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.config.ConfigurationBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class MenuBase extends ConfigurationBase  {
    protected final PluginBase plugin;
    protected final PluginLogger logger;
    protected final PluginTranslator translator;
    protected String menuTitle;
    protected boolean isMenuTitleTranslated;
    protected int menuSize;
    protected Set<MenuButton> menuButtons;

    public MenuBase(PluginBase plugin, String fileName) {
        super(plugin, "menus/" + fileName, null);
        this.plugin = plugin;
        this.logger = plugin.getCustomLogger();
        this.translator = plugin.getTranslator();
    }

    protected Set<MenuButton> resolveButtons(LinkedHashSet<MenuButton> defaultButtons) {
        Set<MenuButton> buttons = new LinkedHashSet<>();

        List<Map<?, ?>> buttonMap = this.getMapList("buttons");
        if (buttonMap == null || buttonMap.isEmpty()) {
            return defaultButtons;
        }
        for (Map<?, ?> actionMap : buttonMap) {
            MenuButton action = MenuButton.fromMap(actionMap);
            if (action != null) {
                buttons.add(action);
            }
        }
        return buttons;
    }
}
