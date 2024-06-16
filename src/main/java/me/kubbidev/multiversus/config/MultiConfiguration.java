package me.kubbidev.multiversus.config;

import me.kubbidev.multiversus.config.generic.KeyedConfiguration;
import me.kubbidev.multiversus.config.generic.adapter.ConfigurationAdapter;
import me.kubbidev.multiversus.plugin.MultiPlugin;

public class MultiConfiguration extends KeyedConfiguration {
    private final MultiPlugin plugin;

    public MultiConfiguration(MultiPlugin plugin, ConfigurationAdapter adapter) {
        super(adapter, ConfigKeys.getKeys());
        this.plugin = plugin;

        init();
    }

    @Override
    public void reload() {
        super.reload();
        getPlugin().getEventDispatcher().dispatchConfigReload();
    }

    public MultiPlugin getPlugin() {
        return this.plugin;
    }
}
