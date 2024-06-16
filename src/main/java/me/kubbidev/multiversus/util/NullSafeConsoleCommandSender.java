package me.kubbidev.multiversus.util;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The {@link Server#getConsoleSender()} method returns null during onEnable
 * in older CraftBukkit builds. This prevents Multiversus from loading correctly.
 */
public class NullSafeConsoleCommandSender implements ConsoleCommandSender {
    private final Server server;

    public NullSafeConsoleCommandSender(Server server) {
        this.server = server;
    }

    private Optional<ConsoleCommandSender> get() {
        //noinspection ConstantConditions
        return Optional.ofNullable(this.server.getConsoleSender());
    }

    @Override
    public void sendMessage(@NotNull String message) {
        Optional<ConsoleCommandSender> console = get();
        if (console.isPresent()) {
            console.get().sendMessage(message);
        } else {
            this.server.getLogger().info(ChatColor.stripColor(message));
        }
    }

    @Override
    public void sendMessage(@NotNull String... messages) {
        for (String msg : messages) {
            sendMessage(msg);
        }
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String... messages) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Server getServer() {
        return this.server;
    }

    @Override
    public @NotNull String getName() {
        return "CONSOLE";
    }

    @NotNull
    @Override
    public Spigot spigot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Component name() {
        return Component.text("CONSOLE");
    }

    @Override
    public boolean isConversing() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void acceptConversationInput(@NotNull String input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean beginConversation(@NotNull Conversation conversation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation, @NotNull ConversationAbandonedEvent details) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendRawMessage(@NotNull String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendRawMessage(@Nullable UUID sender, @NotNull String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPermissionSet(@NotNull String s) {
        return get().map(c -> c.isPermissionSet(s)).orElse(true);
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission permission) {
        return get().map(c -> c.isPermissionSet(permission)).orElse(true);
    }

    @Override
    public boolean hasPermission(@NotNull String s) {
        return get().map(c -> c.hasPermission(s)).orElse(true);
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        return get().map(c -> c.hasPermission(permission)).orElse(true);
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment attachment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recalculatePermissions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
        throw new UnsupportedOperationException();
    }
}
