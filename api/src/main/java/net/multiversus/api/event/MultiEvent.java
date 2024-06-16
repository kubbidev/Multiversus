package net.multiversus.api.event;

import net.multiversus.api.Multiversus;
import org.jetbrains.annotations.NotNull;

/**
 * A superinterface for all Multiversus events.
 */
public interface MultiEvent {

    /**
     * Get the API instance this event was dispatched from
     *
     * @return the api instance
     */
    @NotNull
    Multiversus getMultiversus();

    /**
     * Gets the type of the event.
     *
     * @return the type of the event
     */
    @NotNull Class<? extends MultiEvent> getEventType();

}