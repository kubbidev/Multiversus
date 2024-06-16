package me.kubbidev.multiversus.core.event;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

/**
 * Represents an Living Entity-related event.
 */
public abstract class LivingEntityEvent extends Event {
    protected final LivingEntity entity;

    public LivingEntityEvent(LivingEntity what) {
        this.entity = what;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    /**
     * Gets the EntityType of the Entity involved in this event.
     *
     * @return EntityType of the Entity involved in this event
     */
    public EntityType getEntityType() {
        return this.entity.getType();
    }
}