package io.github.tavstaldev.minecorelib.managers;

import io.github.tavstaldev.minecorelib.PluginBase;
import org.bukkit.entity.Player;

public abstract class MenuManagerBase {

    protected PluginBase plugin;

    public MenuManagerBase(PluginBase plugin) {
        this.plugin = plugin;
    }

    public abstract void open(Player player, String menuId);

    public abstract void close(Player player, boolean openingAnotherMenu);

    public abstract void refresh(Player player, String menuId);

    public abstract boolean hasMenuOpen(Player player);
}
