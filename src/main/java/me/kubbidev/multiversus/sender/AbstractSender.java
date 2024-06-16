package me.kubbidev.multiversus.sender;

import com.google.common.collect.Iterables;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import net.multiversus.api.util.Tristate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;

import java.util.*;

/**
 * Simple implementation of {@link Sender} using a {@link SenderFactory}
 *
 * @param <T> the command sender type
 */
public final class AbstractSender<T> implements Sender {
    private final MultiPlugin plugin;
    private final SenderFactory<?, T> factory;
    private final T sender;

    private final UUID uniqueId;
    private final String name;
    private final boolean isConsole;

    AbstractSender(MultiPlugin plugin, SenderFactory<?, T> factory, T sender) {
        this.plugin = plugin;
        this.factory = factory;
        this.sender = sender;
        this.uniqueId = factory.getUniqueId(this.sender);
        this.name = factory.getName(this.sender);
        this.isConsole = this.factory.isConsole(this.sender);
    }

    @Override
    public MultiPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void sendMessage(Component message) {
        if (isConsole()) {
            for (Component line : splitNewlines(message)) {
                this.factory.sendMessage(this.sender, line);
            }
        } else {
            this.factory.sendMessage(this.sender, message);
        }
    }

    @Override
    public Tristate getPermissionValue(String permission) {
        return (isConsole() && this.factory.consoleHasAllPermissions()) ? Tristate.TRUE : this.factory.getPermissionValue(this.sender, permission);
    }

    @Override
    public boolean hasPermission(String permission) {
        return (isConsole() && this.factory.consoleHasAllPermissions()) || this.factory.hasPermission(this.sender, permission);
    }

    @Override
    public void performCommand(String commandLine) {
        this.factory.performCommand(this.sender, commandLine);
    }

    @Override
    public boolean isConsole() {
        return this.isConsole;
    }

    @Override
    public boolean isValid() {
        return isConsole() || this.plugin.getBootstrap().isPlayerOnline(this.uniqueId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AbstractSender<?>)) {
            return false;
        }
        AbstractSender<?> other = (AbstractSender<?>) o;
        return this.getUniqueId().equals(other.getUniqueId());
    }

    @Override
    public int hashCode() {
        return this.uniqueId.hashCode();
    }

    // A small utility method which splits components built using
    // > join(newLine(), components...)
    // back into separate components.
    public static Iterable<Component> splitNewlines(Component message) {
        if (message instanceof TextComponent && message.style().isEmpty() && !message.children().isEmpty() && ((TextComponent) message).content().isEmpty()) {
            LinkedList<List<Component>> split = new LinkedList<>();
            split.add(new ArrayList<>());

            for (Component child : message.children()) {
                if (Component.newline().equals(child)) {
                    split.add(new ArrayList<>());
                } else {
                    Iterator<Component> splitChildren = splitNewlines(child).iterator();
                    if (splitChildren.hasNext()) {
                        split.getLast().add(splitChildren.next());
                    }
                    while (splitChildren.hasNext()) {
                        split.add(new ArrayList<>());
                        split.getLast().add(splitChildren.next());
                    }
                }
            }

            return Iterables.transform(split, input -> {
                switch (input.size()) {
                    case 0:
                        return Component.empty();
                    case 1:
                        return input.get(0);
                    default:
                        return Component.join(JoinConfiguration.noSeparators(), input);
                }
            });
        }

        return Collections.singleton(message);
    }
}