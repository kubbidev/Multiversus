package me.kubbidev.multiversus.model.manager.user;

import me.kubbidev.multiversus.config.ConfigKeys;
import me.kubbidev.multiversus.model.User;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.util.ExpiringSet;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * The instance responsible for unloading users which are no longer needed.
 */
public class UserHousekeeper implements Runnable {
    private final MultiPlugin plugin;
    private final UserManager<?> userManager;

    // contains the uuids of users who have recently logged in / out
    private final ExpiringSet<UUID> recentlyUsed;

    // contains the uuids of users who have recently been retrieved from the API
    private final ExpiringSet<UUID> recentlyUsedApi;

    public UserHousekeeper(MultiPlugin plugin, UserManager<?> userManager, TimeoutSettings timeoutSettings) {
        this.plugin = plugin;
        this.userManager = userManager;
        this.recentlyUsed = new ExpiringSet<>(timeoutSettings.duration, timeoutSettings.unit);
        this.recentlyUsedApi = new ExpiringSet<>(5, TimeUnit.MINUTES);
    }

    // called when a player attempts a connection or logs out
    public void registerUsage(UUID uuid) {
        this.recentlyUsed.add(uuid);
    }

    public void registerApiUsage(UUID uuid) {
        this.recentlyUsedApi.add(uuid);
    }

    public void clearApiUsage(UUID uuid) {
        this.recentlyUsedApi.remove(uuid);
    }

    @Override
    public void run() {
        for (UUID entry : this.userManager.getAll().keySet()) {
            cleanup(entry);
        }
    }

    public void cleanup(UUID uuid) {
        // unload users which aren't online and who haven't been online (or tried to login) recently
        if (this.recentlyUsed.contains(uuid) || this.recentlyUsedApi.contains(uuid) || this.plugin.getBootstrap().isPlayerOnline(uuid)) {
            return;
        }

        User user = this.userManager.getIfLoaded(uuid);
        if (user == null) {
            return;
        }

        if (this.plugin.getEventDispatcher().dispatchUserUnload(user)) {
            return;
        }

        // unload them
        if (this.plugin.getConfiguration().get(ConfigKeys.DEBUG_LOGINS)) {
            this.plugin.getLogger().info("User Housekeeper: unloading user data for " + uuid);
        }
        this.userManager.unload(uuid);
    }

    public static TimeoutSettings timeoutSettings(long duration, TimeUnit unit) {
        return new TimeoutSettings(duration, unit);
    }

    public static final class TimeoutSettings {
        private final long duration;
        private final TimeUnit unit;

        TimeoutSettings(long duration, TimeUnit unit) {
            this.duration = duration;
            this.unit = unit;
        }
    }
}