package me.kubbidev.multiversus.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import me.kubbidev.multiversus.config.generic.KeyedConfiguration;
import me.kubbidev.multiversus.config.generic.key.ConfigKey;
import me.kubbidev.multiversus.config.generic.key.SimpleConfigKey;
import me.kubbidev.multiversus.storage.StorageType;
import me.kubbidev.multiversus.storage.implementation.split.SplitStorageType;
import me.kubbidev.multiversus.storage.misc.StorageCredentials;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static me.kubbidev.multiversus.config.generic.key.ConfigKeyFactory.*;

/**
 * All of the {@link ConfigKey}s used by Multiversus.
 *
 * <p>The {@link #getKeys()} method and associated behaviour allows this class
 * to function a bit like an enum, but with generics.</p>
 */
@SuppressWarnings("CodeBlock2Expr")
public final class ConfigKeys {
    private ConfigKeys() {
    }

    /**
     * How many minutes to wait between syncs. A value <= 0 will disable syncing.
     */
    public static final ConfigKey<Integer> SYNC_TIME = notReloadable(key(c -> {
        int val = c.getInteger("sync-minutes", -1);
        if (val == -1) {
            val = c.getInteger("data.sync-minutes", -1);
        }
        return val;
    }));

    /**
     * The database settings, username, password, etc for use by any database
     */
    public static final ConfigKey<StorageCredentials> DATABASE_VALUES = notReloadable(key(c -> {
        int maxPoolSize = c.getInteger("data.pool-settings.maximum-pool-size", c.getInteger("data.pool-size", 10));
        int minIdle = c.getInteger("data.pool-settings.minimum-idle", maxPoolSize);
        int maxLifetime = c.getInteger("data.pool-settings.maximum-lifetime", 1800000);
        int keepAliveTime = c.getInteger("data.pool-settings.keepalive-time", 0);
        int connectionTimeout = c.getInteger("data.pool-settings.connection-timeout", 5000);
        Map<String, String> props = ImmutableMap.copyOf(c.getStringMap("data.pool-settings.properties", ImmutableMap.of()));

        return new StorageCredentials(
                c.getString("data.address", null),
                c.getString("data.database", null),
                c.getString("data.username", null),
                c.getString("data.password", null),
                maxPoolSize, minIdle, maxLifetime, keepAliveTime, connectionTimeout, props
        );
    }));

    /**
     * The prefix for any SQL tables
     */
    public static final ConfigKey<String> SQL_TABLE_PREFIX = notReloadable(key(c -> {
        return c.getString("data.table-prefix", c.getString("data.table_prefix", "multiversus_"));
    }));

    /**
     * The prefix for any MongoDB collections
     */
    public static final ConfigKey<String> MONGODB_COLLECTION_PREFIX = notReloadable(key(c -> {
        return c.getString("data.mongodb-collection-prefix", c.getString("data.mongodb_collection_prefix", ""));
    }));

    /**
     * MongoDB ClientConnectionURI to override default connection options
     */
    public static final ConfigKey<String> MONGODB_CONNECTION_URI = notReloadable(key(c -> {
        return c.getString("data.mongodb-connection-uri", c.getString("data.mongodb_connection_URI", ""));
    }));

    /**
     * The name of the storage method being used
     */
    public static final ConfigKey<StorageType> STORAGE_METHOD = notReloadable(key(c -> {
        return StorageType.parse(c.getString("storage-method", "h2"), StorageType.H2);
    }));

    /**
     * If storage files should be monitored for changes
     */
    public static final ConfigKey<Boolean> WATCH_FILES = booleanKey("watch-files", true);

    /**
     * If split storage is being used
     */
    public static final ConfigKey<Boolean> SPLIT_STORAGE = notReloadable(booleanKey("split-storage.enabled", false));

    /**
     * The options for split storage
     */
    public static final ConfigKey<Map<SplitStorageType, StorageType>> SPLIT_STORAGE_OPTIONS = notReloadable(key(c -> {
        EnumMap<SplitStorageType, StorageType> map = new EnumMap<>(SplitStorageType.class);
        map.put(SplitStorageType.USER, StorageType.parse(c.getString("split-storage.methods.user", "h2"), StorageType.H2));
        map.put(SplitStorageType.UUID, StorageType.parse(c.getString("split-storage.methods.uuid", "h2"), StorageType.H2));
        return ImmutableMap.copyOf(map);
    }));

    /**
     * The name of the messaging service in use, or "none" if not enabled
     */
    public static final ConfigKey<String> MESSAGING_SERVICE = notReloadable(lowercaseStringKey("messaging-service", "auto"));

    /**
     * If redis messaging is enabled
     */
    public static final ConfigKey<Boolean> REDIS_ENABLED = notReloadable(booleanKey("redis.enabled", false));

    /**
     * The address of the redis server
     */
    public static final ConfigKey<String> REDIS_ADDRESS = notReloadable(stringKey("redis.address", null));

    /**
     * The addresses of the redis servers (only for redis clusters)
     */
    public static final ConfigKey<List<String>> REDIS_ADDRESSES = notReloadable(stringListKey("redis.addresses", ImmutableList.of()));

    /**
     * The username to connect with, or an empty string if it should use default
     */
    public static final ConfigKey<String> REDIS_USERNAME = notReloadable(stringKey("redis.username", ""));

    /**
     * The password in use by the redis server, or an empty string if there is no password
     */
    public static final ConfigKey<String> REDIS_PASSWORD = notReloadable(stringKey("redis.password", ""));

    /**
     * If the redis connection should use SSL
     */
    public static final ConfigKey<Boolean> REDIS_SSL = notReloadable(booleanKey("redis.ssl", false));

    /**
     * If nats messaging is enabled
     */
    public static final ConfigKey<Boolean> NATS_ENABLED = notReloadable(booleanKey("nats.enabled", false));

    /**
     * The address of the nats server
     */
    public static final ConfigKey<String> NATS_ADDRESS = notReloadable(stringKey("nats.address", null));

    /**
     * The username to connect with, or an empty string if it should use default
     */
    public static final ConfigKey<String> NATS_USERNAME = notReloadable(stringKey("nats.username", ""));

    /**
     * The password in use by the nats server, or an empty string if there is no password
     */
    public static final ConfigKey<String> NATS_PASSWORD = notReloadable(stringKey("nats.password", ""));

    /**
     * If the nats connection should use SSL
     */
    public static final ConfigKey<Boolean> NATS_SSL = notReloadable(booleanKey("nats.ssl", false));

    /**
     * If rabbitmq messaging is enabled
     */
    public static final ConfigKey<Boolean> RABBITMQ_ENABLED = notReloadable(booleanKey("rabbitmq.enabled", false));

    /**
     * The address of the rabbitmq server
     */
    public static final ConfigKey<String> RABBITMQ_ADDRESS = notReloadable(stringKey("rabbitmq.address", null));

    /**
     * The virtual host to be used by the rabbitmq server
     */
    public static final ConfigKey<String> RABBITMQ_VIRTUAL_HOST = notReloadable(stringKey("rabbitmq.vhost", "/"));

    /**
     * The username in use by the rabbitmq server
     */
    public static final ConfigKey<String> RABBITMQ_USERNAME = notReloadable(stringKey("rabbitmq.username", "guest"));

    /**
     * The password in use by the rabbitmq server, or an empty string if there is no password
     */
    public static final ConfigKey<String> RABBITMQ_PASSWORD = notReloadable(stringKey("rabbitmq.password", "guest"));

    /**
     * If Multiversus should rate-limit command executions.
     */
    public static final ConfigKey<Boolean> COMMANDS_RATE_LIMIT = booleanKey("commands-rate-limit", true);

    /**
     * If Multiversus should produce extra logging output when it handles logins.
     */
    public static final ConfigKey<Boolean> DEBUG_LOGINS = booleanKey("debug-logins", false);

    /**
     * If Multiversus should attempt to register "Brigadier" command list data for its commands.
     */
    public static final ConfigKey<Boolean> REGISTER_COMMAND_LIST_DATA = notReloadable(booleanKey("register-command-list-data", true));

    /**
     * If Multiversus should attempt to resolve Vanilla command target selectors for Multiversus commands.
     */
    public static final ConfigKey<Boolean> RESOLVE_COMMAND_SELECTORS = booleanKey("resolve-command-selectors", false);

    /**
     * If the plugin should check for "extra" permissions with users run Multiversus commands
     */
    public static final ConfigKey<Boolean> USE_ARGUMENT_BASED_COMMAND_PERMISSIONS = booleanKey("argument-based-command-permissions", false);

    /**
     * # If the servers own UUID cache/lookup facility should be used when there is no record for a player in the Multiversus cache.
     */
    public static final ConfigKey<Boolean> USE_SERVER_UUID_CACHE = booleanKey("use-server-uuid-cache", false);

    /**
     * If Multiversus should allow usernames with non alphanumeric characters.
     */
    public static final ConfigKey<Boolean> ALLOW_INVALID_USERNAMES = booleanKey("allow-invalid-usernames", false);

    /**
     * A list of the keys defined in this class.
     */
    private static final List<SimpleConfigKey<?>> KEYS = KeyedConfiguration.initialise(ConfigKeys.class);

    public static List<? extends ConfigKey<?>> getKeys() {
        return KEYS;
    }

    /**
     * Check if the value at the given path should be censored in console/log output
     *
     * @param path the path
     * @return true if the value should be censored
     */
    public static boolean shouldCensorValue(final String path) {
        final String lower = path.toLowerCase(Locale.ROOT);
        return lower.contains("password") || lower.contains("uri");
    }
}