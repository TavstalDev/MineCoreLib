package io.github.tavstaldev.minecorelib.utils;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

/**
 * Utility class for managing and displaying BossBars to players.
 */
public class BossBarUtils {
    // A map to store BossBars by their unique IDs.
    private static final HashMap<String, BossBar> bossBars = new HashMap<>();

    /**
     * Ensures that a BossBar with the given ID exists. If it doesn't, creates a new one.
     *
     * @param id The unique identifier for the BossBar.
     * @return The BossBar associated with the given ID.
     */
    private static BossBar ensureBossBarExists(String id) {
        if (!bossBars.containsKey(id)) {
            BossBar bossBar = BossBar.bossBar(
                    Component.text(""),
                    1.0f,
                    BossBar.Color.BLUE,
                    BossBar.Overlay.PROGRESS
            );
            bossBars.put(id, bossBar);
            return bossBar;
        }
        return bossBars.get(id);
    }

    /**
     * Displays the BossBar with the given ID to a specific player.
     *
     * @param id The unique identifier for the BossBar.
     * @param player The player to whom the BossBar will be shown.
     */
    public static void showFor(String id, Player player) {
        BossBar bossBar = ensureBossBarExists(id);
        bossBar.addViewer(player);
    }

    /**
     * Hides the BossBar with the given ID from a specific player.
     *
     * @param id The unique identifier for the BossBar.
     * @param player The player from whom the BossBar will be hidden.
     */
    public static void hideFor(String id, Player player) {
        BossBar bossBar = ensureBossBarExists(id);
        bossBar.removeViewer(player);
    }

    /**
     * Hides all BossBars from all players.
     */
    public static void hideAll() {
        for (BossBar bossBar : bossBars.values()) {
            bossBar.viewers().forEach(x -> {
                if (x instanceof Player player)
                    bossBar.removeViewer(player);
            });
        }
    }

    /**
     * Clears all BossBars by first hiding them from all players
     * and then removing them from the internal storage.
     */
    public static void clearAll() {
        hideAll();
        bossBars.clear();
    }

    /**
     * Displays the BossBar with the given ID to a list of players.
     * Removes any players who are no longer in the list from the BossBar's viewers.
     *
     * @param id The unique identifier for the BossBar.
     * @param players The list of players to whom the BossBar will be shown.
     */
    public static void showFor(String id, List<Player> players) {
        BossBar bossBar = ensureBossBarExists(id);
        bossBar.viewers().forEach(x -> {
            if (x instanceof Player player && !players.contains(player)) {
                bossBar.removeViewer(player);
            }
        });
        for (Player player : players) {
            bossBar.addViewer(player);
        }
    }

    /**
     * Hides the BossBar with the given ID from a list of players.
     *
     * @param id The unique identifier for the BossBar.
     * @param players The list of players from whom the BossBar will be hidden.
     */
    public static void hideFor(String id, List<Player> players) {
        BossBar bossBar = ensureBossBarExists(id);
        for (Player player : players) {
            bossBar.removeViewer(player);
        }
    }

    /**
     * Updates the progress of the BossBar with the given ID.
     *
     * @param id The unique identifier for the BossBar.
     * @param progress The new progress value (0.0 to 1.0).
     */
    public static void updateProgress(String id, float progress) {
        BossBar bossBar = ensureBossBarExists(id);
        bossBar.progress(progress);
    }

    /**
     * Updates the title of the BossBar with the given ID.
     *
     * @param id The unique identifier for the BossBar.
     * @param title The new title for the BossBar, with color codes translated.
     */
    public static void updateTitle(String id, String title) {
        BossBar bossBar = ensureBossBarExists(id);
        bossBar.name(ChatUtils.translateColors(title, true));
    }
}
