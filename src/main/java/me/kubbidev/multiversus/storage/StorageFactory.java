package me.kubbidev.multiversus.storage;

import com.google.common.collect.ImmutableSet;
import me.kubbidev.multiversus.config.ConfigKeys;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.storage.implementation.StorageImplementation;
import me.kubbidev.multiversus.storage.implementation.custom.CustomStorageProviders;
import me.kubbidev.multiversus.storage.implementation.file.CombinedConfigurateStorage;
import me.kubbidev.multiversus.storage.implementation.file.SeparatedConfigurateStorage;
import me.kubbidev.multiversus.storage.implementation.file.loader.HoconLoader;
import me.kubbidev.multiversus.storage.implementation.file.loader.JsonLoader;
import me.kubbidev.multiversus.storage.implementation.file.loader.TomlLoader;
import me.kubbidev.multiversus.storage.implementation.file.loader.YamlLoader;
import me.kubbidev.multiversus.storage.implementation.mongodb.MongoStorage;
import me.kubbidev.multiversus.storage.implementation.split.SplitStorage;
import me.kubbidev.multiversus.storage.implementation.split.SplitStorageType;
import me.kubbidev.multiversus.storage.implementation.sql.SqlStorage;
import me.kubbidev.multiversus.storage.implementation.sql.connection.file.H2ConnectionFactory;
import me.kubbidev.multiversus.storage.implementation.sql.connection.file.SqliteConnectionFactory;
import me.kubbidev.multiversus.storage.implementation.sql.connection.hikari.MariaDbConnectionFactory;
import me.kubbidev.multiversus.storage.implementation.sql.connection.hikari.MySqlConnectionFactory;
import me.kubbidev.multiversus.storage.implementation.sql.connection.hikari.PostgresConnectionFactory;
import me.kubbidev.multiversus.util.ImmutableCollectors;

import java.util.Map;
import java.util.Set;

public class StorageFactory {
    private final MultiPlugin plugin;

    public StorageFactory(MultiPlugin plugin) {
        this.plugin = plugin;
    }

    public Set<StorageType> getRequiredTypes() {
        if (this.plugin.getConfiguration().get(ConfigKeys.SPLIT_STORAGE)) {
            return ImmutableSet.copyOf(this.plugin.getConfiguration().get(ConfigKeys.SPLIT_STORAGE_OPTIONS).values());
        } else {
            return ImmutableSet.of(this.plugin.getConfiguration().get(ConfigKeys.STORAGE_METHOD));
        }
    }

    public Storage getInstance() {
        Storage storage;
        if (this.plugin.getConfiguration().get(ConfigKeys.SPLIT_STORAGE)) {
            this.plugin.getLogger().info("Loading storage provider... [SPLIT STORAGE]");

            Map<SplitStorageType, StorageType> mappedTypes = this.plugin.getConfiguration().get(ConfigKeys.SPLIT_STORAGE_OPTIONS);
            Map<StorageType, StorageImplementation> backing = mappedTypes.values().stream()
                    .distinct()
                    .collect(ImmutableCollectors.toEnumMap(StorageType.class, e -> e, this::createNewImplementation));

            // make a base implementation
            storage = new Storage(this.plugin, new SplitStorage(this.plugin, backing, mappedTypes));

        } else {
            StorageType type = this.plugin.getConfiguration().get(ConfigKeys.STORAGE_METHOD);
            this.plugin.getLogger().info("Loading storage provider... [" + type.name() + "]");
            storage = new Storage(this.plugin, createNewImplementation(type));
        }

        storage.init();
        return storage;
    }

    private StorageImplementation createNewImplementation(StorageType method) {
        switch (method) {
            case CUSTOM:
                return CustomStorageProviders.getProvider().provide(this.plugin);
            case MARIADB:
                return new SqlStorage(
                        this.plugin,
                        new MariaDbConnectionFactory(this.plugin.getConfiguration().get(ConfigKeys.DATABASE_VALUES)),
                        this.plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
                );
            case MYSQL:
                return new SqlStorage(
                        this.plugin,
                        new MySqlConnectionFactory(this.plugin.getConfiguration().get(ConfigKeys.DATABASE_VALUES)),
                        this.plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
                );
            case SQLITE:
                return new SqlStorage(
                        this.plugin,
                        new SqliteConnectionFactory(this.plugin.getBootstrap().getDataDirectory().resolve("multiversus-sqlite.db")),
                        this.plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
                );
            case H2:
                return new SqlStorage(
                        this.plugin,
                        new H2ConnectionFactory(this.plugin.getBootstrap().getDataDirectory().resolve("multiversus-h2-v2")),
                        this.plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
                );
            case POSTGRESQL:
                return new SqlStorage(
                        this.plugin,
                        new PostgresConnectionFactory(this.plugin.getConfiguration().get(ConfigKeys.DATABASE_VALUES)),
                        this.plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
                );
            case MONGODB:
                return new MongoStorage(
                        this.plugin,
                        this.plugin.getConfiguration().get(ConfigKeys.DATABASE_VALUES),
                        this.plugin.getConfiguration().get(ConfigKeys.MONGODB_COLLECTION_PREFIX),
                        this.plugin.getConfiguration().get(ConfigKeys.MONGODB_CONNECTION_URI)
                );
            case YAML:
                return new SeparatedConfigurateStorage(this.plugin, "YAML", new YamlLoader(), ".yml", "yaml-storage");
            case JSON:
                return new SeparatedConfigurateStorage(this.plugin, "JSON", new JsonLoader(), ".json", "json-storage");
            case HOCON:
                return new SeparatedConfigurateStorage(this.plugin, "HOCON", new HoconLoader(), ".conf", "hocon-storage");
            case TOML:
                return new SeparatedConfigurateStorage(this.plugin, "TOML", new TomlLoader(), ".toml", "toml-storage");
            case YAML_COMBINED:
                return new CombinedConfigurateStorage(this.plugin, "YAML Combined", new YamlLoader(), ".yml", "yaml-storage");
            case JSON_COMBINED:
                return new CombinedConfigurateStorage(this.plugin, "JSON Combined", new JsonLoader(), ".json", "json-storage");
            case HOCON_COMBINED:
                return new CombinedConfigurateStorage(this.plugin, "HOCON Combined", new HoconLoader(), ".conf", "hocon-storage");
            case TOML_COMBINED:
                return new CombinedConfigurateStorage(this.plugin, "TOML Combined", new TomlLoader(), ".toml", "toml-storage");
            default:
                throw new RuntimeException("Unknown method: " + method);
        }
    }
}