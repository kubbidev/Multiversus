package me.kubbidev.multiversus.config.generic.adapter;

import me.kubbidev.multiversus.config.ConfigKeys;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class EnvironmentVariableConfigAdapter extends StringBasedConfigurationAdapter {
    private static final String PREFIX = "MULTIVERSUS_";

    private final MultiPlugin plugin;

    public EnvironmentVariableConfigAdapter(MultiPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected @Nullable String resolveValue(String path) {
        // e.g.
        // 'server'            -> MULTIVERSUS_SERVER
        // 'data.table_prefix' -> MULTIVERSUS_DATA_TABLE_PREFIX
        String key = PREFIX + path.toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace('.', '_');

        String value = System.getenv(key);
        if (value != null) {
            String printableValue = ConfigKeys.shouldCensorValue(path) ? "*****" : value;
            this.plugin.getLogger().info(String.format("Resolved configuration value from environment variable: %s = %s", key, printableValue));
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