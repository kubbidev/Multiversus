package me.kubbidev.multiversus.messaging;

import net.multiversus.api.messenger.Messenger;
import net.multiversus.api.messenger.MessengerProvider;

public interface InternalMessagingService {

    /**
     * Gets the name of this messaging service
     *
     * @return the name of this messaging service
     */
    String getName();

    Messenger getMessenger();

    MessengerProvider getMessengerProvider();

    /**
     * Closes the messaging service
     */
    void close();

    /**
     * Pushes a custom payload to connected instances.
     *
     * @param channelId the channel id
     * @param payload the payload
     */
    void pushCustomPayload(String channelId, String payload);
}