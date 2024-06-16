package me.kubbidev.multiversus.storage.misc;

import com.google.common.collect.ImmutableSet;
import net.multiversus.api.model.PlayerSaveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Represents the result to a player history save operation
 */
public final class PlayerSaveResultImpl implements PlayerSaveResult {
    private static final PlayerSaveResultImpl CLEAN_INSERT = new PlayerSaveResultImpl(Outcome.CLEAN_INSERT);
    private static final PlayerSaveResultImpl NO_CHANGE = new PlayerSaveResultImpl(Outcome.NO_CHANGE);

    public static PlayerSaveResultImpl cleanInsert() {
        return CLEAN_INSERT;
    }

    public static PlayerSaveResultImpl noChange() {
        return NO_CHANGE;
    }

    public static PlayerSaveResultImpl usernameUpdated(String oldUsername) {
        return new PlayerSaveResultImpl(EnumSet.of(Outcome.USERNAME_UPDATED), oldUsername, null);
    }

    public static PlayerSaveResultImpl determineBaseResult(String username, String oldUsername) {
        PlayerSaveResultImpl result;
        if (oldUsername == null) {
            result = PlayerSaveResultImpl.cleanInsert();
        } else if (oldUsername.equalsIgnoreCase(username)) {
            result = PlayerSaveResultImpl.noChange();
        } else {
            result = PlayerSaveResultImpl.usernameUpdated(oldUsername);
        }
        return result;
    }

    private final Set<Outcome> outcomes;
    private final @Nullable String previousUsername;
    private final @Nullable Set<UUID> otherUuids;

    private PlayerSaveResultImpl(EnumSet<Outcome> outcomes, @Nullable String previousUsername, @Nullable Set<UUID> otherUuids) {
        this.outcomes = ImmutableSet.copyOf(outcomes);
        this.previousUsername = previousUsername;
        this.otherUuids = otherUuids;
    }

    private PlayerSaveResultImpl(Outcome outcome) {
        this(EnumSet.of(outcome), null, null);
    }

    /**
     * Returns a new {@link PlayerSaveResultImpl} with the {@link Outcome#OTHER_UNIQUE_IDS_PRESENT_FOR_USERNAME}
     * status attached to the state of this result.
     *
     * @param otherUuids the other uuids
     * @return a new result
     */
    public PlayerSaveResultImpl withOtherUuidsPresent(@NotNull Set<UUID> otherUuids) {
        EnumSet<Outcome> outcomes = EnumSet.copyOf(this.outcomes);
        outcomes.add(Outcome.OTHER_UNIQUE_IDS_PRESENT_FOR_USERNAME);
        return new PlayerSaveResultImpl(outcomes, this.previousUsername, ImmutableSet.copyOf(otherUuids));
    }

    @Override
    public @NotNull Set<Outcome> getOutcomes() {
        return this.outcomes;
    }

    @Override
    public @Nullable String getPreviousUsername() {
        return this.previousUsername;
    }

    @Override
    public @Nullable Set<UUID> getOtherUniqueIds() {
        return this.otherUuids;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlayerSaveResultImpl)) {
            return false;
        }
        PlayerSaveResultImpl other = (PlayerSaveResultImpl) o;
        return Objects.equals(this.outcomes, other.outcomes) &&
                Objects.equals(this.previousUsername, other.previousUsername) &&
                Objects.equals(this.otherUuids, other.otherUuids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.outcomes, this.previousUsername, this.otherUuids);
    }

    @Override
    public String toString() {
        return "PlayerSaveResult(outcomes=" + this.outcomes + ", previousUsername=" + this.previousUsername + ", otherUuids=" + this.otherUuids + ")";
    }
}