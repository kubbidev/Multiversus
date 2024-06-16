package me.kubbidev.multiversus.api.implementation;

import me.kubbidev.multiversus.messaging.InternalMessagingService;
import net.multiversus.api.messaging.MessagingService;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ApiMessagingService implements MessagingService {
    private final InternalMessagingService handle;

    public ApiMessagingService(InternalMessagingService handle) {
        this.handle = handle;
    }

    @Override
    public @NotNull String getName() {
        return this.handle.getName();
    }

    @Override
    public void sendCustomMessage(@NotNull String channelId, @NotNull String payload) {
        Objects.requireNonNull(channelId, "channelId");
        Objects.requireNonNull(payload, "payload");
        this.handle.pushCustomPayload(channelId, payload);
    }
}
