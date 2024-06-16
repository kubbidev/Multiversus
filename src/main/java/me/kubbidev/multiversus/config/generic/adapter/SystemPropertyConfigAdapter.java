package me.kubbidev.multiversus.config.generic.adapter;

import me.kubbidev.multiversus.config.ConfigKeys;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import org.jetbrains.annotations.Nullable;

public class SystemPropertyConfigAdapter extends StringBasedConfigurationAdapter {
    private static final String PREFIX = "multiversus.";

    private final MultiPlugin plugin;

    public SystemPropertyConfigAdapter(MultiPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected @Nullable String resolveValue(String path) {
        // e.g.
        // 'server'            -> multiversus.server
        // 'data.table_prefix' -> multiversus.data.table-prefix
        String key = PREFIX + path;

        String value = System.getProperty(key);
        if (value != null) {
            String printableValue = ConfigKeys.shouldCensorValue(path) ? "*****" : value;
            this.plugin.getLogger().info(String.format("Resolved configuration value from system property: %s = %s", key, printableValue));
        }
        return value;
    }

    @Override
    public MultiPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public void reload() {
        // no-op
    }
}