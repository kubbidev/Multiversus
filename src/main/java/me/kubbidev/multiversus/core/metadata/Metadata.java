package me.kubbidev.multiversus.core.metadata;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.google.common.base.Preconditions;
import me.kubbidev.multiversus.core.metadata.registry.MetadataRegistry;
import me.kubbidev.multiversus.core.metadata.registry.type.EntityMetadataRegistry;
import me.kubbidev.multiversus.core.metadata.registry.type.PlayerMetadataRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides access to {@link MetadataRegistry} instances bound to players and entities.
 */
public final class Metadata extends BukkitRunnable implements Listener {
    private Metadata() {
    }

    /**
     * Singleton instance of metadata housekeeper
     */
    private static Metadata housekeeper = null;

    @Override
    public void run() {
        for (MetadataRegistry<?> registry : StandardMetadataRegistries.values()) {
            registry.cleanup();
        }
    }

    @EventHandler
    public void unregisterEntity(EntityRemoveFromWorldEvent e) {
        // remove entity metadata when they leave the world (kill or discard)
        StandardMetadataRegistries.ENTITY.remove(e.getEntity().getUniqueId());
    }

    public static void setupHousekeeper(JavaPlugin plugin) {
        Preconditions.checkArgument(housekeeper == null, "Metadata already setup");

        housekeeper = new Metadata();
        housekeeper.runTaskTimerAsynchronously(plugin, 0, 1200);
        // register listener events to the bukkit plugin manager instance.
        Bukkit.getPluginManager().registerEvents(housekeeper, plugin);
    }

    /**
     * Gets the {@link MetadataRegistry} for {@link Player}s.
     *
     * @return the {@link PlayerMetadataRegistry}
     */
    public static PlayerMetadataRegistry players() {
        return StandardMetadataRegistries.PLAYER;
    }

    /**
     * Gets the {@link MetadataRegistry} for {@link Entity}s.
     *
     * @return the {@link EntityMetadataRegistry}
     */
    public static EntityMetadataRegistry entities() {
        return StandardMetadataRegistries.ENTITY;
    }

    /**
     * Produces a {@link MetadataMap} for the given object.
     * <p>
     * A map will only be returned if the object is an instance of
     * {@link Player}, {@link UUID} or {@link Entity}.
     *
     * @param o the object
     * @return a metadata map
     */
    public static MetadataMap provide(Object o) {
        Objects.requireNonNull(o, "o");
        if (o instanceof Player) {
            return provideForPlayer((Player) o);
        } else if (o instanceof UUID) {
            return provideForPlayer((UUID) o);
        } else if (o instanceof Entity) {
            return provideForEntity((Entity) o);
        } else {
            throw new IllegalArgumentException("Unknown object type: " + o.getClass());
        }
    }

    /**
     * Gets a {@link MetadataMap} for the given object, if one already exists and has
     * been cached in this registry.
     * <p>
     * A map will only be returned if the object is an instance of
     * {@link Player}, {@link UUID} or {@link Entity}.
     *
     * @param o the object
     * @return a metadata map
     */
    public static Optional<MetadataMap> get(Object o) {
        Objects.requireNonNull(o, "o");
        if (o instanceof Player) {
            return getForPlayer((Player) o);
        } else if (o instanceof UUID) {
            return getForPlayer((UUID) o);
        } else if (o instanceof Entity) {
            return getForEntity((Entity) o);
        } else {
            throw new IllegalArgumentException("Unknown object type: " + o.getClass());
        }
    }

    public static MetadataMap provideForPlayer(UUID uuid) {
        return players().provide(uuid);
    }

    public static MetadataMap provideForPlayer(Player player) {
        return players().provide(player);
    }

    public static Optional<MetadataMap> getForPlayer(UUID uuid) {
        return players().get(uuid);
    }

    public static Optional<MetadataMap> getForPlayer(Player player) {
        return players().get(player);
    }

    public static <T> Map<Player, T> lookupPlayersWithKey(MetadataKey<T> key) {
        return players().getAllWithKey(key);
    }
    public static MetadataMap provideForEntity(UUID uuid) {
        return entities().provide(uuid);
    }

    public static MetadataMap provideForEntity(Entity entity) {
        return entities().provide(entity);
    }

    public static Optional<MetadataMap> getForEntity(UUID uuid) {
        return entities().get(uuid);
    }

    public static Optional<MetadataMap> getForEntity(Entity entity) {
        return entities().get(entity);
    }

    public static <T> Map<Entity, T> lookupEntitiesWithKey(MetadataKey<T> key) {
        return entities().getAllWithKey(key);
    }
}