package me.kubbidev.multiversus.api.implementation;

import me.kubbidev.multiversus.plugin.MultiPlugin;

public abstract class ApiAbstractManager<I, E, H> {
    protected final MultiPlugin plugin;
    protected final H handle;

    protected ApiAbstractManager(MultiPlugin plugin, H handle) {
        this.plugin = plugin;
        this.handle = handle;
    }

    protected abstract E proxy(I internal);

}