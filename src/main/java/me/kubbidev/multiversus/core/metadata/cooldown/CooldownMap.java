package me.kubbidev.multiversus.core.metadata.cooldown;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

/**
 * A self-populating map of cooldown instances
 *
 * @param <T> the type
 */
public interface CooldownMap<T> {

    /**
     * Creates a new collection
     *
     * @return a new collection
     */
    static <T> CooldownMap<T> create() {
        return new CooldownMapImpl<>();
    }

    /**
     * Gets the internal cooldown instance associated with the given key.
     *
     * <p>The inline Cooldown methods in this class should be used to access the functionality of the cooldown as opposed
     * to calling the methods directly via the instance returned by this method.</p>
     *
     * @param key the key
     * @return a cooldown instance
     */
    Optional<Cooldown> get(T key);

    void put(T key, Cooldown cooldown);

    /**
     * Gets the cooldowns contained within this collection.
     *
     * @return the backing map
     */
    Map<T, Cooldown> getAll();

    /* methods from Cooldown */

    default boolean test(T key) {
        return get(key).map(Cooldown::test).orElse(true);
    }

    default boolean testSilently(T key) {
        return get(key).map(Cooldown::test).orElse(true);
    }

    default long elapsed(T key) {
        return get(key).map(Cooldown::elapsed).orElse(0L);
    }

    default void reset(T key) {
        get(key).ifPresent(Cooldown::reset);
    }

    default long remainingMillis(T key) {
        return get(key).map(Cooldown::remainingMillis).orElse(0L);
    }

    default long remainingTime(T key, TimeUnit unit) {
        return get(key).map(cooldown -> cooldown.remainingTime(unit)).orElse(0L);
    }

    default OptionalLong getLastTested(T key) {
        return get(key).map(Cooldown::getLastTested).orElse(OptionalLong.empty());
    }

    default void setLastTested(T key, long time) {
        get(key).ifPresent(cooldown -> cooldown.setLastTested(time));
    }

}