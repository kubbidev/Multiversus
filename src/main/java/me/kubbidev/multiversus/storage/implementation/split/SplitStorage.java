package me.kubbidev.multiversus.storage.implementation.split;

import com.google.common.collect.ImmutableMap;
import me.kubbidev.multiversus.model.User;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.storage.StorageMetadata;
import me.kubbidev.multiversus.storage.StorageType;
import me.kubbidev.multiversus.storage.implementation.StorageImplementation;
import net.multiversus.api.model.PlayerSaveResult;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SplitStorage implements StorageImplementation {
    private final MultiPlugin plugin;
    private final Map<StorageType, StorageImplementation> implementations;
    private final Map<SplitStorageType, StorageType> types;

    public SplitStorage(MultiPlugin plugin, Map<StorageType, StorageImplementation> implementations, Map<SplitStorageType, StorageType> types) {
        this.plugin = plugin;
        this.implementations = ImmutableMap.copyOf(implementations);
        this.types = ImmutableMap.copyOf(types);
    }

    public Map<StorageType, StorageImplementation> getImplementations() {
        return this.implementations;
    }

    private StorageImplementation implFor(SplitStorageType type) {
        return this.implementations.get(this.types.get(type));
    }

    @Override
    public MultiPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public String getImplementationName() {
        return "Split Storage";
    }

    @Override
    public void init() {
        boolean failed = false;
        for (StorageImplementation ds : this.implementations.values()) {
            try {
                ds.init();
            } catch (Exception ex) {
                failed = true;
                ex.printStackTrace();
            }
        }
        if (failed) {
            throw new RuntimeException("One of the backings failed to init");
        }
    }

    @Override
    public void shutdown() {
        for (StorageImplementation ds : this.implementations.values()) {
            try {
                ds.shutdown();
            } catch (Exception e) {
                this.plugin.getLogger().severe("Exception whilst disabling " + ds + " storage", e);
            }
        }
    }

    @Override
    public StorageMetadata getMeta() {
        StorageMetadata metadata = new StorageMetadata();
        for (StorageImplementation backing : this.implementations.values()) {
            metadata.combine(backing.getMeta());
        }
        return metadata;
    }

    @Override
    public User loadUser(UUID uniqueId, String username) throws Exception {
        return implFor(SplitStorageType.USER).loadUser(uniqueId, username);
    }

    @Override
    public Map<UUID, User> loadUsers(Set<UUID> uniqueIds) throws Exception {
        return implFor(SplitStorageType.USER).loadUsers(uniqueIds);
    }

    @Override
    public void saveUser(User user) throws Exception {
        implFor(SplitStorageType.USER).saveUser(user);
    }

    @Override
    public Set<UUID> getUniqueUsers() throws Exception {
        return implFor(SplitStorageType.USER).getUniqueUsers();
    }

    @Override
    public PlayerSaveResult savePlayerData(UUID uniqueId, String username) throws Exception {
        return implFor(SplitStorageType.UUID).savePlayerData(uniqueId, username);
    }

    @Override
    public void deletePlayerData(UUID uniqueId) throws Exception {
        implFor(SplitStorageType.UUID).deletePlayerData(uniqueId);
    }

    @Override
    public UUID getPlayerUniqueId(String username) throws Exception {
        return implFor(SplitStorageType.UUID).getPlayerUniqueId(username);
    }

    @Override
    public String getPlayerName(UUID uniqueId) throws Exception {
        return implFor(SplitStorageType.UUID).getPlayerName(uniqueId);
    }
}