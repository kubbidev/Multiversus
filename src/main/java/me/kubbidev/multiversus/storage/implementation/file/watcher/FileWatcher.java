package me.kubbidev.multiversus.storage.implementation.file.watcher;

import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.util.ExpiringSet;
import me.kubbidev.multiversus.util.Iterators;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Simple implementation of {@link AbstractFileWatcher} for Multiversus data files.
 */
public class FileWatcher extends AbstractFileWatcher {

    /** The base watched path */
    private final Path basePath;

    /** A map of watched locations with corresponding listeners */
    private final Map<Path, WatchedLocation> watchedLocations;

    public FileWatcher(MultiPlugin plugin, Path basePath) throws IOException {
        super(basePath.getFileSystem(), true);
        this.watchedLocations = Collections.synchronizedMap(new HashMap<>());
        this.basePath = basePath;

        super.registerRecursively(basePath);
        plugin.getBootstrap().getScheduler().executeAsync(super::runEventProcessingLoop);
    }

    /**
     * Gets a {@link WatchedLocation} instance for a given path.
     *
     * @param path the path to get a watcher for
     * @return the watched location
     */
    public WatchedLocation getWatcher(Path path) {
        if (path.isAbsolute()) {
            path = this.basePath.relativize(path);
        }
        return this.watchedLocations.computeIfAbsent(path, WatchedLocation::new);
    }

    @Override
    protected void processEvent(WatchEvent<Path> event, Path path) {
        // get the relative path of the event
        Path relativePath = this.basePath.relativize(path);
        if (relativePath.getNameCount() == 0) {
            return;
        }

        // pass the event onto all watched locations that match
        for (Map.Entry<Path, WatchedLocation> entry : this.watchedLocations.entrySet()) {
            if (relativePath.startsWith(entry.getKey())) {
                entry.getValue().onEvent(event, relativePath);
            }
        }
    }

    /**
     * Encapsulates a "watcher" in a specific directory.
     */
    public static final class WatchedLocation {
        /** The directory being watched by this instance. */
        private final Path path;

        /** A set of files which have been modified recently */
        private final ExpiringSet<String> recentlyModifiedFiles = new ExpiringSet<>(4, TimeUnit.SECONDS);

        /** The listener callback functions */
        private final List<Consumer<Path>> callbacks = new CopyOnWriteArrayList<>();

        WatchedLocation(Path path) {
            this.path = path;
        }

        void onEvent(WatchEvent<Path> event, Path path) {
            // get the relative path of the modified file
            Path relativePath = this.path.relativize(path);

            // check if the file has been modified recently
            String fileName = relativePath.toString();
            if (!this.recentlyModifiedFiles.add(fileName)) {
                return;
            }

            // pass the event onto registered listeners
            Iterators.tryIterate(this.callbacks, cb -> cb.accept(relativePath));
        }

        /**
         * Record that a file has been changed recently.
         *
         * @param fileName the name of the file
         */
        public void recordChange(String fileName) {
            this.recentlyModifiedFiles.add(fileName);
        }

        /**
         * Register a listener.
         *
         * @param listener the listener
         */
        public void addListener(Consumer<Path> listener) {
            this.callbacks.add(listener);
        }
    }

}