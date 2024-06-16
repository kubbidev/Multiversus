package me.kubbidev.multiversus.model.manager.user;

import me.kubbidev.multiversus.model.User;
import me.kubbidev.multiversus.plugin.MultiPlugin;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class StandardUserManager extends AbstractUserManager<User> {
    private final MultiPlugin plugin;

    public StandardUserManager(MultiPlugin plugin) {
        super(plugin, UserHousekeeper.timeoutSettings(1, TimeUnit.MINUTES));
        this.plugin = plugin;
    }

    @Override
    public User apply(UUID id) {
        return new User(id, this.plugin);
    }
}