package me.kubbidev.multiversus.event;

import net.multiversus.api.event.EventSubscription;
import net.multiversus.api.event.MultiEvent;
import net.kyori.event.EventSubscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Simple implementation of {@link EventSubscription}.
 *
 * @param <T> the event type
 */
public class MultiEventSubscription<T extends MultiEvent> implements EventSubscription<T>, EventSubscriber<T> {

    /**
     * The event bus which created this handler
     */
    private final AbstractEventBus<?> eventBus;

    /**
     * The event class
     */
    private final Class<T> eventClass;

    /**
     * The delegate "event handler"
     */
    private final Consumer<? super T> consumer;

    /**
     * The plugin which "owns" this handler
     */
    private final @Nullable Object plugin;

    /**
     * If this handler is active
     */
    private final AtomicBoolean active = new AtomicBoolean(true);

    public MultiEventSubscription(AbstractEventBus<?> eventBus, Class<T> eventClass, Consumer<? super T> consumer, @Nullable Object plugin) {
        this.eventBus = eventBus;
        this.eventClass = eventClass;
        this.consumer = consumer;
        this.plugin = plugin;
    }

    @Override
    public boolean isActive() {
        return this.active.get();
    }

    @Override
    public void close() {
        // already unregistered
        if (!this.active.getAndSet(false)) {
            return;
        }

        this.eventBus.unregisterHandler(this);
    }

    @Override
    public void invoke(@NotNull T event) throws Throwable {
        try {
            this.consumer.accept(event);
        } catch (Throwable t) {
            this.eventBus.getPlugin().getLogger().warn("Unable to pass event " + event.getEventType().getSimpleName() + " to handler " + this.consumer.getClass().getName(), t);
        }
    }

    @Override
    public @NotNull Class<T> getEventClass() {
        return this.eventClass;
    }

    @Override
    public @NotNull Consumer<? super T> getHandler() {
        return this.consumer;
    }

    public @Nullable Object getPlugin() {
        return this.plugin;
    }
}