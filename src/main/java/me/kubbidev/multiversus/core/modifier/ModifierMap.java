package me.kubbidev.multiversus.core.modifier;

import me.kubbidev.multiversus.core.util.Closeable;
import me.kubbidev.multiversus.core.util.EquipmentSlot;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class ModifierMap<T extends EntityModifier> {
    private final LivingEntity entity;

    /**
     * This hashmap encapsulate all {@link EntityModifier} present in this modifier map.
     */
    private final Map<UUID, T> modifiers = new HashMap<>();

    public ModifierMap(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    /**
     * @return The {@link T Modifier}s that have been manipulated so far since the
     * entity has spawn.
     * <p>
     * {@link T Modifier}s are completely flushed when the server restarts.
     */
    public Collection<T> getModifiers() {
        return this.modifiers.values();
    }

    public List<T> isolateModifiers(EquipmentSlot hand) {
        List<T> isolated = new ArrayList<>();

        for (T modifier : getModifiers()) {
            if (hand.isCompatible(modifier)) {
                isolated.add(modifier);
            }
        }
        return isolated;
    }

    public @Nullable T addModifier(T modifier) {
        return this.modifiers.put(modifier.getUniqueId(), modifier);
    }

    public @Nullable T removeModifier(UUID uuid) {
        @Nullable T removed = this.modifiers.remove(uuid);
        if (removed instanceof Closeable) {
            ((Closeable) removed).close();
        }
        return removed;
    }

    public void removeModifiers(String key) {
        Iterator<T> iterator = this.modifiers.values().iterator();
        while (iterator.hasNext()) {
            T skill = iterator.next();
            if (skill.getKey().equals(key)) {
                if (skill instanceof Closeable) {
                    ((Closeable) skill).close();
                }
                iterator.remove();
            }
        }
    }
}