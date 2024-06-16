package me.kubbidev.multiversus.core.damage;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AttackMetadata {

    private final DamageMetadata metadata;
    private final LivingEntity target;

    @Nullable
    private final EntityMetadata attacker;

    /**
     * Used by {@link AttackHandler} instances to register attacks.
     * <p>
     * {@link DamageMetadata} only gives information about the attack damage and types while
     * this class also contains info about the damager.
     * <p>
     * Some plugins don't let Multiversus determine what the damager is so there might
     * be problem with damage/reduction stat application.
     *
     * @param metadata The attack result.
     * @param target   The entity that received the damage.
     * @param attacker The entity who dealt the damage.
     */
    public AttackMetadata(DamageMetadata metadata, LivingEntity target, @Nullable EntityMetadata attacker) {
        this.metadata = Objects.requireNonNull(metadata, "Damage cannot be null");
        this.target = target;
        this.attacker = attacker;
    }

    public DamageMetadata getMetadata() {
        return this.metadata;
    }

    public LivingEntity getTarget() {
        return this.target;
    }

    public @Nullable EntityMetadata getAttacker() {
        return this.attacker;
    }

    public boolean hasAttacker() {
        return this.attacker != null;
    }
}