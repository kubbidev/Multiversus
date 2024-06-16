package net.multiversus.api.event.user;

import net.multiversus.api.event.MultiEvent;
import net.multiversus.api.event.util.Param;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Called when the user logs into the network for the first time.
 *
 * <p>Particularly useful for networks with multiple
 * lobbies, who want to welcome a user when they join for the first time.</p>
 *
 * <p>This event is fired before the player has actually joined the game on the async login / auth event. If you want to
 * do something with the user, store the UUID in a set, and then check the set in the PlayerJoinEvent o.e.</p>
 *
 * <p>The users data will not be loaded when this event is called.</p>
 */
public interface UserFirstLoginEvent extends MultiEvent {

    /**
     * Gets the UUID of the user
     *
     * @return the uuid of the user
     */
    @Param(0)
    @NotNull UUID getUniqueId();

    /**
     * Gets the username of the user
     *
     * @return the username of the user
     */
    @Param(1)
    @NotNull String getUsername();

}