package me.kubbidev.multiversus.storage.implementation.file;

import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.storage.implementation.file.loader.ConfigurateLoader;
import me.kubbidev.multiversus.storage.implementation.file.watcher.FileWatcher;
import me.kubbidev.multiversus.util.Uuids;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Flat-file storage using Configurate {@link ConfigurationNode}s.
 * The data for users is stored in a single shared file.
 */
public class CombinedConfigurateStorage extends AbstractConfigurateStorage {
    private final String fileExtension;

    private CachedLoader users;
    private FileWatcher.WatchedLocation watcher = null;

    public CombinedConfigurateStorage(MultiPlugin plugin, String implementationName, ConfigurateLoader loader, String fileExtension, String dataFolderName) {
        super(plugin, implementationName, loader, dataFolderName);
        this.fileExtension = fileExtension;
    }

    @Override
    protected ConfigurationNode readFile(StorageLocation location, String name) throws IOException {
        ConfigurationNode root = getLoader(location).getNode();
        ConfigurationNode node = root.getNode(name);
        return node.isVirtual() ? null : node;
    }

    @Override
    protected void saveFile(StorageLocation location, String name, ConfigurationNode node) throws IOException {
        getLoader(location).apply(true, false, root -> root.getNode(name).setValue(node));
    }

    private CachedLoader getLoader(StorageLocation location) {
        if (Objects.requireNonNull(location) == StorageLocation.USERS) {
            return this.users;
        }
        throw new RuntimeException();
    }

    @Override
    public void init() throws IOException {
        super.init();

        this.users = new CachedLoader(super.dataDirectory.resolve("users" + this.fileExtension));

        // Listen for file changes.
        FileWatcher watcher = this.plugin.getFileWatcher().orElse(null);
        if (watcher != null) {
            this.watcher = watcher.getWatcher(super.dataDirectory);
            this.watcher.addListener(path -> {
                if (path.getFileName().equals(this.users.file.getFileName())) {
                    this.plugin.getLogger().info("[FileWatcher] Detected change in users file - reloading...");
                    this.users.reload();
                    this.plugin.getSyncTaskBuffer().request();
                }
            });
        }
    }

    @Override
    public void shutdown() {
        try {
            this.users.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.shutdown();
    }

    @Override
    public Set<UUID> getUniqueUsers() throws Exception {
        return this.users.getNode().getChildrenMap().keySet().stream()
                .map(Object::toString)
                .map(Uuids::fromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private final class CachedLoader {
        private final Path file;
        private final ConfigurationLoader<? extends ConfigurationNode> loader;
        private final ReentrantLock lock = new ReentrantLock();
        private ConfigurationNode node = null;

        private CachedLoader(Path file) {
            this.file = file;
            this.loader = CombinedConfigurateStorage.super.loader.loader(file);
            reload();
        }

        private void recordChange() {
            if (CombinedConfigurateStorage.this.watcher != null) {
                CombinedConfigurateStorage.this.watcher.recordChange(this.file.getFileName().toString());
            }
        }

        public ConfigurationNode getNode() throws IOException {
            this.lock.lock();
            try {
                if (this.node == null) {
                    this.node = this.loader.load();
                }

                return this.node;
            } finally {
                this.lock.unlock();
            }
        }

        public void apply(Consumer<ConfigurationNode> action) throws IOException {
            apply(false, false, action);
        }

        public void apply(boolean save, boolean reload, Consumer<ConfigurationNode> action) throws IOException {
            this.lock.lock();
            try {
                if (this.node == null || reload) {
                    reload();
                }

                action.accept(this.node);

                if (save) {
                    save();
                }
            } finally {
                this.lock.unlock();
            }
        }

        public void save() throws IOException {
            this.lock.lock();
            try {
                recordChange();
                this.loader.save(this.node);
            } finally {
                this.lock.unlock();
            }
        }

        public void reload() {
            this.lock.lock();
            try {
                this.node = null;
                try {
                    recordChange();
                    this.node = this.loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                this.lock.unlock();
            }
        }
    }
}