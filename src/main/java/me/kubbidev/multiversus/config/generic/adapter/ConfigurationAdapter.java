package me.kubbidev.multiversus.config.generic.adapter;

import me.kubbidev.multiversus.plugin.MultiPlugin;

import java.util.List;
import java.util.Map;

public interface ConfigurationAdapter {

    MultiPlugin getPlugin();

    void reload();

    String getString(String path, String def);

    int getInteger(String path, int def);

    boolean getBoolean(String path, boolean def);

    List<String> getStringList(String path, List<String> def);

    Map<String, String> getStringMap(String path, Map<String, String> def);

}