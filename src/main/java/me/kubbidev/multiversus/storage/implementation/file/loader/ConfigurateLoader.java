package me.kubbidev.multiversus.storage.implementation.file.loader;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.nio.file.Path;

/**
 * Wraps an object which can produce configurate {@link ConfigurationLoader}s.
 */
public interface ConfigurateLoader {

    ConfigurationLoader<? extends ConfigurationNode> loader(Path path);

}