package me.kubbidev.multiversus.event.gen;

import net.multiversus.api.Multiversus;
import net.multiversus.api.event.MultiEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;

/**
 * Abstract implementation of {@link MultiEvent}.
 */
public abstract class AbstractEvent implements MultiEvent {
    private final Multiversus api;

    protected AbstractEvent(Multiversus api) {
        this.api = api;
    }

    @Override
    public @NotNull Multiversus getMultiversus() {
        return this.api;
    }

    // Overridden by the subclass. Used by GeneratedEventClass to get setter method handles.
    public MethodHandles.Lookup mhl() {
        throw new UnsupportedOperationException();
    }
}