package me.kubbidev.multiversus.core.metadata.cooldown;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Range;

import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

/**
 * A simple cooldown abstraction
 */
public interface Cooldown {

    /**
     * Creates a cooldown lasting a specified amount of time
     *
     * @param amount the amount of time
     * @param unit   the unit of time
     * @return a new cooldown
     */
    static Cooldown of(long amount, TimeUnit unit) {
        return new CooldownImpl(amount, unit);
    }

    /**
     * Returns true if the cooldown is not active, and then resets the timer
     *
     * <p>If the cooldown is currently active, the timer is <strong>not</strong> reset.</p>
     *
     * @return true if the cooldown is not active
     */
    default boolean test() {
        if (!testSilently()) {
            return false;
        }

        reset();
        return true;
    }

    /**
     * Returns true if the cooldown is not active
     *
     * @return true if the cooldown is not active
     */
    default boolean testSilently() {
        return elapsed() > getTimeout();
    }

    /**
     * Returns the elapsed time in milliseconds since the cooldown was last reset, or since creation time
     *
     * @return the elapsed time
     */
    default long elapsed() {
        return System.currentTimeMillis() - getLastTested().orElse(0);
    }

    /**
     * Resets the cooldown
     */
    default void reset() {
        setLastTested(System.currentTimeMillis());
    }

    /**
     * Gets the time in milliseconds until the cooldown will become inactive.
     *
     * <p>If the cooldown is not active, this method returns <code>0</code>.</p>
     *
     * @return the time in millis until the cooldown will expire
     */
    default long remainingMillis() {
        long diff = elapsed();
        return diff > getTimeout() ? 0L : getTimeout() - diff;
    }

    /**
     * Gets the time until the cooldown will become inactive.
     *
     * <p>If the cooldown is not active, this method returns <code>0</code>.</p>
     *
     * @param unit the unit to return in
     * @return the time until the cooldown will expire
     */
    default long remainingTime(TimeUnit unit) {
        return Math.max(0L, unit.convert(remainingMillis(), TimeUnit.MILLISECONDS));
    }

    /**
     * Reduces the remaining cooldown time by a percentage.
     *
     * @param p the percentage by which to reduce the remaining cooldown, in the range from 0 to 1
     */
    default void reduceRemainingCooldown(@Range(from = 0, to = 1) float p) {
        Preconditions.checkArgument(p >= 0 && p <= 1, "percentage must be between 0 and 1");

        long remaining = remainingMillis();
        OptionalLong lastTestedOptional = getLastTested();
        if (!lastTestedOptional.isPresent()) {
            // if lastTested is not set, there's nothing to reduce
            return;
        }

        long lastTested = lastTestedOptional.getAsLong();
        long reductionMillis = (long) (remaining * p);

        // calculate the new last tested time, ensuring it does not go below 0
        long newLastTested = Math.max(0, lastTested - reductionMillis);
        setLastTested(newLastTested);
    }

    /**
     * Reduces the initial cooldown time by a percentage.
     *
     * @param p the percentage by which to reduce the initial cooldown, in the range from 0 to 1
     */
    default void reduceInitialCooldown(@Range(from = 0, to = 1) float p) {
        Preconditions.checkArgument(p >= 0 && p <= 1, "percentage must be between 0 and 1");

        long initialTimeout = getTimeout();
        OptionalLong lastTestedOptional = getLastTested();
        if (!lastTestedOptional.isPresent()) {
            // if lastTested is not set, there's nothing to reduce
            return;
        }

        long lastTested = lastTestedOptional.getAsLong();
        long reductionMillis = (long) (initialTimeout * p);

        // calculate the new last tested time, ensuring it does not go below 0
        long newLastTested = Math.max(0, lastTested - reductionMillis);
        setLastTested(newLastTested);
    }

    /**
     * Reduces the remaining cooldown time by a flat amount.
     *
     * @param d the amount by which to reduce the remaining cooldown
     */
    default void reduceFlat(@Range(from = 0, to = Long.MAX_VALUE) float d) {
        Preconditions.checkArgument(d >= 0, "Reduction amount must be non-negative");
        OptionalLong lastTestedOptional = getLastTested();
        if (!lastTestedOptional.isPresent()) {
            // if lastTested is not set, there's nothing to reduce
            return;
        }

        long lastTested = lastTestedOptional.getAsLong();
        long reductionMillis = (long) (d * 1000L);

        // calculate the new last tested time, ensuring it does not go below 0
        long newLastTested = Math.max(0, lastTested - reductionMillis);
        setLastTested(newLastTested);
    }

    /**
     * Return the time in milliseconds when this cooldown was last {@link #test()}ed.
     *
     * @return the last call time
     */
    OptionalLong getLastTested();

    /**
     * Sets the time in milliseconds when this cooldown was last tested.
     *
     * <p>Note: this should only be used when re-constructing a cooldown
     * instance. Use {@link #test()} otherwise.</p>
     *
     * @param time the time
     */
    void setLastTested(long time);

    /**
     * Gets the timeout in milliseconds for this cooldown
     *
     * @return the timeout in milliseconds
     */
    long getTimeout();

    /**
     * Copies the properties of this cooldown to a new instance
     *
     * @return a cloned cooldown instance
     */
    Cooldown copy();
}