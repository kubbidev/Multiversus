package net.multiversus.api.event.user;

import net.multiversus.api.event.MultiEvent;
import net.multiversus.api.event.util.Param;
import net.multiversus.api.model.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a user is loaded into memory from the storage.
 */
public interface UserLoadEvent extends MultiEvent {

    /**
     * Gets the user that was loaded
     *
     * @return the user that was loaded
     */
    @Param(0)
    @NotNull User getUser();

}