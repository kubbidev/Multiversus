package me.kubbidev.multiversus.storage.implementation.file;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import me.kubbidev.multiversus.model.User;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.storage.implementation.file.loader.ConfigurateLoader;
import me.kubbidev.multiversus.storage.implementation.file.watcher.FileWatcher;
import me.kubbidev.multiversus.util.CaffeineFactory;
import me.kubbidev.multiversus.util.MoreFiles;
import me.kubbidev.multiversus.util.Uuids;
import ninja.leaping.configurate.ConfigurationNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Flat-file storage using Configurate {@link ConfigurationNode}s.
 * The data for each user is stored in a separate file.
 */
public class SeparatedConfigurateStorage extends AbstractConfigurateStorage {
    private final String fileExtension;
    private final Predicate<Path> fileExtensionFilter;

    private final Map<StorageLocation, FileGroup> fileGroups;
    private final FileGroup users;

    private static final class FileGroup {
        private Path directory;
        private FileWatcher.WatchedLocation watcher;
    }

    private final LoadingCache<Path, ReentrantLock> ioLocks;

    public SeparatedConfigurateStorage(MultiPlugin plugin, String implementationName, ConfigurateLoader loader, String fileExtension, String dataFolderName) {
        super(plugin, implementationName, loader, dataFolderName);
        this.fileExtension = fileExtension;
        this.fileExtensionFilter = path -> path.getFileName().toString().endsWith(this.fileExtension);

        this.users = new FileGroup();

        EnumMap<StorageLocation, FileGroup> fileGroups = new EnumMap<>(StorageLocation.class);
        fileGroups.put(StorageLocation.USERS, this.users);
        this.fileGroups = ImmutableMap.copyOf(fileGroups);

        this.ioLocks = CaffeineFactory.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(key -> new ReentrantLock());
    }

    @Override
    protected ConfigurationNode readFile(StorageLocation location, String name) throws IOException {
        Path file = getDirectory(location).resolve(name + this.fileExtension);
        registerFileAction(location, file);
        return readFile(file);
    }

    private ConfigurationNode readFile(Path file) throws IOException {
        ReentrantLock lock = Objects.requireNonNull(this.ioLocks.get(file));
        lock.lock();
        try {
            if (!Files.exists(file)) {
                return null;
            }

            return this.loader.loader(file).load();
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void saveFile(StorageLocation location, String name, ConfigurationNode node) throws IOException {
        Path file = getDirectory(location).resolve(name + this.fileExtension);
        registerFileAction(location, file);
        saveFile(file, node);
    }

    private void saveFile(Path file, ConfigurationNode node) throws IOException {
        ReentrantLock lock = Objects.requireNonNull(this.ioLocks.get(file));
        lock.lock();
        try {
            if (node == null) {
                Files.deleteIfExists(file);
                return;
            }

            this.loader.loader(file).save(node);
        } finally {
            lock.unlock();
        }
    }

    private Path getDirectory(StorageLocation location) {
        return this.fileGroups.get(location).directory;
    }

    private void registerFileAction(StorageLocation type, Path file) {
        FileWatcher.WatchedLocation watcher = this.fileGroups.get(type).watcher;
        if (watcher != null) {
            watcher.recordChange(file.getFileName().toString());
        }
    }

    @Override
    public void init() throws IOException {
        super.init();

        this.users.directory = MoreFiles.createDirectoryIfNotExists(super.dataDirectory.resolve("users"));

        // Listen for file changes.
        FileWatcher watcher = this.plugin.getFileWatcher().orElse(null);
        if (watcher != null) {
            this.users.watcher = watcher.getWatcher(this.users.directory);
            this.users.watcher.addListener(path -> {
                String fileName = path.getFileName().toString();
                if (!fileName.endsWith(this.fileExtension)) {
                    return;
                }

                String user = fileName.substring(0, fileName.length() - this.fileExtension.length());
                UUID uuid = Uuids.parse(user);
                if (uuid == null) {
                    return;
                }

                User u = this.plugin.getUserManager().getIfLoaded(uuid);
                if (u != null) {
                    this.plugin.getLogger().info("[FileWatcher] Detected change in user file for " + u.getPlainDisplayName() + " - reloading...");
                    this.plugin.getStorage().loadUser(uuid, null);
                }
            });
        }
    }

    @Override
    public Set<UUID> getUniqueUsers() throws IOException {
        try (Stream<Path> stream = Files.list(this.users.directory)) {
            return stream.filter(this.fileExtensionFilter)
                    .map(p -> p.getFileName().toString())
                    .map(s -> s.substring(0, s.length() - this.fileExtension.length()))
                    .map(Uuids::fromString)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
    }
}