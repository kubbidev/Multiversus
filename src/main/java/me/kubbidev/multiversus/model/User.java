package me.kubbidev.multiversus.model;

import me.kubbidev.multiversus.api.implementation.ApiUser;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class User {
    private final ApiUser apiProxy = new ApiUser(this);

    /**
     * Reference to the main plugin instance
     * @see #getPlugin()
     */
    private final MultiPlugin plugin;

    /**
     * The users Mojang UUID
     */
    private final UUID uniqueId;

    /**
     * The last known username of a player
     */
    private @Nullable String username = null;

    public User(UUID uniqueId, MultiPlugin plugin) {
        this.plugin = plugin;
        this.uniqueId = uniqueId;
    }

    // getters

    public MultiPlugin getPlugin() {
        return this.plugin;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public Optional<String> getUsername() {
        return Optional.ofNullable(this.username);
    }

    public Component getFormattedDisplayName() {
        return Component.text(getPlainDisplayName());
    }

    public String getPlainDisplayName() {
        return this.username != null ? this.username : this.uniqueId.toString();
    }

    public ApiUser getApiProxy() {
        return this.apiProxy;
    }

    /**
     * Sets the users name
     *
     * @param name the name to set
     * @param weak if true, the value will only be updated if a value hasn't been set previously.
     * @return true if a change was made
     */
    public boolean setUsername(String name, boolean weak) {
        if (name != null && name.length() > 16) {
            return false; // nope
        }

        // if weak is true, only update the value in the User if it's null
        if (weak && this.username != null) {

            // try to update casing if they're equalIgnoreCase
            if (this.username.equalsIgnoreCase(name)) {
                this.username = name;
            }

            return false;
        }

        // consistency. if the name being set is equivalent to null, just make it null.
        if (name != null && (name.isEmpty() || name.equalsIgnoreCase("null"))) {
            name = null;
        }

        // if one or the other is null, just update and return true
        if ((this.username == null) != (name == null)) {
            this.username = name;
            return true;
        }

        if (this.username == null) {
            // they're both null
            return false;
        } else {
            // both non-null
            if (this.username.equalsIgnoreCase(name)) {
                this.username = name; // update case anyway, but return false
                return false;
            } else {
                this.username = name;
                return true;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        User other = (User) o;
        return this.uniqueId.equals(other.uniqueId);
    }

    @Override
    public int hashCode() {
        return this.uniqueId.hashCode();
    }

    @Override
    public String toString() {
        return "User(uuid=" + this.uniqueId + ")";
    }
}
