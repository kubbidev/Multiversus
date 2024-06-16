package net.multiversus.api.event.user;

import net.multiversus.api.event.MultiEvent;
import net.multiversus.api.event.type.Cancellable;
import net.multiversus.api.event.util.Param;
import net.multiversus.api.model.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a user is about to be unloaded from memory.
 */
public interface UserUnloadEvent extends MultiEvent, Cancellable {

    /**
     * Gets the user that is being unloaded
     *
     * @return the user that is being unloaded
     */
    @Param(0)
    @NotNull User getUser();
}