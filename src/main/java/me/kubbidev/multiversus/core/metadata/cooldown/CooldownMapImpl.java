package me.kubbidev.multiversus.core.metadata.cooldown;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class CooldownMapImpl<T> implements CooldownMap<T> {
    private final Map<T, Cooldown> cache = new HashMap<>();

    @Override
    public Optional<Cooldown> get(T key) {
        Objects.requireNonNull(key, "key");
        return this.cache.containsKey(key)
                ? Optional.ofNullable(this.cache.get(key))
                : Optional.empty();
    }

    @Override
    public void put(T key, Cooldown cooldown) {
        Objects.requireNonNull(key, "key");
        this.cache.put(key, cooldown);
    }

    @Override
    public Map<T, Cooldown> getAll() {
        return this.cache;
    }
}