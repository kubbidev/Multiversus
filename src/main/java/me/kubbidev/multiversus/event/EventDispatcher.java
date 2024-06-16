package me.kubbidev.multiversus.event;

import me.kubbidev.multiversus.event.gen.GeneratedEventClass;
import me.kubbidev.multiversus.model.User;
import net.multiversus.api.event.MultiEvent;
import net.multiversus.api.event.extension.ExtensionLoadEvent;
import net.multiversus.api.event.messaging.CustomMessageReceiveEvent;
import net.multiversus.api.event.player.PlayerDataSaveEvent;
import net.multiversus.api.event.player.PlayerLoginProcessEvent;
import net.multiversus.api.event.player.lookup.UniqueIdDetermineTypeEvent;
import net.multiversus.api.event.player.lookup.UniqueIdLookupEvent;
import net.multiversus.api.event.player.lookup.UsernameLookupEvent;
import net.multiversus.api.event.player.lookup.UsernameValidityCheckEvent;
import net.multiversus.api.event.sync.ConfigReloadEvent;
import net.multiversus.api.event.sync.PostSyncEvent;
import net.multiversus.api.event.sync.PreSyncEvent;
import net.multiversus.api.event.type.Cancellable;
import net.multiversus.api.event.type.ResultEvent;
import net.multiversus.api.event.user.UserFirstLoginEvent;
import net.multiversus.api.event.user.UserLoadEvent;
import net.multiversus.api.event.user.UserUnloadEvent;
import net.multiversus.api.extension.Extension;
import net.multiversus.api.model.PlayerSaveResult;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class EventDispatcher {
    private final AbstractEventBus<?> eventBus;

    public EventDispatcher(AbstractEventBus<?> eventBus) {
        this.eventBus = eventBus;
    }

    public AbstractEventBus<?> getEventBus() {
        return this.eventBus;
    }

    private MultiEvent generate(Class<? extends MultiEvent> eventClass, Object... params) {
        try {
            return GeneratedEventClass.generate(eventClass).newInstance(this.eventBus.getApiProvider(), params);
        } catch (Throwable e) {
            throw new RuntimeException("Exception occurred whilst generating event instance", e);
        }
    }

    private void post(Class<? extends MultiEvent> eventClass, Object... params) {
        MultiEvent event = generate(eventClass, params);
        this.eventBus.post(event);
    }

    private void postAsync(Class<? extends MultiEvent> eventClass, Object... params) {
        // check against common mistakes - events with any sort of result shouldn't be posted async
        if (Cancellable.class.isAssignableFrom(eventClass) || ResultEvent.class.isAssignableFrom(eventClass)) {
            throw new RuntimeException("Event cannot be posted async (" + eventClass.getName() + ")");
        }

        // if there aren't any handlers registered for the event, don't bother trying to post it
        if (!this.eventBus.shouldPost(eventClass)) {
            return;
        }

        // async: generate an event class and post it
        this.eventBus.getPlugin().getBootstrap().getScheduler().executeAsync(() -> post(eventClass, params));
    }

    private void postSync(Class<? extends MultiEvent> eventClass, Object... params) {
        // if there aren't any handlers registered for our event, don't bother trying to post it
        if (!this.eventBus.shouldPost(eventClass)) {
            return;
        }

        // generate an event class and post it
        post(eventClass, params);
    }

    private boolean postCancellable(Class<? extends MultiEvent> eventClass, Object... params) {
        if (!Cancellable.class.isAssignableFrom(eventClass)) {
            throw new RuntimeException("Event is not cancellable: " + eventClass.getName());
        }

        // extract the initial state from the first parameter
        boolean initialState = (boolean) params[0];

        // if there aren't any handlers registered for the event, just return the initial state
        if (!this.eventBus.shouldPost(eventClass)) {
            return initialState;
        }

        // otherwise:
        // - initialise an AtomicBoolean for the result with the initial state
        // - replace the boolean with the AtomicBoolean in the params array
        // - generate an event class and post it
        AtomicBoolean cancel = new AtomicBoolean(initialState);
        params[0] = cancel;
        post(eventClass, params);

        // return the final status
        return cancel.get();
    }

    public void dispatchCustomMessageReceive(String channelId, String payload) {
        postAsync(CustomMessageReceiveEvent.class, channelId, payload);
    }

    public void dispatchExtensionLoad(Extension extension) {
        postAsync(ExtensionLoadEvent.class, extension);
    }

    public void dispatchConfigReload() {
        postAsync(ConfigReloadEvent.class);
    }

    public void dispatchPostSync() {
        postAsync(PostSyncEvent.class);
    }

    public boolean dispatchPreSync(boolean initialState) {
        return postCancellable(PreSyncEvent.class, initialState);
    }

    public void dispatchUserFirstLogin(UUID uniqueId, String username) {
        postAsync(UserFirstLoginEvent.class, uniqueId, username);
    }

    public void dispatchPlayerLoginProcess(UUID uniqueId, String username, @Nullable User user) {
        postSync(PlayerLoginProcessEvent.class, uniqueId, username, user == null ? null : user.getApiProxy());
    }

    public void dispatchPlayerDataSave(UUID uniqueId, String username, PlayerSaveResult result) {
        postAsync(PlayerDataSaveEvent.class, uniqueId, username, result);
    }

    public String dispatchUniqueIdDetermineType(UUID uniqueId, String initialType) {
        AtomicReference<String> result = new AtomicReference<>(initialType);
        postSync(UniqueIdDetermineTypeEvent.class, result, uniqueId);
        return result.get();
    }

    public UUID dispatchUniqueIdLookup(String username, UUID initial) {
        AtomicReference<UUID> result = new AtomicReference<>(initial);
        postSync(UniqueIdLookupEvent.class, result, username);
        return result.get();
    }

    public String dispatchUsernameLookup(UUID uniqueId, String initial) {
        AtomicReference<String> result = new AtomicReference<>(initial);
        postSync(UsernameLookupEvent.class, result, uniqueId);
        return result.get();
    }

    public boolean dispatchUsernameValidityCheck(String username, boolean initialState) {
        AtomicBoolean result = new AtomicBoolean(initialState);
        postSync(UsernameValidityCheckEvent.class, username, result);
        return result.get();
    }

    public void dispatchUserLoad(User user) {
        postAsync(UserLoadEvent.class, user.getApiProxy());
    }

    public boolean dispatchUserUnload(User user) {
        return postCancellable(UserUnloadEvent.class, false, user.getApiProxy());
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends MultiEvent>[] getKnownEventTypes() {
        return new Class[]{
                ExtensionLoadEvent.class,
                CustomMessageReceiveEvent.class,
                PlayerDataSaveEvent.class,
                PlayerLoginProcessEvent.class,
                UniqueIdDetermineTypeEvent.class,
                UniqueIdLookupEvent.class,
                UsernameLookupEvent.class,
                UsernameValidityCheckEvent.class,
                ConfigReloadEvent.class,
                PostSyncEvent.class,
                PreSyncEvent.class,
                UserFirstLoginEvent.class,
                UserLoadEvent.class,
                UserUnloadEvent.class,
        };
    }
}