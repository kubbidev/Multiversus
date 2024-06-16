package me.kubbidev.multiversus.api.implementation;

import com.google.common.base.Preconditions;
import me.kubbidev.multiversus.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ApiUser implements net.multiversus.api.model.user.User {
    public static User cast(net.multiversus.api.model.user.User u) {
        Preconditions.checkState(u instanceof ApiUser, "Illegal instance " + u.getClass() + " cannot be handled by this implementation.");
        return ((ApiUser) u).getHandle();
    }

    private final User handle;

    public ApiUser(User handle) {
        this.handle = handle;
    }

    User getHandle() {
        return this.handle;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return this.handle.getUniqueId();
    }

    @Override
    public @Nullable String getUsername() {
        return this.handle.getUsername().orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ApiUser)) {
            return false;
        }
        ApiUser other = (ApiUser) o;
        return this.handle.equals(other.handle);
    }

    @Override
    public int hashCode() {
        return this.handle.hashCode();
    }
}
