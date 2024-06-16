package net.multiversus.api.messaging;

import net.multiversus.api.event.messaging.CustomMessageReceiveEvent;
import org.jetbrains.annotations.NotNull;

/**
 * A means to send messages to other servers using the platforms networking
 */
public interface MessagingService {

    /**
     * Gets the name of this messaging service
     *
     * @return the name of this messaging service
     */
    @NotNull String getName();

    /**
     * Uses the messaging service to send a message with a custom payload.
     *
     * <p>The intended use case of this functionality is to allow plugins/mods
     * to send <b>lightweight</b> custom messages
     * between instances, piggy-backing on top of the messenger abstraction
     * already built into Multiversus.</p>
     *
     * <p>It is <b>not</b> intended as a full message broker replacement/abstraction.
     * Note that some of the messenger implementations in Multiversus cannot handle
     * a high volume of messages being sent (for example the SQL messenger).
     * Additionally, some implementations do not give any guarantees that a message
     * will be delivered on time or even at all (for example the plugin message
     * messengers).</p>
     *
     * <p>With all of that in mind, please consider that if you are using this
     * functionality to send messages that have nothing to do with Multiversus or
     * permissions, or that require guarantees around delivery reliability, you
     * are most likely misusing the API and would be better off building your own
     * integration with a message broker.</p>
     *
     * <p>Whilst there is (currently) no strict validation, it is recommended
     * that the channel id should use the same format as Minecraft resource locations /
     * namespaced keys. For example, a plugin called "SuperRanks" sending rank-up
     * notifications using custom payload messages might use the channel id
     * {@code "superranks:notifications"} for this purpose.</p>
     *
     * <p>The payload can be any valid UTF-8 string.</p>
     *
     * <p>The message will be delivered asynchronously.</p>
     *
     * <p>Other Multiversus instances that receive the message will publish it to API
     * consumers using the {@link CustomMessageReceiveEvent}.</p>
     *
     * @param channelId the channel id
     * @param payload the message payload
     */
    void sendCustomMessage(@NotNull String channelId, @NotNull String payload);

}