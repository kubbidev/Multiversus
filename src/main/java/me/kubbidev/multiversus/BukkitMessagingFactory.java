package me.kubbidev.multiversus;

import me.kubbidev.multiversus.messaging.InternalMessagingService;
import me.kubbidev.multiversus.messaging.MessagingFactory;

public class BukkitMessagingFactory extends MessagingFactory<FBukkitPlugin> {
    public BukkitMessagingFactory(FBukkitPlugin plugin) {
        super(plugin);
    }

    @Override
    protected InternalMessagingService getServiceFor(String messagingType) {
        return super.getServiceFor(messagingType);
    }
}