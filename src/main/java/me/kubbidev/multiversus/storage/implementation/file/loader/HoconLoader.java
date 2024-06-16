package me.kubbidev.multiversus.storage.implementation.file.loader;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class HoconLoader implements ConfigurateLoader {

    @Override
    public ConfigurationLoader<? extends ConfigurationNode> loader(Path path) {
        return HoconConfigurationLoader.builder()
                .setSource(() -> Files.newBufferedReader(path, StandardCharsets.UTF_8))
                .setSink(() -> Files.newBufferedWriter(path, StandardCharsets.UTF_8))
                .build();
    }
}