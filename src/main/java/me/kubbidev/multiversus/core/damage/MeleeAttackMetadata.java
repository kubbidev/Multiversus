package me.kubbidev.multiversus.core.damage;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Used by melee attacks with melee weapons like custom or vanilla swords, axes...
 */
public class MeleeAttackMetadata extends AttackMetadata {

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
    public MeleeAttackMetadata(DamageMetadata metadata, LivingEntity target, @Nullable EntityMetadata attacker) {
        super(metadata, target, attacker);
    }
}