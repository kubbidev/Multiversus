package net.multiversus.api.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;
import java.util.function.Consumer;

/**
 * The Multiversus event bus.
 *
 * <p>Used to subscribe (or "listen") to Multiversus events.</p>
 */
public interface EventBus {

    /**
     * Registers a new subscription to the given event.
     *
     * <p>The returned {@link EventSubscription} instance encapsulates the subscription state. It has
     * methods which can be used to terminate the subscription, or view stats about the nature of
     * the subscription.</p>
     *
     * @param eventClass the event class
     * @param handler    the event handler
     * @param <T>        the event class
     * @return an event handler instance representing this subscription
     */
    <T extends MultiEvent> @NotNull EventSubscription<T> subscribe(@NotNull Class<T> eventClass, @NotNull Consumer<? super T> handler);

    /**
     * Registers a new subscription to the given event.
     *
     * <p>The returned {@link EventSubscription} instance encapsulates the subscription state. It has
     * methods which can be used to terminate the subscription, or view stats about the nature of
     * the subscription.</p>
     *
     * <p>Unlike {@link #subscribe(Class, Consumer)}, this method accepts an additional parameter
     * for {@code plugin}. This object must be a "plugin" instance on the platform, and is used to
     * automatically {@link EventSubscription#close() unregister} the subscription when the
     * corresponding plugin is disabled.</p>
     *
     * @param <T>        the event class
     * @param plugin     a plugin instance to bind the subscription to.
     * @param eventClass the event class
     * @param handler    the event handler
     * @return an event handler instance representing this subscription
     */
    <T extends MultiEvent> @NotNull EventSubscription<T> subscribe(Object plugin, @NotNull Class<T> eventClass, @NotNull Consumer<? super T> handler);

    /**
     * Gets a set of all registered handlers for a given event.
     *
     * @param eventClass the event to find handlers for
     * @param <T>        the event class
     * @return an immutable set of event handlers
     */
    <T extends MultiEvent> @NotNull @Unmodifiable Set<EventSubscription<T>> getSubscriptions(@NotNull Class<T> eventClass);

}