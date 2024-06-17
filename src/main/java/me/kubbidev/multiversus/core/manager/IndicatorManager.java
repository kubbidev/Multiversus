package me.kubbidev.multiversus.core.manager;

import me.kubbidev.multiversus.FBukkitPlugin;
import me.kubbidev.multiversus.config.ConfigKeys;
import me.kubbidev.multiversus.core.listener.indicator.type.DamageIndicator;
import me.kubbidev.multiversus.core.listener.indicator.type.RegenerationIndicator;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;

public final class IndicatorManager {
    private final List<Listener> indicatorsListeners = new ArrayList<>();

    /**
     * Register all indicators listeners and add them to the list.
     */
    public void load(FBukkitPlugin plugin) {
        PluginManager manager = Bukkit.getPluginManager();

        if (plugin.getConfiguration().get(ConfigKeys.INDICATOR_DAMAGE_ENABLED)) {
            try {
                Listener listener = new DamageIndicator(plugin);
                manager.registerEvents(listener, plugin.getLoader());

                this.indicatorsListeners.add(listener);
            } catch (RuntimeException e) {
                plugin.getLogger().warn("Could not load damage indicators: " + e.getMessage());
            }
        }
        if (plugin.getConfiguration().get(ConfigKeys.INDICATOR_REGENERATION_ENABLED)) {
            try {
                Listener listener = new RegenerationIndicator(plugin);
                manager.registerEvents(listener, plugin.getLoader());

                this.indicatorsListeners.add(listener);
            } catch (RuntimeException e) {
                plugin.getLogger().warn("Could not load regeneration indicators: " + e.getMessage());
            }
        }
    }

    /**
     * Unregister all listeners, remove them from the list and call the
     * {@link IndicatorManager#load(FBukkitPlugin)} method.
     */
    public void reload(FBukkitPlugin plugin) {
        // unregister listeners
        this.indicatorsListeners.forEach(HandlerList::unregisterAll);
        this.indicatorsListeners.clear();

        // register listeners
        load(plugin);
    }
}