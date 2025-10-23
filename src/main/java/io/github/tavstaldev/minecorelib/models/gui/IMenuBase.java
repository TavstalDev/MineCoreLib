package io.github.tavstaldev.minecorelib.models.gui;

import com.samjakob.spigui.menu.SGMenu;

public interface IMenuBase {
    void open();

    void close();

    SGMenu createMenu();

    void refresh();
}
