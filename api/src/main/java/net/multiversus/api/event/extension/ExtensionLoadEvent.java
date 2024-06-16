package net.multiversus.api.event.extension;

import net.multiversus.api.event.MultiEvent;
import net.multiversus.api.event.util.Param;
import net.multiversus.api.extension.Extension;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an {@link Extension} is loaded.
 */
public interface ExtensionLoadEvent extends MultiEvent {

    /**
     * Gets the extension that was loaded.
     *
     * @return the extension
     */
    @Param(0)
    @NotNull Extension getExtension();

}