package me.kubbidev.multiversus.storage.implementation.custom;

import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.storage.implementation.StorageImplementation;

/**
 * A storage provider
 */
@FunctionalInterface
public interface CustomStorageProvider {

    StorageImplementation provide(MultiPlugin plugin);

}