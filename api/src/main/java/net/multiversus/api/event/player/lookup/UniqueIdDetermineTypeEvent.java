package net.multiversus.api.event.player.lookup;

import net.multiversus.api.event.MultiEvent;
import net.multiversus.api.event.type.ResultEvent;
import net.multiversus.api.event.util.Param;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Called when the platform needs to determine the type of a player's {@link UUID unique id}.
 */
public interface UniqueIdDetermineTypeEvent extends MultiEvent, ResultEvent<String> {

    /**
     * The players UUID has been obtained by authenticating with the Mojang session servers.
     *
     * <p>Usually indicated by the UUID being {@link UUID#version() version} 4.</p>
     */
    String TYPE_AUTHENTICATED = "authenticated";

    /**
     * The players UUID has not been obtained through authentication, and instead is likely based
     * on the username they connected with.
     *
     * <p>Usually indicated by the UUID being {@link UUID#version() version} 3.</p>
     */
    String TYPE_UNAUTHENTICATED = "unauthenticated";

    /**
     * The players UUID most likely belongs to a NPC (non-player character).
     *
     * <p>Usually indicated by the UUID being {@link UUID#version() version} 2.</p>
     */
    String TYPE_NPC = "npc";

    /**
     * Unknown UUID type.
     */
    String TYPE_UNKNOWN = "unknown";

    /**
     * Gets the {@link UUID unique id} being queried.
     *
     * @return the unique id
     */
    @Param(0)
    @NotNull UUID getUniqueId();

    /**
     * Gets the current result unique id type.
     *
     * @return the type
     */
    default @NotNull String getType() {
        return result().get();
    }

    /**
     * Sets the result unique id type.
     *
     * @param type the type
     */
    default void setType(@NotNull String type) {
        Objects.requireNonNull(type, "type");
        result().set(type);
    }

}