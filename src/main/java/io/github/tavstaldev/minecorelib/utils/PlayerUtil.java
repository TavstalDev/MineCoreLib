package io.github.tavstaldev.minecorelib.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Utility class for player-related operations.
 */
public class PlayerUtil {

    /**
     * Clears the inventory of the specified player, including their armor, off-hand item,
     * and any open inventory view. Updates the player's inventory afterward.
     *
     * @param player The player whose inventory will be cleared.
     */
    public static void clearInventory(Player player) {
        player.closeInventory();
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setItemInOffHand(null);
        player.updateInventory();
    }
}