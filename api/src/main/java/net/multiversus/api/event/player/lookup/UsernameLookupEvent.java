package net.multiversus.api.event.player.lookup;

import net.multiversus.api.event.MultiEvent;
import net.multiversus.api.event.type.ResultEvent;
import net.multiversus.api.event.util.Param;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Called when the platform needs a username for a given unique id.
 */
public interface UsernameLookupEvent extends MultiEvent, ResultEvent<String> {

    /**
     * Gets the {@link UUID unique id} being looked up.
     *
     * @return the unique id
     */
    @Param(0)
    @NotNull UUID getUniqueId();

    /**
     * Sets the result username.
     *
     * @param username the username
     */
    default void setUsername(@Nullable String username) {
        result().set(username);
    }

}