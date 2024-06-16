package net.multiversus.api.event.player.lookup;

import net.multiversus.api.event.MultiEvent;
import net.multiversus.api.event.type.ResultEvent;
import net.multiversus.api.event.util.Param;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Called when the platform needs a unique id for a given username.
 */
public interface UniqueIdLookupEvent extends MultiEvent, ResultEvent<UUID> {

    /**
     * Gets the username being looked up.
     *
     * @return the username
     */
    @Param(0)
    @NotNull String getUsername();

    /**
     * Sets the result unique id.
     *
     * @param uniqueId the unique id
     */
    default void setUniqueId(@Nullable UUID uniqueId) {
        result().set(uniqueId);
    }

}