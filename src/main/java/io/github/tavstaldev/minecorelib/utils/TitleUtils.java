package io.github.tavstaldev.minecorelib.utils;

import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Map;

/**
 * Utility class for sending titles to players.
 */
public class TitleUtils {

    /**
     * Sends a title and subtitle to the specified player using localized strings.
     * The title and subtitle are fetched using the provided localization key and arguments.
     * If both the title and subtitle are empty, no title is sent.
     *
     * @param player    The player to whom the title will be sent.
     * @param key       The localization key used to fetch the title and subtitle.
     * @param arguments A map of arguments to be used in the localization process. Can be null.
     */
    public static void sendTitle(PluginBase plugin, Player player, String key, Map<String, Object> arguments) {
        PluginTranslator translator = plugin.getTranslator();
        if (arguments == null) {
            arguments = Map.of();
        }

        String mainTitle = translator.localize(player, key + ".Title", arguments);
        String subTitle = translator.localize(player, key + ".Subtitle", arguments);
        if (mainTitle.isEmpty() && subTitle.isEmpty()) {
            return;
        }

        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),   // Fade In: 0.5 seconds
                Duration.ofSeconds(2),    // Stay: 2 seconds
                Duration.ofSeconds(1)     // Fade Out: 1 second
        );
        Title title = Title.title(ChatUtils.translateColors(mainTitle, true), ChatUtils.translateColors(subTitle, true), times);
        player.showTitle(title);
    }

    /**
     * Sends a title and subtitle to the specified player.
     * The title and subtitle are displayed with predefined fade-in, stay, and fade-out durations.
     * If both the title and subtitle are empty, no title is sent.
     *
     * @param player   The player to whom the title will be sent.
     * @param title    The main title text to display. Can be empty.
     * @param subTitle The subtitle text to display. Can be empty.
     */
    public static void sendTitle(Player player, String title, String subTitle) {
        if (title.isEmpty() && subTitle.isEmpty()) {
            return;
        }

        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),   // Fade In: 0.5 seconds
                Duration.ofSeconds(2),    // Stay: 2 seconds
                Duration.ofSeconds(1)     // Fade Out: 1 second
        );
        Title screenTitle = Title.title(ChatUtils.translateColors(title, true), ChatUtils.translateColors(subTitle, true), times);
        player.showTitle(screenTitle);
    }
}
