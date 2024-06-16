package net.multiversus.api.event.player;

import net.multiversus.api.event.MultiEvent;
import net.multiversus.api.event.util.Param;
import net.multiversus.api.model.user.User;
import net.multiversus.api.util.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Called when Multiversus has finished processing a Player's initial connection.
 *
 * <p>This event will always execute during the platforms async connection
 * event. The Multiversus platform listener processing the connection will block
 * while this event is posted.</p>
 *
 * <p>This, among other things, allows you to wait until data is
 * loaded for a User during the BungeeCord 'LoginEvent', as event priorities are
 * ignored by the current implementation.</p>
 *
 * <p>The implementation will make an attempt to ensure this event is called
 * for all connections, even if the operation to load User data was not
 * successful. Note that Multiversus will usually cancel the platform connection
 * event if data could not be loaded.</p>
 */
public interface PlayerLoginProcessEvent extends MultiEvent, Result {

    /**
     * Gets the UUID of the connection which was processed
     *
     * @return the uuid of the connection which was processed
     */
    @Param(0)
    @NotNull UUID getUniqueId();

    /**
     * Gets the username of the connection which was processed
     *
     * @return the username of the connection which was processed
     */
    @Param(1)
    @NotNull String getUsername();

    /**
     * Gets if the login was processed successfully.
     *
     * @return true if the login was successful
     */
    @Override
    default boolean wasSuccessful() {
        return getUser() != null;
    }

    /**
     * Gets the resultant User instance which was loaded.
     *
     * <p>Returns {@code null} if the login was not processed
     * {@link #wasSuccessful() successfully.}</p>
     *
     * @return the user instance
     */
    @Param(2)
    @Nullable User getUser();

}