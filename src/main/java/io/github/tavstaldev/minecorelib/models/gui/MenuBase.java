package io.github.tavstaldev.minecorelib.models.gui;

import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.config.ConfigurationBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;

public abstract class MenuBase extends ConfigurationBase implements IMenuBase  {
    protected final PluginBase plugin;
    protected final PluginLogger logger;
    protected final PluginTranslator translator;

    public MenuBase(PluginBase plugin, String fileName) {
        super(plugin, "menus/" + fileName, null);
        this.plugin = plugin;
        this.logger = plugin.getCustomLogger();
        this.translator = plugin.getTranslator();
    }

    /* PLAN

        The menu base should contain mostly common functionality for menus,
        A menu item should look like the following:



        item:
          material: DIAMOND_SWORD OR headTexture
          title: "&aMy Item" OR "item.title" (from translator)
          lore: list of lines OR "item.lore" (from translator), for easier implementation it probably should be a single string key
          slot: 10 - fix slot of the item
          slots: list of slots - alternative to slot, for multiple slots
          action: NONE, CLOSE, BACK, COMMAND, OPEN - etc... but I do not plan to hardcode too many actions here, just the basic ones, probably for complex actions will be a just a placeholder or something
          command: "/say hello" - only if action is COMMAND

        Also for dynamic menus, there should be way to set the slots that can be filled dynamically
        They probably should be just defined like above, but only support translator keys, and it should support only slots, no fixed slot


     */
}
