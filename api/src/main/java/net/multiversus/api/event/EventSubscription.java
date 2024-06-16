package net.multiversus.api.event;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Represents a subscription to a {@link MultiEvent}.
 *
 * @param <T> the event class
 */
public interface EventSubscription<T extends MultiEvent> extends AutoCloseable {

    /**
     * Gets the class this handler is listening to
     *
     * @return the event class
     */
    @NotNull Class<T> getEventClass();

    /**
     * Returns true if this handler is active
     *
     * @return true if this handler is still active
     */
    boolean isActive();

    /**
     * Unregisters this handler from the event bus.
     */
    @Override
    void close();

    /**
     * Gets the event consumer responsible for handling the event
     *
     * @return the event consumer
     */
    @NotNull Consumer<? super T> getHandler();
}