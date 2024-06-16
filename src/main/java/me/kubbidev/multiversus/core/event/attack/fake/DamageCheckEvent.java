package me.kubbidev.multiversus.core.event.attack.fake;

import me.kubbidev.multiversus.core.interaction.InteractionType;
import org.bukkit.entity.Entity;

public class DamageCheckEvent extends FakeEntityDamageByEntityEvent {
    private final InteractionType interactionType;

    /**
     * This is the fake event used to determine if an entity can hit ANY entity.
     *
     * @param damager The entity damaging the other entity.
     * @param victim  The entity being attacked.
     * @param type    The interaction type check for this event.
     */
    public DamageCheckEvent(Entity damager, Entity victim, InteractionType type) {
        super(damager, victim, DamageCause.ENTITY_ATTACK, 0);
        this.interactionType = type;
    }

    public InteractionType getInteractionType() {
        return this.interactionType;
    }
}