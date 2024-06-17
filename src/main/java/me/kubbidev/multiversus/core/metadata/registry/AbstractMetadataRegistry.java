package me.kubbidev.multiversus.core.metadata.registry;

import me.kubbidev.multiversus.cache.LoadingMap;
import me.kubbidev.multiversus.core.metadata.MetadataMap;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A basic implementation of {@link MetadataRegistry} using a LoadingCache.
 *
 * @param <T> the type
 */
public class AbstractMetadataRegistry<T> implements MetadataRegistry<T> {
    private static final Function<?, MetadataMap> LOADER = new Loader<>();

    @SuppressWarnings("unchecked")
    private static <T> Function<T, MetadataMap> getLoader() {
        return (Function<T, MetadataMap>) LOADER;
    }

    protected final LoadingMap<T, MetadataMap> cache = LoadingMap.of(getLoader());

    @Override
    public MetadataMap provide(T id) {
        Objects.requireNonNull(id, "id");
        return this.cache.get(id);
    }

    @Override
    public Optional<MetadataMap> get(T id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(this.cache.getIfPresent(id));
    }

    @Override
    public void remove(T id) {
        MetadataMap map = this.cache.remove(id);
        if (map != null) {
            map.clear();
        }
    }

    @Override
    public void cleanup() {
        // MetadataMap#isEmpty also removes expired values
        this.cache.values().removeIf(MetadataMap::isEmpty);
    }

    private static final class Loader<T> implements Function<T, MetadataMap> {

        @Override
        public MetadataMap apply(T t) {
            return MetadataMap.create();
        }
    }
}