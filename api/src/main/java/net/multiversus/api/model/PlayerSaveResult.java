package net.multiversus.api.model;

import net.multiversus.api.model.user.UserManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Encapsulates the result of an operation to save uuid data about a player.
 *
 * <p>The corresponding method can be found at
 * {@link UserManager#savePlayerData(UUID, String)}.</p>
 */
public interface PlayerSaveResult {

    /**
     * Gets the status returned by the operation
     *
     * @return the status
     */
    @NotNull @Unmodifiable Set<Outcome> getOutcomes();

    /**
     * Gets if the result includes a certain outcome.
     *
     * @param outcome the outcome to check for
     * @return if the result includes the outcome
     */
    default boolean includes(@NotNull Outcome outcome) {
        Objects.requireNonNull(outcome, "outcome");
        return getOutcomes().contains(outcome);
    }

    /**
     * Gets the previous username involved in the result.
     *
     * <p>Returns null when the result doesn't {@link #includes(Outcome) include} the
     * {@link Outcome#USERNAME_UPDATED} status.</p>
     *
     * @return the previous username
     * @see Outcome#USERNAME_UPDATED
     */
    @Nullable String getPreviousUsername();

    /**
     * Gets the other uuids involved in the result.
     *
     * <p>Returns null when the result doesn't {@link #includes(Outcome) include} the
     * {@link Outcome#OTHER_UNIQUE_IDS_PRESENT_FOR_USERNAME} status.</p>
     *
     * @return the other uuids
     * @see Outcome#OTHER_UNIQUE_IDS_PRESENT_FOR_USERNAME
     */
    @Nullable @Unmodifiable Set<UUID> getOtherUniqueIds();

    /**
     * The various states the result can take
     */
    enum Outcome {

        /**
         * There was no existing data saved for either the uuid or username
         */
        CLEAN_INSERT,

        /**
         * There was existing data for the player, no change was needed.
         */
        NO_CHANGE,

        /**
         * There was already a record for the UUID saved, but it was for a different username.
         *
         * <p>This is normal, players are able to change their usernames.</p>
         */
        USERNAME_UPDATED,

        /**
         * There was already a record for the username saved, but it was under a different uuid.
         *
         * <p>This is a bit of a cause for concern. It's possible that "player1" has changed
         * their username to "player2", and "player3" has changed their username to "player1".
         * If the original "player1" doesn't join after changing their name, this conflict could
         * occur.</p>
         *
         * <p>However, what's more likely is that the server is not setup to authenticate
         * correctly. Usually this is a problem with BungeeCord "ip-forwarding", but could be
         * that the user of the plugin is running a network off a shared database with one
         * server in online mode and another in offline mode.</p>
         */
        OTHER_UNIQUE_IDS_PRESENT_FOR_USERNAME,
    }
}