package me.kubbidev.multiversus.messaging.message;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.kubbidev.multiversus.messaging.MultiMessagingService;
import me.kubbidev.multiversus.util.gson.JObject;
import net.multiversus.api.messenger.message.type.CustomMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CustomMessageImpl extends AbstractMessage implements CustomMessage {
    public static final String TYPE = "custom";

    public static CustomMessageImpl decode(@Nullable JsonElement content, UUID id) {
        if (content == null) {
            throw new IllegalStateException("Missing content");
        }

        JsonObject obj = content.getAsJsonObject();
        if (!obj.has("channelId")) {
            throw new IllegalStateException("Incoming message has no 'channelId' argument: " + content);
        }
        if (!obj.has("payload")) {
            throw new IllegalStateException("Incoming message has no 'payload' argument: " + content);
        }

        String channelId = obj.get("channelId").getAsString();
        String payload = obj.get("payload").getAsString();

        return new CustomMessageImpl(id, channelId, payload);
    }

    private final String channelId;
    private final String payload;

    public CustomMessageImpl(UUID id, String channelId, String payload) {
        super(id);
        this.channelId = channelId;
        this.payload = payload;
    }

    @Override
    public @NotNull String getChannelId() {
        return this.channelId;
    }

    @Override
    public @NotNull String getPayload() {
        return this.payload;
    }

    @Override
    public @NotNull String asEncodedString() {
        return MultiMessagingService.encodeMessageAsString(
                TYPE, getId(), new JObject().add("channelId", this.channelId).add("payload", this.payload).toJson()
        );
    }
}