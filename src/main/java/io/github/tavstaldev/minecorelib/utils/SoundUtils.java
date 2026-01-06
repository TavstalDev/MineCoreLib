package io.github.tavstaldev.minecorelib.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

/**
 * Utility class for handling sound-related operations.
 * Provides methods to retrieve sound objects based on their names.
 */
public class SoundUtils {
    /**
     * Retrieves a sound object based on its name with default volume and pitch.
     *
     * @param name The name of the sound to retrieve.
     * @return An Optional containing the Sound object if found, or an empty Optional if not.
     */
    public static Optional<Sound> getSound(@NotNull String name) {
        return getSound(name, 1.0f, 1.0f);
    }

    /**
     * Retrieves a sound object based on its name, volume, and pitch.
     *
     * @param name   The name of the sound to retrieve.
     * @param volume The volume of the sound.
     * @param pitch  The pitch of the sound.
     * @return An Optional containing the Sound object if found, or an empty Optional if not.
     */
    public static Optional<Sound> getSound(@NotNull String name, float volume, float pitch) {
        @SuppressWarnings("PatternValidation")
        String key = name.toLowerCase(Locale.ROOT);
        try {
            // Return an empty Optional if the name is "none"
            if ("none".equalsIgnoreCase(key))
                return Optional.empty();

            @SuppressWarnings("PatternValidation")
            Key soundKey = Key.key(key);

            // Create and return the Sound object
            return Optional.of(Sound.sound(
                    soundKey,
                    Sound.Source.MASTER,
                    volume,
                    pitch
            ));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    /**
     * Plays a sound for the specified player if the sound is present.
     *
     * @param player The player for whom the sound will be played.
     * @param sound  An Optional containing the Sound object to play. If empty, no sound will be played.
     */
    public static void playSound(Player player, Optional<Sound> sound) {
        if (sound.isEmpty())
            return;
        player.playSound(sound.get());
    }
}