package me.kubbidev.multiversus.core.event.indicator;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class IndicatorDisplayEvent extends EntityEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private boolean cancelled;
    private final IndicatorType type;

    private Component message;

    /**
     * Called when an entity emits either a damage or a healing indicator.
     *
     * @param entity  The entity emitting the indicator
     * @param message The message displayed
     * @param type    The type of indicator
     */
    public IndicatorDisplayEvent(Entity entity, Component message, IndicatorType type) {
        super(entity);
        this.message = message;
        this.type = type;
    }

    public IndicatorType getType() {
        return this.type;
    }

    public Component getMessage() {
        return this.message;
    }

    public void setMessage(Component message) {
        this.message = message;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public enum IndicatorType {

        /**
         * Displayed when an entity is being damaged
         */
        DAMAGE,

        /**
         * Displayed when an entity regenerates some health
         */
        REGENERATION
    }
}