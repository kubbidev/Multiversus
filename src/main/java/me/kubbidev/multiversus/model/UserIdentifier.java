package me.kubbidev.multiversus.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Used to identify a specific {@link User}.
 */
public final class UserIdentifier {

    /**
     * Creates a {@link UserIdentifier}.
     *
     * @param uniqueId the uuid of the user
     * @param username the username of the user, nullable
     * @return a new identifier
     */
    public static UserIdentifier of(@NotNull UUID uniqueId, @Nullable String username) {
        Objects.requireNonNull(uniqueId, "uuid");
        if (username == null || username.equalsIgnoreCase("null") || username.isEmpty()) {
            username = null;
        }

        return new UserIdentifier(uniqueId, username);
    }

    private final UUID uniqueId;
    private final String username;

    private UserIdentifier(UUID uniqueId, String username) {
        this.uniqueId = uniqueId;
        this.username = username;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public Optional<String> getUsername() {
        return Optional.ofNullable(this.username);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof UserIdentifier)) {
            return false;
        }
        UserIdentifier other = (UserIdentifier) o;
        return this.uniqueId.equals(other.uniqueId);
    }

    @Override
    public int hashCode() {
        return this.uniqueId.hashCode();
    }

    @Override
    public String toString() {
        return "UserIdentifier(uniqueId=" + this.uniqueId + ", username=" + this.username + ")";
    }
}