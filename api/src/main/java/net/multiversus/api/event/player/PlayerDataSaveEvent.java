package net.multiversus.api.event.player;

import net.multiversus.api.event.MultiEvent;
import net.multiversus.api.event.util.Param;
import net.multiversus.api.model.PlayerSaveResult;
import net.multiversus.api.model.user.UserManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Called when player data is saved to the storage.
 *
 * <p>Data can be saved using {@link UserManager#savePlayerData(UUID, String)}.</p>
 */
public interface PlayerDataSaveEvent extends MultiEvent {

    /**
     * Gets the unique ID that was saved.
     *
     * @return the uuid
     */
    @Param(0)
    @NotNull UUID getUniqueId();

    /**
     * Gets the username that was saved.
     *
     * @return the username
     */
    @Param(1)
    @NotNull String getUsername();

    /**
     * Gets the result of the operation.
     *
     * @return the result
     */
    @Param(2)
    @NotNull PlayerSaveResult getResult();

}