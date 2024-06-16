package net.multiversus.api.model.user;

import net.multiversus.api.model.PlayerSaveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Represents the object responsible for managing {@link User} instances.
 *
 * <p>Note that User instances are automatically loaded for online players.
 * It's likely that offline players will not have an instance pre-loaded.</p>
 *
 * <p>All blocking methods return {@link CompletableFuture}s, which will be
 * populated with the result once the data has been loaded/saved asynchronously.
 * Care should be taken when using such methods to ensure that the main server
 * thread is not blocked.</p>
 *
 * <p>Methods such as {@link CompletableFuture#get()} and equivalent should
 * <strong>not</strong> be called on the main server thread. If you need to use
 * the result of these operations on the main server thread, register a
 * callback using {@link CompletableFuture#thenAcceptAsync(Consumer, Executor)}.</p>
 */
public interface UserManager {

    /**
     * Loads a user from the plugin's storage provider into memory.
     *
     * @param uniqueId the uuid of the user
     * @param username the username, if known
     * @return the resultant user
     * @throws NullPointerException if the uuid is null
     */
    @NotNull CompletableFuture<User> loadUser(@NotNull UUID uniqueId, @Nullable String username);

    /**
     * Loads a user from the plugin's storage provider into memory.
     *
     * @param uniqueId the uuid of the user
     * @return the resultant user
     * @throws NullPointerException if the uuid is null
     */
    default @NotNull CompletableFuture<User> loadUser(@NotNull UUID uniqueId) {
        return loadUser(uniqueId, null);
    }

    /**
     * Uses the Multiversus cache to find a uuid for the given username.
     *
     * <p>This lookup is case insensitive.</p>
     *
     * @param username the username
     * @return a uuid, could be null
     * @throws NullPointerException     if either parameters are null
     * @throws IllegalArgumentException if the username is invalid
     */
    @NotNull CompletableFuture<UUID> lookupUniqueId(@NotNull String username);

    /**
     * Uses the Multiversus cache to find a username for the given uuid.
     *
     * @param uniqueId the uuid
     * @return a username, could be null
     * @throws NullPointerException     if either parameters are null
     * @throws IllegalArgumentException if the username is invalid
     */
    @NotNull CompletableFuture<String> lookupUsername(@NotNull UUID uniqueId);

    /**
     * Saves a user's data back to the plugin's storage provider.
     *
     * <p>You should call this after you make any changes to a user.</p>
     *
     * @param user the user to save
     * @return a future to encapsulate the operation.
     * @throws NullPointerException  if user is null
     * @throws IllegalStateException if the user instance was not obtained from Multiversus.
     */
    @NotNull CompletableFuture<Void> saveUser(@NotNull User user);

    /**
     * Loads a user from the plugin's storage provider, applies the given {@code action},
     * then saves the user's data back to storage.
     *
     * <p>This method effectively calls {@link #loadUser(UUID)}, followed by the {@code action},
     * then {@link #saveUser(User)}, and returns an encapsulation of the whole process as a
     * {@link CompletableFuture}. </p>
     *
     * @param uniqueId the uuid of the user
     * @param action the action to apply to the user
     * @return a future to encapsulate the operation
     */
    default @NotNull CompletableFuture<Void> modifyUser(@NotNull UUID uniqueId, @NotNull Consumer<? super User> action) {
        /* This default method is overridden in the implementation, and is just here
           to demonstrate what this method does in the API sources. */
        return loadUser(uniqueId)
                .thenApplyAsync(user -> { action.accept(user); return user; })
                .thenCompose(this::saveUser);
    }

    /**
     * Saves data about a player to the uuid caching system.
     *
     * @param uniqueId     the users mojang unique id
     * @param username the users username
     * @return the result of the operation.
     * @throws NullPointerException     if either parameters are null
     * @throws IllegalArgumentException if the username is invalid
     */
    @NotNull CompletableFuture<PlayerSaveResult> savePlayerData(@NotNull UUID uniqueId, @NotNull String username);

    /**
     * Deletes any data about a given player from the uuid caching system.
     *
     * <p>Note that this method does not affect any saved user data.</p>
     *
     * @param uniqueId the users mojang unique id
     * @return a future encapsulating the result of the operation
     */
    @NotNull CompletableFuture<Void> deletePlayerData(@NotNull UUID uniqueId);

    /**
     * Gets a set all "unique" user UUIDs.
     *
     * <p>"Unique" meaning the user isn't just a member of the "default" group.</p>
     *
     * @return a set of uuids
     */
    @NotNull CompletableFuture<@Unmodifiable Set<UUID>> getUniqueUsers();

    /**
     * Gets a loaded user.
     *
     * @param uniqueId the uuid of the user to get
     * @return a {@link User} object, if one matching the uuid is loaded, or null if not
     * @throws NullPointerException if the uuid is null
     */
    @Nullable User getUser(@NotNull UUID uniqueId);

    /**
     * Gets a loaded user.
     *
     * @param username the username of the user to get
     * @return a {@link User} object, if one matching the uuid is loaded, or null if not
     * @throws NullPointerException if the name is null
     */
    @Nullable User getUser(@NotNull String username);

    /**
     * Gets a set of all loaded users.
     *
     * @return a {@link Set} of {@link User} objects
     */
    @NotNull @Unmodifiable Set<User> getLoadedUsers();

    /**
     * Check if a user is loaded in memory
     *
     * @param uniqueId the uuid to check for
     * @return true if the user is loaded
     * @throws NullPointerException if the uuid is null
     */
    boolean isLoaded(@NotNull UUID uniqueId);

    /**
     * Unload a user from the internal storage, if they're not currently online.
     *
     * @param user the user to unload
     * @throws NullPointerException if the user is null
     */
    void cleanupUser(@NotNull User user);

}