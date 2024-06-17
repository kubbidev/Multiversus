package me.kubbidev.multiversus.core.metadata.registry.type;

import me.kubbidev.multiversus.core.metadata.MetadataKey;
import me.kubbidev.multiversus.core.metadata.MetadataMap;
import me.kubbidev.multiversus.core.metadata.registry.MetadataRegistry;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A registry which provides and stores {@link MetadataMap}s for {@link Player}s.
 */
public interface PlayerMetadataRegistry extends MetadataRegistry<UUID> {

    /**
     * Produces a {@link MetadataMap} for the given player.
     *
     * @param player the player
     * @return a metadata map
     */
    MetadataMap provide(Player player);

    /**
     * Gets a {@link MetadataMap} for the given player, if one already exists and has
     * been cached in this registry.
     *
     * @param player the player
     * @return a metadata map, if present
     */
    Optional<MetadataMap> get(Player player);

    /**
     * Gets a map of the players with a given metadata key
     *
     * @param key the key
     * @param <K> the key type
     * @return an immutable map of players to key value
     */
    <K> Map<Player, K> getAllWithKey(MetadataKey<K> key);

}