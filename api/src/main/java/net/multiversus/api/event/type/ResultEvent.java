package net.multiversus.api.event.type;

import net.multiversus.api.event.util.Param;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents an event that has a result.
 *
 * @param <T> the type of the result
 */
public interface ResultEvent<T> {

    /**
     * Gets an {@link AtomicReference} containing the result.
     *
     * @return the result
     */
    @Param(-1)
    @NotNull AtomicReference<T> result();

    /**
     * Gets if a result has been set for the event.
     *
     * @return if there is a result
     */
    default boolean hasResult() {
        return result().get() != null;
    }

}