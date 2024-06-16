package me.kubbidev.multiversus.storage.implementation.file.loader;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.yaml.snakeyaml.DumperOptions;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class YamlLoader implements ConfigurateLoader {

    @Override
    public ConfigurationLoader<? extends ConfigurationNode> loader(Path path) {
        return YAMLConfigurationLoader.builder()
                .setFlowStyle(DumperOptions.FlowStyle.BLOCK)
                .setIndent(2)
                .setSource(() -> Files.newBufferedReader(path, StandardCharsets.UTF_8))
                .setSink(() -> Files.newBufferedWriter(path, StandardCharsets.UTF_8))
                .build();
    }
}