package net.multiversus.api.platform;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Provides information about the platform Multiversus is running on.
 */
public interface Platform {

    /**
     * Gets the type of platform Multiversus is running on
     *
     * @return the type of platform Multiversus is running on
     */
    Platform.@NotNull Type getType();

    /**
     * Gets the unique players which have connected to the server since it started.
     *
     * @return the unique connections
     */
    @NotNull @Unmodifiable Set<UUID> getUniqueConnections();

    /**
     * Gets the time when the plugin first started.
     *
     * @return the enable time
     */
    @NotNull Instant getStartTime();

    /**
     * Represents a type of platform which Multiversus can run on.
     */
    enum Type {
        BUKKIT("Bukkit"),
        STANDALONE("Standalone");

        private final String friendlyName;

        Type(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        /**
         * Gets a readable name for the platform type.
         *
         * @return a readable name
         */
        public @NotNull String getFriendlyName() {
            return this.friendlyName;
        }
    }
}