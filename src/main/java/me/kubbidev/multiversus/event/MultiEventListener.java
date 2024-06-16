package me.kubbidev.multiversus.event;

import net.multiversus.api.event.EventBus;
import net.multiversus.api.event.MultiEvent;

/**
 * Defines a class which listens to {@link MultiEvent}s.
 */
public interface MultiEventListener {

    void bind(EventBus bus);

}