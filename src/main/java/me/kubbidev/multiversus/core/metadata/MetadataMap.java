package me.kubbidev.multiversus.core.metadata;

import com.google.common.collect.ImmutableMap;
import me.kubbidev.multiversus.core.metadata.value.TransientValue;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A map which holds {@link MetadataKey}s, and maps them to persistent or transient (expiring / weak) values.
 * <p>
 * Transient values will only be removed during map maintenance, which occurs during a housekeeping task running
 * every minute. Method signatures for the {@link #has(MetadataKey)} and equivalent remain accurate though.
 *
 * @see TransientValue
 */
public interface MetadataMap {

    static MetadataMap create() {
        return new MetadataMapImpl();
    }

    /**
     * Adds a metadata key and corresponding value into this map, removing any previous value if present.
     * <p>
     * If a MetadataKey with the same id is already contained within this map, a {@link ClassCastException} will be
     * thrown if they do not share the same type, and the existing value will remain.
     *
     * @param key   the key to put
     * @param value the value to map against (non null)
     * @param <T>   the value type
     * @throws ClassCastException if any existing key with the same id has a differing type to the key being add
     */
    <T> void put(MetadataKey<T> key, T value);

    /**
     * Adds a metadata key and corresponding value into this map, removing any previous value if present.
     * <p>
     * If a MetadataKey with the same id is already contained within this map, a {@link ClassCastException} will be
     * thrown if they do not share the same type, and the existing value will remain.
     *
     * @param key   the key to put
     * @param value the value to map against (non null)
     * @param <T>   the value type
     * @throws ClassCastException if any existing key with the same id has a differing type to the key being add
     */
    <T> void put(MetadataKey<T> key, TransientValue<T> value);

    /**
     * Adds a metadata key and corresponding value into this map, removing any previous value if present.
     * <p>
     * Unlike {@link #put(MetadataKey, Object)}, the existing value if present does not need to have the same type.
     *
     * @param key   the key to put
     * @param value the value to map against (non null)
     * @param <T>   the value type
     */
    <T> void forcePut(MetadataKey<T> key, T value);

    /**
     * Adds a metadata key and corresponding value into this map, removing any previous value if present.
     * <p>
     * Unlike {@link #put(MetadataKey, Object)}, the existing value if present does not need to have the same type.
     *
     * @param key   the key to put
     * @param value the value to map against (non null)
     * @param <T>   the value type
     */
    <T> void forcePut(MetadataKey<T> key, TransientValue<T> value);

    /**
     * Adds a metadata key and corresponding value into this map, only if an existing key is not present.
     *
     * @param key   the key to put
     * @param value the value to map against (non null)
     * @param <T>   the value type
     * @return true if there wasn't an existing key, and the key was added
     */
    <T> boolean putIfAbsent(MetadataKey<T> key, T value);

    /**
     * Adds a metadata key and corresponding value into this map, only if an existing key is not present.
     *
     * @param key   the key to put
     * @param value the value to map against (non null)
     * @param <T>   the value type
     * @return true if there wasn't an existing key, and the key was added
     */
    <T> boolean putIfAbsent(MetadataKey<T> key, TransientValue<T> value);

    /**
     * Gets an optional value for the given key.
     *
     * @param key the metadata key to get
     * @param <T> the value type
     * @return an optional containing the value
     * @throws ClassCastException if there is a key held in the map with the same id but differing type to the given key.
     */
    <T> Optional<T> get(MetadataKey<T> key);

    /**
     * Attempts to get a value for the given key, and applies the action is present.
     *
     * @param key    the metadata key to lookup
     * @param action the action to apply
     * @param <T>    the value type
     * @return true if the action was applied
     * @throws ClassCastException if there is a key held in the map with the same id but differing type to the given key.
     */
    <T> boolean ifPresent(MetadataKey<T> key, Consumer<? super T> action);

    /**
     * Gets a value for the given key, or null if one isn't present.
     *
     * @param key the metadata key to get
     * @param <T> the value type
     * @return the value, or null if no key is present.
     * @throws ClassCastException if there is a key held in the map with the same id but differing type to the given key.
     */
    @Nullable
    <T> T getOrNull(MetadataKey<T> key);

    /**
     * Gets a value for the given key, or returns the default if one isn't present.
     *
     * @param key the metadata key to get
     * @param def the default value
     * @param <T> the value type
     * @return the value, or the default if no key is present.
     * @throws ClassCastException if there is a key held in the map with the same id but differing type to the given key.
     */
    <T> T getOrDefault(MetadataKey<T> key, @Nullable T def);

    /**
     * Gets a value for the given key, or puts and returns the default if one isn't present.
     *
     * @param key the metadata key to get
     * @param def the default value
     * @param <T> the value type
     * @return the value, or the supplied value if no key is present.
     * @throws ClassCastException if there is a key held in the map with the same id but differing type to the given key.
     */
    <T> T getOrPut(MetadataKey<T> key, Supplier<? extends T> def);

    /**
     * Gets a value for the given key, or puts and returns the default if one isn't present.
     *
     * @param key the metadata key to get
     * @param def the default value
     * @param <T> the value type
     * @return the value, or the supplied value if no key is present.
     * @throws ClassCastException if there is a key held in the map with the same id but differing type to the given key.
     */
    <T> T getOrPutExpiring(MetadataKey<T> key, Supplier<? extends TransientValue<T>> def);

    /**
     * Returns if this map contains the given key, and the types of each key match.
     *
     * @param key the key to check for
     * @return true if this map contains the key
     */
    boolean has(MetadataKey<?> key);

    /**
     * Removes the given key from this map
     *
     * @param key the key to remove
     * @return true if a value was removed from the map
     */
    boolean remove(MetadataKey<?> key);

    /**
     * Clears the map
     */
    void clear();

    /**
     * Returns an immutable view of the backing map
     *
     * @return an immutable view of the backing map
     */
    ImmutableMap<MetadataKey<?>, Object> asMap();

    /**
     * Returns if the map is empty
     *
     * @return true if the map is empty
     */
    boolean isEmpty();

    /**
     * Removes expired {@link TransientValue}s from this map.
     *
     * <p>Note that this method does need to be explicitly called - as cleanup happens naturally over time.</p>
     */
    void cleanup();
}