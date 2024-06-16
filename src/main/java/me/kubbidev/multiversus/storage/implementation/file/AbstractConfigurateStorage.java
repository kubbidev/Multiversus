package me.kubbidev.multiversus.storage.implementation.file;

import me.kubbidev.multiversus.model.User;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.storage.StorageMetadata;
import me.kubbidev.multiversus.storage.implementation.StorageImplementation;
import me.kubbidev.multiversus.storage.implementation.file.loader.ConfigurateLoader;
import me.kubbidev.multiversus.util.MoreFiles;
import net.multiversus.api.model.PlayerSaveResult;
import ninja.leaping.configurate.ConfigurationNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Abstract storage implementation using Configurate {@link ConfigurationNode}s to
 * serialize and deserialize data.
 */
public abstract class AbstractConfigurateStorage implements StorageImplementation {
    /**
     * The plugin instance
     */
    protected final MultiPlugin plugin;

    /**
     * The name of this implementation
     */
    private final String implementationName;

    /**
     * The Configurate loader used to read/write data
     */
    protected final ConfigurateLoader loader;

    /* The data directory */
    protected Path dataDirectory;
    private final String dataDirectoryName;

    /* The UUID cache */
    private final FileUuidCache uuidCache;
    private Path uuidCacheFile;

    protected AbstractConfigurateStorage(MultiPlugin plugin, String implementationName, ConfigurateLoader loader, String dataDirectoryName) {
        this.plugin = plugin;
        this.implementationName = implementationName;
        this.loader = loader;
        this.dataDirectoryName = dataDirectoryName;

        this.uuidCache = new FileUuidCache();
    }

    @Override
    public MultiPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public String getImplementationName() {
        return this.implementationName;
    }

    @Override
    public StorageMetadata getMeta() {
        return new StorageMetadata();
    }

    /**
     * Reads a configuration node from the given location
     *
     * @param location the location
     * @param name the name of the object
     * @return the node
     * @throws IOException if an io error occurs
     */
    protected abstract ConfigurationNode readFile(StorageLocation location, String name) throws IOException;

    /**
     * Saves a configuration node to the given location
     *
     * @param location the location
     * @param name the name of the object
     * @param node the node
     * @throws IOException if an io error occurs
     */
    protected abstract void saveFile(StorageLocation location, String name, ConfigurationNode node) throws IOException;

    @Override
    public void init() throws IOException {
        // init the data directory and ensure it exists
        this.dataDirectory = this.plugin.getBootstrap().getDataDirectory().resolve(this.dataDirectoryName);
        MoreFiles.createDirectoriesIfNotExists(this.dataDirectory);

        // setup the uuid cache
        this.uuidCacheFile = MoreFiles.createFileIfNotExists(this.dataDirectory.resolve("uuidcache.txt"));
        this.uuidCache.load(this.uuidCacheFile);
    }

    @Override
    public void shutdown() {
        this.uuidCache.save(this.uuidCacheFile);
    }

    @Override
    public User loadUser(UUID uniqueId, String username) throws IOException {
        User user = this.plugin.getUserManager().getOrMake(uniqueId, username);
        try {
            ConfigurationNode file = readFile(StorageLocation.USERS, uniqueId.toString());
            if (file != null) {
                String name = file.getNode("name").getString();
                user.setUsername(name, true);

                boolean updatedUsername = user.getUsername().isPresent() && (name == null || !user.getUsername().get().equalsIgnoreCase(name));
                if (updatedUsername) {
                    saveUser(user);
                }
            }
        } catch (Exception e) {
            throw new FileIOException(uniqueId.toString(), e);
        }
        return user;
    }

    @Override
    public Map<UUID, User> loadUsers(Set<UUID> uniqueIds) throws Exception {
        // add multithreading here?
        Map<UUID, User> map = new HashMap<>();
        for (UUID uniqueId : uniqueIds) {
            map.put(uniqueId, loadUser(uniqueId, null));
        }
        return map;
    }

    @Override
    public void saveUser(User user) throws IOException {
        try {
            ConfigurationNode file = ConfigurationNode.root();
            if (this instanceof SeparatedConfigurateStorage) {
                file.getNode("uuid").setValue(user.getUniqueId().toString());
            }

            String name = user.getUsername().orElse("null");
            file.getNode("name").setValue(name);

            saveFile(StorageLocation.USERS, user.getUniqueId().toString(), file);

        } catch (Exception e) {
            throw new FileIOException(user.getUniqueId().toString(), e);
        }
    }

    @Override
    public PlayerSaveResult savePlayerData(UUID uniqueId, String username) {
        return this.uuidCache.addMapping(uniqueId, username);
    }

    @Override
    public void deletePlayerData(UUID uniqueId) {
        this.uuidCache.removeMapping(uniqueId);
    }

    @Override
    public UUID getPlayerUniqueId(String username) {
        return this.uuidCache.lookupUuid(username);
    }

    @Override
    public String getPlayerName(UUID uniqueId) {
        return this.uuidCache.lookupUsername(uniqueId);
    }
}