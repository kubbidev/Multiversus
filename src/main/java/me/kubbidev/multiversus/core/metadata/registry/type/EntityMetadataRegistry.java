package me.kubbidev.multiversus.core.metadata.registry.type;

import me.kubbidev.multiversus.core.metadata.MetadataKey;
import me.kubbidev.multiversus.core.metadata.MetadataMap;
import me.kubbidev.multiversus.core.metadata.registry.MetadataRegistry;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A registry which provides and stores {@link MetadataMap}s for {@link Entity}s.
 */
public interface EntityMetadataRegistry extends MetadataRegistry<UUID> {

    /**
     * Produces a {@link MetadataMap} for the given entity.
     *
     * @param entity the entity
     * @return a metadata map
     */
    MetadataMap provide(Entity entity);

    /**
     * Gets a {@link MetadataMap} for the given entity, if one already exists and has
     * been cached in this registry.
     *
     * @param entity the entity
     * @return a metadata map, if present
     */
    Optional<MetadataMap> get(Entity entity);

    /**
     * Gets a map of the entities with a given metadata key
     *
     * @param key the key
     * @param <K> the key type
     * @return an immutable map of entities to key value
     */
    <K> Map<Entity, K> getAllWithKey(MetadataKey<K> key);

}