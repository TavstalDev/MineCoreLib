package io.github.tavstaldev.minecorelib.managers;

import com.samjakob.spigui.SpiGUI;
import io.github.tavstaldev.minecorelib.PluginBase;

public class SGMenuManager extends MenuManagerBase {
    private final SpiGUI _spiGUI;

    public SGMenuManager(PluginBase plugin) {
        super(plugin);
        _spiGUI = new SpiGUI(plugin);
    }

    // TODO: Implement open, close, and refresh methods using SpiGUI
}
