package io.github.tavstaldev.minecorelib.utils;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for type casting operations with optional logging.
 */
public class TypeUtils {

    /**
     * Attempts to cast an object to a specified target class.
     *
     * @param obj           The object to be cast.
     * @param targetClass   The target class to cast the object to.
     * @param optionalLogger An optional logger for logging errors or warnings.
     * @param <T>           The type of the target class.
     * @return The cast object if successful, or null if the cast fails.
     */
    public static <T> @Nullable T cast(@NotNull Object obj, @NotNull Class<T> targetClass, @Nullable PluginLogger optionalLogger) {
        boolean hasLogger = optionalLogger != null;

        try {
            if (targetClass.isInstance(obj)) {
                return targetClass.cast(obj);
            } else {
                if (hasLogger) {
                    optionalLogger.Error(String.format("Cannot cast object of type %s to %s",
                            obj.getClass().getName(),
                            targetClass.getName()
                    ));
                }
                return null; // returning null to indicate failure
            }
        } catch (Exception ex) {
            if (hasLogger) {
                optionalLogger.Error(String.format("Error casting object: %s", ex.getMessage()));
            }
            return null; // returning null to indicate failure
        }
    }

    /**
     * Attempts to cast an object to a list of a specified type.
     *
     * @param obj           The object to be cast.
     * @param optionalLogger An optional logger for logging warnings or errors.
     * @param <T>           The type of the elements in the list.
     * @return A list of the specified type if the cast is successful, or null if the cast fails.
     */
    public static <T> @Nullable List<T> castAsList(@NotNull Object obj, @Nullable PluginLogger optionalLogger) {
        boolean hasLogger = optionalLogger != null;
        if (!(obj instanceof List<?> rawList)) {
            if (hasLogger) {
                optionalLogger.Warn("Expected List, but found: " + obj.getClass().getName());
            }
            return null; // returning null to indicate failure
        }

        List<T> resultList = new ArrayList<>();
        try {
            for (Object item : rawList) {
                if (item == null) {
                    if (hasLogger) {
                        optionalLogger.Warn("Found null item in list, skipping.");
                    }
                    continue; // skip null items
                }
                @SuppressWarnings("unchecked")
                T typedItem = (T) item; // Potential for inner warning
                resultList.add(typedItem);
            }
        } catch (Exception ex) {
            if (hasLogger) {
                optionalLogger.Error(String.format("Error casting object to List<T>: %s", ex.getMessage()));
            }
            return null;
        }
        return resultList;
    }

    /**
     * Attempts to cast an object to a map with specified key and value types.
     *
     * @param obj           The object to be cast.
     * @param optionalLogger An optional logger for logging warnings or errors.
     * @param <K>           The type of the keys in the map.
     * @param <V>           The type of the values in the map.
     * @return A map of the specified key-value types if the cast is successful, or null if the cast fails.
     */
    public static <K, V> @Nullable Map<K, V> castAsMap(@NotNull Object obj, @Nullable PluginLogger optionalLogger) {
        boolean hasLogger = optionalLogger != null;

        if (!(obj instanceof Map<?, ?> rawMap)) {
            if (hasLogger) {
                optionalLogger.Warn("Expected Map, but found: " + obj.getClass().getName());
            }
            return null; // returning null to indicate failure
        }

        try {
            @SuppressWarnings("unchecked")
            Map<K, V> typedMap = (Map<K, V>) rawMap; // Potential for inner warning
            return typedMap;
        } catch (Exception ex) {
            if (hasLogger) {
                optionalLogger.Error(String.format("Error casting object to Map<K, V>: %s", ex.getMessage()));
            }
            return null; // returning null to indicate failure
        }
    }

    /**
     * Attempts to cast an object to a list of maps with specified key and value types.
     *
     * @param obj           The object to be cast.
     * @param optionalLogger An optional logger for logging warnings or errors.
     * @param <K>           The type of the keys in the maps.
     * @param <V>           The type of the values in the maps.
     * @return A list of maps if the cast is successful, or null if the cast fails.
     */
    public static <K, V> @Nullable List<Map<K, V>> castAsListOfMaps(@NotNull Object obj, @Nullable PluginLogger optionalLogger) {
        boolean hasLogger = optionalLogger != null;

        if (!(obj instanceof List<?> rawList)) {
            if (hasLogger) {
                optionalLogger.Warn("Expected List, but found: " + obj.getClass().getName());
            }
            return null; // returning null to indicate failure
        }

        List<Map<K, V>> resultList = new ArrayList<>();
        for (Object item : rawList) {
            if (item == null) {
                if (hasLogger) {
                    optionalLogger.Warn("Found null item in list, skipping.");
                }
                continue; // skip null items
            }

            if (item instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<K, V> typedMap = (Map<K, V>) item; // Potential for inner warning
                resultList.add(typedMap);
            } else {
                if (hasLogger) {
                    optionalLogger.Warn("Expected Map in list, but found: " + item.getClass().getName());
                }
            }
        }
        return resultList;
    }
}