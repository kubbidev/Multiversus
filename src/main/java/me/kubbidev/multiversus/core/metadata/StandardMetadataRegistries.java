package me.kubbidev.multiversus.core.metadata;

import com.google.common.collect.ImmutableMap;
import me.kubbidev.multiversus.core.metadata.registry.AbstractMetadataRegistry;
import me.kubbidev.multiversus.core.metadata.registry.MetadataRegistry;
import me.kubbidev.multiversus.core.metadata.registry.type.EntityMetadataRegistry;
import me.kubbidev.multiversus.core.metadata.registry.type.PlayerMetadataRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * The Metadata registries provided by helper.
 * <p>
 * These instances can be accessed through {@link Metadata}.
 */
final class StandardMetadataRegistries {
    private StandardMetadataRegistries() {
    }

    public static final PlayerMetadataRegistry PLAYER = new PlayerRegistry();
    public static final EntityMetadataRegistry ENTITY = new EntityRegistry();

    private static final MetadataRegistry<?>[] VALUES = new MetadataRegistry[]{PLAYER, ENTITY};

    public static MetadataRegistry<?>[] values() {
        return VALUES;
    }

    private static final class PlayerRegistry extends AbstractMetadataRegistry<UUID> implements PlayerMetadataRegistry {

        @Override
        public MetadataMap provide(Player player) {
            Objects.requireNonNull(player, "player");
            return provide(player.getUniqueId());
        }

        @Override
        public Optional<MetadataMap> get(Player player) {
            Objects.requireNonNull(player, "player");
            return get(player.getUniqueId());
        }

        @Override
        public <K> Map<Player, K> getAllWithKey(MetadataKey<K> key) {
            Objects.requireNonNull(key, "key");
            ImmutableMap.Builder<Player, K> ret = ImmutableMap.builder();
            this.cache.forEach((uuid, map) -> map.get(key).ifPresent(t -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    ret.put(player, t);
                }
            }));
            return ret.build();
        }
    }

    private static final class EntityRegistry extends AbstractMetadataRegistry<UUID> implements EntityMetadataRegistry {

        @Override
        public MetadataMap provide(Entity entity) {
            Objects.requireNonNull(entity, "entity");
            return provide(entity.getUniqueId());
        }

        @Override
        public Optional<MetadataMap> get(Entity entity) {
            Objects.requireNonNull(entity, "entity");
            return get(entity.getUniqueId());
        }

        @Override
        public <K> Map<Entity, K> getAllWithKey(MetadataKey<K> key) {
            Objects.requireNonNull(key, "key");
            ImmutableMap.Builder<Entity, K> ret = ImmutableMap.builder();
            this.cache.forEach((uuid, map) -> map.get(key).ifPresent(t -> {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity != null) {
                    ret.put(entity, t);
                }
            }));
            return ret.build();
        }
    }
}