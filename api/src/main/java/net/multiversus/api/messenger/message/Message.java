package net.multiversus.api.messenger.message;

import net.multiversus.api.messenger.Messenger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a message sent received via a {@link Messenger}.
 */
@ApiStatus.NonExtendable
public interface Message {

    /**
     * Gets the unique id associated with this message.
     *
     * <p>This ID is used to ensure a single instance doesn't process
     * the same message twice.</p>
     *
     * @return the id of the message
     */
    @NotNull UUID getId();

}