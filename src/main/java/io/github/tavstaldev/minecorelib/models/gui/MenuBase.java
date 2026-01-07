package io.github.tavstaldev.minecorelib.models.gui;

import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.config.ConfigurationBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;

public abstract class MenuBase extends ConfigurationBase  {
    protected final PluginBase plugin;
    protected final PluginLogger logger;
    protected final PluginTranslator translator;
    protected String menuTitle;
    protected boolean isMenuTitleTranslated;
    protected int menuSize;
    protected HashMap<String, int[]> dynamicSlots;
    protected HashSet<MenuButton> menuButtons;

    public MenuBase(PluginBase plugin, String fileName) {
        super(plugin, "menus/" + fileName, null);
        this.plugin = plugin;
        this.logger = plugin.getCustomLogger();
        this.translator = plugin.getTranslator();
    }

    protected HashMap<String, int[]> resolveDynamicSlots() {
        HashMap<String, int[]> dynamicSlots = new HashMap<>();

        ConfigurationSection section = this.getConfigurationSection("dynamicSlots");
        if (section == null) {
            return dynamicSlots;
        }
        for (String key : section.getKeys(false)) {
            List<Integer> slotList = this.getIntegerList("dynamicSlots." + key);
            int[] slots = slotList.stream().mapToInt(Integer::intValue).toArray();
            dynamicSlots.put(key, slots);
        }
        return dynamicSlots;
    }

    protected HashSet<MenuButton> resolveButtons(LinkedHashSet<MenuButton> defaultButtons) {
        HashSet<MenuButton> buttons = new LinkedHashSet<>();

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

    public abstract SGMenu create(Player player);

    public abstract void refresh(Player player, SGMenu menu);
}
