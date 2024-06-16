package me.kubbidev.multiversus.sender;

import me.kubbidev.multiversus.plugin.MultiPlugin;
import net.multiversus.api.util.Tristate;
import net.kyori.adventure.text.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Factory class to make a thread-safe sender instance
 *
 * @param <P> the plugin type
 * @param <T> the command sender type
 */
public abstract class SenderFactory<P extends MultiPlugin, T> implements AutoCloseable {
    private final P plugin;

    public SenderFactory(P plugin) {
        this.plugin = plugin;
    }

    protected P getPlugin() {
        return this.plugin;
    }

    protected abstract UUID getUniqueId(T sender);

    protected abstract String getName(T sender);

    protected abstract void sendMessage(T sender, Component message);

    protected abstract Tristate getPermissionValue(T sender, String node);

    protected abstract boolean hasPermission(T sender, String node);

    protected abstract void performCommand(T sender, String command);

    protected abstract boolean isConsole(T sender);

    protected boolean consoleHasAllPermissions() {
        return true;
    }

    public final Sender wrap(T sender) {
        Objects.requireNonNull(sender, "sender");
        return new AbstractSender<>(this.plugin, this, sender);
    }

    @Override
    public void close() {

    }
}