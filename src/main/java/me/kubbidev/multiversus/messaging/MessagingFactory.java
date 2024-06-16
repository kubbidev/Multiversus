package me.kubbidev.multiversus.messaging;

import me.kubbidev.multiversus.config.ConfigKeys;
import me.kubbidev.multiversus.config.MultiConfiguration;
import me.kubbidev.multiversus.messaging.nats.NatsMessenger;
import me.kubbidev.multiversus.messaging.postgres.PostgresMessenger;
import me.kubbidev.multiversus.messaging.rabbitmq.RabbitMQMessenger;
import me.kubbidev.multiversus.messaging.redis.RedisMessenger;
import me.kubbidev.multiversus.messaging.sql.SqlMessenger;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.storage.implementation.StorageImplementation;
import me.kubbidev.multiversus.storage.implementation.sql.SqlStorage;
import me.kubbidev.multiversus.storage.implementation.sql.connection.hikari.MariaDbConnectionFactory;
import me.kubbidev.multiversus.storage.implementation.sql.connection.hikari.MySqlConnectionFactory;
import me.kubbidev.multiversus.storage.implementation.sql.connection.hikari.PostgresConnectionFactory;
import net.multiversus.api.messenger.IncomingMessageConsumer;
import net.multiversus.api.messenger.Messenger;
import net.multiversus.api.messenger.MessengerProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MessagingFactory<P extends MultiPlugin> {
    private final P plugin;

    public MessagingFactory(P plugin) {
        this.plugin = plugin;
    }

    protected P getPlugin() {
        return this.plugin;
    }

    public final InternalMessagingService getInstance() {
        String messagingType = this.plugin.getConfiguration().get(ConfigKeys.MESSAGING_SERVICE);
        if (messagingType.equals("none")) {
            messagingType = "auto";
        }

        // attempt to detect "auto" messaging service type.
        if (messagingType.equals("auto")) {
            if (this.plugin.getConfiguration().get(ConfigKeys.REDIS_ENABLED)) {
                messagingType = "redis";
            } else if (this.plugin.getConfiguration().get(ConfigKeys.RABBITMQ_ENABLED)) {
                messagingType = "rabbitmq";
            } else if (this.plugin.getConfiguration().get(ConfigKeys.NATS_ENABLED)) {
                messagingType = "nats";
            } else {
                for (StorageImplementation implementation : this.plugin.getStorage().getImplementations()) {
                    if (implementation instanceof SqlStorage) {
                        SqlStorage sql = (SqlStorage) implementation;
                        if (sql.getConnectionFactory() instanceof MySqlConnectionFactory || sql.getConnectionFactory() instanceof MariaDbConnectionFactory) {
                            messagingType = "sql";
                            break;
                        }
                        if (sql.getConnectionFactory() instanceof PostgresConnectionFactory) {
                            messagingType = "postgresql";
                            break;
                        }
                    }
                }
            }
        }

        if (messagingType.equals("auto") || messagingType.equals("notsql")) {
            return null;
        }

        if (messagingType.equals("custom")) {
            this.plugin.getLogger().info("Messaging service is set to custom. No service is initialized at this stage yet.");
            return null;
        }

        this.plugin.getLogger().info("Loading messaging service... [" + messagingType.toUpperCase(Locale.ROOT) + "]");
        InternalMessagingService service = getServiceFor(messagingType);
        if (service != null) {
            return service;
        }
        this.plugin.getLogger().warn("Messaging service '" + messagingType + "' not recognised.");
        return null;
    }

    protected InternalMessagingService getServiceFor(String messagingType) {
        switch (messagingType) {
            case "redis":
                if (this.plugin.getConfiguration().get(ConfigKeys.REDIS_ENABLED)) {
                    try {
                        return new MultiMessagingService(this.plugin, new RedisMessengerProvider());
                    } catch (Exception e) {
                        getPlugin().getLogger().severe("Exception occurred whilst enabling Redis messaging service", e);
                    }
                } else {
                    this.plugin.getLogger().warn("Messaging Service was set to redis, but redis is not enabled!");
                }
                break;
            case "nats":
                if (this.plugin.getConfiguration().get(ConfigKeys.NATS_ENABLED)) {
                    try {
                        return new MultiMessagingService(this.plugin, new NatsMesengerProvider());
                    } catch (Exception e) {
                        getPlugin().getLogger().severe("Exception occurred whilst enabling Nats messaging service", e);
                    }
                } else {
                    this.plugin.getLogger().warn("Messaging Service was set to nats, but nats is not enabled!");
                }
                break;
            case "rabbitmq":
                if (this.plugin.getConfiguration().get(ConfigKeys.RABBITMQ_ENABLED)) {
                    try {
                        return new MultiMessagingService(this.plugin, new RabbitMQMessengerProvider());
                    } catch (Exception e) {
                        getPlugin().getLogger().severe("Exception occurred whilst enabling RabbitMQ messaging service", e);
                    }
                } else {
                    this.plugin.getLogger().warn("Messaging Service was set to rabbitmq, but rabbitmq is not enabled!");
                }
                break;
            case "sql":
                try {
                    return new MultiMessagingService(this.plugin, new SqlMessengerProvider());
                } catch (Exception e) {
                    getPlugin().getLogger().severe("Exception occurred whilst enabling SQL messaging service", e);
                }
                break;
            case "postgresql":
                try {
                    return new MultiMessagingService(this.plugin, new PostgresMessengerProvider());
                } catch (Exception e) {
                    getPlugin().getLogger().severe("Exception occurred whilst enabling Postgres messaging service", e);
                }
                break;
        }

        return null;
    }

    private class NatsMesengerProvider implements MessengerProvider {

        @Override
        public @NotNull String getName() {
            return "Nats";
        }

        @Override
        public @NotNull Messenger obtain(@NotNull IncomingMessageConsumer incomingMessageConsumer) {
            NatsMessenger natsMessenger = new NatsMessenger(getPlugin(), incomingMessageConsumer);

            MultiConfiguration configuration = getPlugin().getConfiguration();
            String address = configuration.get(ConfigKeys.NATS_ADDRESS);
            String username = configuration.get(ConfigKeys.NATS_USERNAME);
            String password = configuration.get(ConfigKeys.NATS_PASSWORD);
            if (password.isEmpty()) {
                password = null;
            }
            if (username.isEmpty()) {
                username = null;
            }
            boolean ssl = configuration.get(ConfigKeys.NATS_SSL);

            natsMessenger.init(address, username, password, ssl);
            return natsMessenger;
        }
    }

    private class RedisMessengerProvider implements MessengerProvider {

        @Override
        public @NotNull String getName() {
            return "Redis";
        }

        @Override
        public @NotNull Messenger obtain(@NotNull IncomingMessageConsumer incomingMessageConsumer) {
            RedisMessenger redis = new RedisMessenger(getPlugin(), incomingMessageConsumer);

            MultiConfiguration config = getPlugin().getConfiguration();
            String address = config.get(ConfigKeys.REDIS_ADDRESS);
            List<String> addresses = config.get(ConfigKeys.REDIS_ADDRESSES);
            String username = config.get(ConfigKeys.REDIS_USERNAME);
            String password = config.get(ConfigKeys.REDIS_PASSWORD);
            if (password.isEmpty()) {
                password = null;
            }
            if (username.isEmpty()) {
                username = null;
            }
            boolean ssl = config.get(ConfigKeys.REDIS_SSL);

            if (!addresses.isEmpty()) {
                // redis cluster
                addresses = new ArrayList<>(addresses);
                if (address != null) {
                    addresses.add(address);
                }
                redis.init(addresses, username, password, ssl);
            } else {
                // redis pool
                redis.init(address, username, password, ssl);
            }

            return redis;
        }
    }

    private class RabbitMQMessengerProvider implements MessengerProvider {

        @Override
        public @NotNull String getName() {
            return "RabbitMQ";
        }

        @Override
        public @NotNull Messenger obtain(@NotNull IncomingMessageConsumer incomingMessageConsumer) {
            RabbitMQMessenger rabbitmq = new RabbitMQMessenger(getPlugin(), incomingMessageConsumer);

            MultiConfiguration config = getPlugin().getConfiguration();
            String address = config.get(ConfigKeys.RABBITMQ_ADDRESS);
            String virtualHost = config.get(ConfigKeys.RABBITMQ_VIRTUAL_HOST);
            String username = config.get(ConfigKeys.RABBITMQ_USERNAME);
            String password = config.get(ConfigKeys.RABBITMQ_PASSWORD);

            rabbitmq.init(address, virtualHost, username, password);
            return rabbitmq;
        }
    }

    private class SqlMessengerProvider implements MessengerProvider {

        @Override
        public @NotNull String getName() {
            return "Sql";
        }

        @Override
        public @NotNull Messenger obtain(@NotNull IncomingMessageConsumer incomingMessageConsumer) {
            for (StorageImplementation implementation : getPlugin().getStorage().getImplementations()) {
                if (implementation instanceof SqlStorage) {
                    SqlStorage storage = (SqlStorage) implementation;
                    if (storage.getConnectionFactory() instanceof MySqlConnectionFactory || storage.getConnectionFactory() instanceof MariaDbConnectionFactory) {
                        // found an implementation match!
                        SqlMessenger sql = new SqlMessenger(getPlugin(), storage, incomingMessageConsumer);
                        sql.init();
                        return sql;
                    }
                }
            }

            throw new IllegalStateException("Can't find a supported sql storage implementation");
        }
    }

    private class PostgresMessengerProvider implements MessengerProvider {

        @Override
        public @NotNull String getName() {
            return "PostgreSQL";
        }

        @Override
        public @NotNull Messenger obtain(@NotNull IncomingMessageConsumer incomingMessageConsumer) {
            for (StorageImplementation implementation : getPlugin().getStorage().getImplementations()) {
                if (implementation instanceof SqlStorage) {
                    SqlStorage storage = (SqlStorage) implementation;
                    if (storage.getConnectionFactory() instanceof PostgresConnectionFactory) {
                        // found an implementation match!
                        PostgresMessenger messenger = new PostgresMessenger(getPlugin(), storage, incomingMessageConsumer);
                        messenger.init();
                        return messenger;
                    }
                }
            }

            throw new IllegalStateException("Can't find a supported sql storage implementation");
        }
    }
}