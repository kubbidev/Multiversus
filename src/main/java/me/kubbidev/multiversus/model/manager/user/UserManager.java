package me.kubbidev.multiversus.model.manager.user;

import me.kubbidev.multiversus.model.User;
import me.kubbidev.multiversus.model.manager.Manager;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserManager<T extends User> extends Manager<UUID, User, T> {

    T getOrMake(UUID id, String username);

    /**
     * Get a user object by name
     *
     * @param name The name to search by
     * @return a {@link User} object if the user is loaded, returns null if the user is not loaded
     */
    T getByUsername(String name);

    /**
     * Gets the instance responsible for unloading unneeded users.
     *
     * @return the housekeeper
     */
    UserHousekeeper getHouseKeeper();

    /**
     * Reloads the data of all *online* users
     */
    CompletableFuture<Void> loadAllUsers();

}