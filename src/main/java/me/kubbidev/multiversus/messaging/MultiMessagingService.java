package me.kubbidev.multiversus.messaging;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.kubbidev.multiversus.messaging.message.CustomMessageImpl;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.util.ExpiringSet;
import me.kubbidev.multiversus.util.gson.GsonProvider;
import me.kubbidev.multiversus.util.gson.JObject;
import net.multiversus.api.messenger.IncomingMessageConsumer;
import net.multiversus.api.messenger.Messenger;
import net.multiversus.api.messenger.MessengerProvider;
import net.multiversus.api.messenger.message.Message;
import net.multiversus.api.messenger.message.type.CustomMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MultiMessagingService implements InternalMessagingService, IncomingMessageConsumer {
    private final MultiPlugin plugin;
    private final ExpiringSet<UUID> receivedMessages;

    private final MessengerProvider messengerProvider;
    private final Messenger messenger;

    public MultiMessagingService(MultiPlugin plugin, MessengerProvider messengerProvider) {
        this.plugin = plugin;

        this.messengerProvider = messengerProvider;
        this.messenger = messengerProvider.obtain(this);
        Objects.requireNonNull(this.messenger, "messenger");

        this.receivedMessages = new ExpiringSet<>(5, TimeUnit.MINUTES);
    }

    @Override
    public String getName() {
        return this.messengerProvider.getName();
    }

    @Override
    public Messenger getMessenger() {
        return this.messenger;
    }

    @Override
    public MessengerProvider getMessengerProvider() {
        return this.messengerProvider;
    }

    @Override
    public void close() {
        this.messenger.close();
    }

    private UUID generatePingId() {
        UUID uuid = UUID.randomUUID();
        this.receivedMessages.add(uuid);
        return uuid;
    }

    @Override
    public void pushCustomPayload(String channelId, String payload) {
        this.plugin.getBootstrap().getScheduler().executeAsync(() -> {
            UUID requestId = generatePingId();
            this.messenger.sendOutgoingMessage(new CustomMessageImpl(requestId, channelId, payload));
        });
    }

    @Override
    public boolean consumeIncomingMessage(@NotNull Message message) {
        Objects.requireNonNull(message, "message");

        if (!this.receivedMessages.add(message.getId())) {
            return false;
        }

        // determine if the message can be handled by us
        boolean valid = message instanceof CustomMessage;

        // instead of throwing an exception here, just return false
        // it means an instance of Multiversus can gracefully handle messages it doesn't
        // "understand" yet. (sent from an instance running a newer version, etc)
        if (!valid) {
            return false;
        }

        processIncomingMessage(message);
        return true;
    }

    @Override
    public boolean consumeIncomingMessageAsString(@NotNull String encodedString) {
        try {
            return consumeIncomingMessageAsString0(encodedString);
        } catch (Exception e) {
            this.plugin.getLogger().warn("Unable to decode incoming messaging service message: '" + encodedString + "'", e);
            return false;
        }
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private boolean consumeIncomingMessageAsString0(@NotNull String encodedString) {
        Objects.requireNonNull(encodedString, "encodedString");
        JsonObject parsed = Objects.requireNonNull(GsonProvider.normal().fromJson(encodedString, JsonObject.class), "parsed");
        JsonObject json = parsed.getAsJsonObject();

        // extract id
        JsonElement idElement = json.get("id");
        if (idElement == null) {
            throw new IllegalStateException("Incoming message has no id argument: " + encodedString);
        }
        UUID id = UUID.fromString(idElement.getAsString());

        // ensure the message hasn't been received already
        if (!this.receivedMessages.add(id)) {
            return false;
        }

        // extract type
        JsonElement typeElement = json.get("type");
        if (typeElement == null) {
            throw new IllegalStateException("Incoming message has no type argument: " + encodedString);
        }
        String type = typeElement.getAsString();

        // extract content
        @Nullable JsonElement content = json.get("content");

        // decode message
        Message decoded;
        switch (type) {
            case CustomMessageImpl.TYPE:
                decoded = CustomMessageImpl.decode(content, id);
                break;
            default:// gracefully return if we just don't recognise the type
                return false;
        }

        // consume the message
        processIncomingMessage(decoded);
        return true;
    }

    public static String encodeMessageAsString(String type, UUID id, @Nullable JsonElement content) {
        JsonObject json = new JObject()
                .add("id", id.toString())
                .add("type", type)
                .consume(o -> {
                    if (content != null) {
                        o.add("content", content);
                    }
                })
                .toJson();

        return GsonProvider.normal().toJson(json);
    }

    private void processIncomingMessage(Message message) {
        if (message instanceof CustomMessage) {
            CustomMessage msg = (CustomMessage) message;

            this.plugin.getEventDispatcher().dispatchCustomMessageReceive(msg.getChannelId(), msg.getPayload());

        } else {
            throw new IllegalArgumentException("Unknown message type: " + message.getClass().getName());
        }
    }
}