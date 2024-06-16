package me.kubbidev.multiversus.event;

import me.kubbidev.multiversus.api.MultiversusApiProvider;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import net.multiversus.api.event.EventBus;
import net.multiversus.api.event.EventSubscription;
import net.multiversus.api.event.MultiEvent;
import net.kyori.event.EventSubscriber;
import net.kyori.event.SimpleEventBus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractEventBus<P> implements EventBus, AutoCloseable {

    /**
     * The plugin instance
     */
    private final MultiPlugin plugin;

    /**
     * The api provider instance
     */
    private final MultiversusApiProvider apiProvider;

    /**
     * The delegate event bus
     */
    private final Bus bus = new Bus();

    protected AbstractEventBus(MultiPlugin plugin, MultiversusApiProvider apiProvider) {
        this.plugin = plugin;
        this.apiProvider = apiProvider;
    }

    public MultiPlugin getPlugin() {
        return this.plugin;
    }

    public MultiversusApiProvider getApiProvider() {
        return this.apiProvider;
    }

    /**
     * Checks that the given plugin object is a valid plugin instance for the platform
     *
     * @param plugin the object
     * @return a plugin
     * @throws IllegalArgumentException if the plugin is invalid
     */
    protected abstract P checkPlugin(Object plugin) throws IllegalArgumentException;

    public void post(MultiEvent event) {
        this.bus.post(event);
    }

    public boolean shouldPost(Class<? extends MultiEvent> eventClass) {
        return this.bus.hasSubscribers(eventClass);
    }

    public void subscribe(MultiEventListener listener) {
        listener.bind(this);
    }

    @Override
    public <T extends MultiEvent> @NotNull EventSubscription<T> subscribe(@NotNull Class<T> eventClass, @NotNull Consumer<? super T> handler) {
        Objects.requireNonNull(eventClass, "eventClass");
        Objects.requireNonNull(handler, "handler");
        return registerSubscription(eventClass, handler, null);
    }

    @Override
    public <T extends MultiEvent> @NotNull EventSubscription<T> subscribe(Object plugin, @NotNull Class<T> eventClass, @NotNull Consumer<? super T> handler) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(eventClass, "eventClass");
        Objects.requireNonNull(handler, "handler");
        return registerSubscription(eventClass, handler, checkPlugin(plugin));
    }

    private <T extends MultiEvent> EventSubscription<T> registerSubscription(Class<T> eventClass, Consumer<? super T> handler, Object plugin) {
        if (!eventClass.isInterface()) {
            throw new IllegalArgumentException("class " + eventClass + " is not an interface");
        }
        if (!MultiEvent.class.isAssignableFrom(eventClass)) {
            throw new IllegalArgumentException("class " + eventClass.getName() + " does not implement MultiEvent");
        }

        MultiEventSubscription<T> eventHandler = new MultiEventSubscription<>(this, eventClass, handler, plugin);
        this.bus.register(eventClass, eventHandler);

        return eventHandler;
    }

    @Override
    public <T extends MultiEvent> @NotNull Set<EventSubscription<T>> getSubscriptions(@NotNull Class<T> eventClass) {
        return this.bus.getHandlers(eventClass);
    }

    /**
     * Removes a specific handler from the bus
     *
     * @param handler the handler to remove
     */
    public void unregisterHandler(MultiEventSubscription<?> handler) {
        this.bus.unregister(handler);
    }

    /**
     * Removes all handlers for a specific plugin
     *
     * @param plugin the plugin
     */
    protected void unregisterHandlers(P plugin) {
        this.bus.unregister(sub -> ((MultiEventSubscription<?>) sub).getPlugin() == plugin);
    }

    @Override
    public void close() {
        this.bus.unregisterAll();
    }

    private static final class Bus extends SimpleEventBus<MultiEvent> {
        Bus() {
            super(MultiEvent.class);
        }

        @Override
        protected boolean shouldPost(@NotNull MultiEvent event, @NotNull EventSubscriber<?> subscriber) {
            return true;
        }

        public <T extends MultiEvent> Set<EventSubscription<T>> getHandlers(Class<T> eventClass) {
            //noinspection unchecked
            return super.subscribers().values().stream()
                    .filter(s -> s instanceof EventSubscription && ((EventSubscription<?>) s).getEventClass().isAssignableFrom(eventClass))
                    .map(s -> (EventSubscription<T>) s)
                    .collect(Collectors.toSet());
        }
    }
}