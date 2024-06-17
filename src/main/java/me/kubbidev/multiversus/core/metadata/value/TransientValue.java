package me.kubbidev.multiversus.core.metadata.value;

import me.kubbidev.multiversus.core.metadata.MetadataMap;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a value in a {@link MetadataMap} which will automatically expire at some point.
 *
 * @param <T> the type of the underlying value
 */
public interface TransientValue<T> {

    /**
     * Returns the underlying value, or null if it has expired
     *
     * @return the underlying value, or null if it has expired
     */
    @Nullable
    T getOrNull();

    /**
     * Returns if this value should be removed from the map
     *
     * @return true if the value should expire
     */
    boolean shouldExpire();

}