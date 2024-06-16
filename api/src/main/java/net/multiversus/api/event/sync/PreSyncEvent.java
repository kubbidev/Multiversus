package net.multiversus.api.event.sync;

import net.multiversus.api.event.MultiEvent;
import net.multiversus.api.event.type.Cancellable;

/**
 * Called just before a full synchronisation task runs.
 */
public interface PreSyncEvent extends MultiEvent, Cancellable {

}