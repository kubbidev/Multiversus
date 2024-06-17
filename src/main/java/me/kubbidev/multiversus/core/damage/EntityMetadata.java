package me.kubbidev.multiversus.core.damage;

import com.google.common.base.Preconditions;
import me.kubbidev.multiversus.FBukkitPlugin;
import me.kubbidev.multiversus.core.util.EquipmentSlot;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class EntityMetadata {
    private final FBukkitPlugin plugin;
    private final LivingEntity entity;
    private final EquipmentSlot actionHand;

    public EntityMetadata(FBukkitPlugin plugin, LivingEntity entity, EquipmentSlot actionHand) {
        Preconditions.checkArgument(actionHand.isHand(), "Equipment slot must be a hand");
        this.plugin = plugin;
        this.entity = entity;
        this.actionHand = actionHand;
    }

    public FBukkitPlugin getPlugin() {
        return this.plugin;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public EquipmentSlot getActionHand() {
        return this.actionHand;
    }

    /**
     * Utility method that makes an entity deal damage to another specific entity.
     * <p>
     * This creates the attackMetadata based on the data stored by the CasterMetadata,
     * and calls it using damage manager.
     *
     * @param target The target entity.
     * @param damage The damage dealt.
     * @param types  The type of damage inflicted.
     * @return The (modified) attack metadata
     */
    public AttackMetadata attack(LivingEntity target, double damage, DamageType... types) {
        return attack(target, damage, null, types);
    }

    /**
     * Utility method that makes an entity deal damage to another specific entity.
     * <p>
     * This creates the attackMetadata based on the data stored by the CasterMetadata,
     * and calls it using damage manager.
     *
     * @param target  The target entity.
     * @param damage  The damage dealt.
     * @param element The damage element applied.
     * @param types   The type of damage inflicted.
     * @return The (modified) attack metadata
     */
    public AttackMetadata attack(LivingEntity target, double damage, @Nullable Element element, DamageType... types) {
        return attack(target, damage, true, element, types);
    }

    /**
     * Utility method that makes an entity deal damage to another specific entity.
     * <p>
     * This creates the attackMetadata based on the data stored by the CasterMetadata,
     * and calls it using damage manager.
     *
     * @param target    The target entity.
     * @param damage    The damage dealt.
     * @param element   The damage element applied.
     * @param knockback If should apply knockback.
     * @param types     The type of damage inflicted.
     * @return The (modified) attack metadata
     */
    public AttackMetadata attack(LivingEntity target, double damage, boolean knockback, @Nullable Element element, DamageType... types) {
        @Nullable AttackMetadata registeredAttack = this.plugin.getDamageManager().getRegisteredAttackMetadata(target);
        if (registeredAttack != null) {
            registeredAttack.getMetadata().add(damage, element, types);
            return registeredAttack;
        }

        DamageMetadata damageMetadata = new DamageMetadata(damage, element, types);
        AttackMetadata attackMetadata = new AttackMetadata(damageMetadata, target, this);

        this.plugin.getDamageManager().registerAttack(attackMetadata, knockback, false);
        return attackMetadata;
    }
}