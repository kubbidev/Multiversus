package net.multiversus.api.model.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A player which holds data.
 */
public interface User {

    /**
     * Gets the users unique ID
     *
     * @return the users Mojang assigned unique id
     */
    @NotNull UUID getUniqueId();

    /**
     * Gets the users username
     *
     * <p>Returns null if no username is known for the user.</p>
     *
     * @return the users username
     */
    @Nullable String getUsername();
}