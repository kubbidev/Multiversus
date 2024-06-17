package me.kubbidev.multiversus.config;

import me.kubbidev.multiversus.config.generic.KeyedConfiguration;
import me.kubbidev.multiversus.config.generic.adapter.ConfigurationAdapter;
import me.kubbidev.multiversus.plugin.MultiPlugin;

import java.text.DecimalFormat;

public class MultiConfiguration extends KeyedConfiguration {
    private final MultiPlugin plugin;

    private DecimalFormat decimalFormat;
    private DecimalFormat decimalsFormat;

    public MultiConfiguration(MultiPlugin plugin, ConfigurationAdapter adapter) {
        super(adapter, ConfigKeys.getKeys());
        this.plugin = plugin;

        init();
    }

    @Override
    protected void load(boolean initial) {
        super.load(initial);
        this.decimalFormat = formatFrom("0.#");
        this.decimalsFormat = formatFrom("0.##");
    }

    @Override
    public void reload() {
        super.reload();
        getPlugin().getEventDispatcher().dispatchConfigReload();
    }

    public MultiPlugin getPlugin() {
        return this.plugin;
    }

    public DecimalFormat getDecimalFormat() {
        return this.decimalFormat;
    }

    public DecimalFormat getDecimalsFormat() {
        return this.decimalsFormat;
    }

    /**
     * The plugin mostly cache the return value of that method in fields
     * for easy access, therefore a server restart is required when editing the
     * decimal-separator option in the config
     *
     * @param pattern Something like "0.#"
     * @return New decimal format with the decimal separator given by the config.
     */
    public DecimalFormat formatFrom(String pattern) {
        return new DecimalFormat(pattern, get(ConfigKeys.DECIMAL_FORMAT_SEPARATOR));
    }
}
