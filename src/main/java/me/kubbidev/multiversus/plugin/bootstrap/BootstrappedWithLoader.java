package me.kubbidev.multiversus.plugin.bootstrap;

/**
 * A {@link MultiBootstrap} that was bootstrapped by a loader.
 */
public interface BootstrappedWithLoader {

    /**
     * Gets the loader object that did the bootstrapping.
     *
     * @return the loader
     */
    Object getLoader();
}