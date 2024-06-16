package me.kubbidev.multiversus.storage;

import me.kubbidev.multiversus.model.User;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.storage.implementation.StorageImplementation;
import me.kubbidev.multiversus.storage.implementation.split.SplitStorage;
import me.kubbidev.multiversus.util.Throwing;
import net.multiversus.api.model.PlayerSaveResult;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Provides a {@link CompletableFuture} based API for interacting with a {@link StorageImplementation}.
 */
public class Storage {
    private final MultiPlugin plugin;
    private final StorageImplementation implementation;

    public Storage(MultiPlugin plugin, StorageImplementation implementation) {
        this.plugin = plugin;
        this.implementation = implementation;
    }

    public StorageImplementation getImplementation() {
        return this.implementation;
    }

    public Collection<StorageImplementation> getImplementations() {
        if (this.implementation instanceof SplitStorage) {
            return ((SplitStorage) this.implementation).getImplementations().values();
        } else {
            return Collections.singleton(this.implementation);
        }
    }

    private <T> CompletableFuture<T> future(Callable<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        }, this.plugin.getBootstrap().getScheduler().async());
    }

    private CompletableFuture<Void> future(Throwing.Runnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        }, this.plugin.getBootstrap().getScheduler().async());
    }

    public String getName() {
        return this.implementation.getImplementationName();
    }

    public void init() {
        try {
            this.implementation.init();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to init storage implementation", e);
        }
    }

    public void shutdown() {
        try {
            this.implementation.shutdown();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to shutdown storage implementation", e);
        }
    }

    public StorageMetadata getMeta() {
        return this.implementation.getMeta();
    }

    public CompletableFuture<User> loadUser(UUID uniqueId, String username) {
        return future(() -> {
            User user = this.implementation.loadUser(uniqueId, username);
            if (user != null) {
                this.plugin.getEventDispatcher().dispatchUserLoad(user);
            }
            return user;
        });
    }

    public CompletableFuture<Map<UUID, User>> loadUsers(Set<UUID> uniqueIds) {
        return future(() -> {
            Map<UUID, User> users = this.implementation.loadUsers(uniqueIds);
            for (User user : users.values()) {
                this.plugin.getEventDispatcher().dispatchUserLoad(user);
            }
            return users;
        });
    }

    public CompletableFuture<Void> saveUser(User user) {
        return future(() -> this.implementation.saveUser(user));
    }

    public CompletableFuture<Set<UUID>> getUniqueUsers() {
        return future(this.implementation::getUniqueUsers);
    }

    public CompletableFuture<PlayerSaveResult> savePlayerData(UUID uniqueId, String username) {
        return future(() -> {
            PlayerSaveResult result = this.implementation.savePlayerData(uniqueId, username);
            if (result != null) {
                this.plugin.getEventDispatcher().dispatchPlayerDataSave(uniqueId, username, result);
            }
            return result;
        });
    }

    public CompletableFuture<Void> deletePlayerData(UUID uniqueId) {
        return future(() -> this.implementation.deletePlayerData(uniqueId));
    }

    public CompletableFuture<UUID> getPlayerUniqueId(String username) {
        return future(() -> this.implementation.getPlayerUniqueId(username));
    }

    public CompletableFuture<String> getPlayerName(UUID uniqueId) {
        return future(() -> this.implementation.getPlayerName(uniqueId));
    }
}