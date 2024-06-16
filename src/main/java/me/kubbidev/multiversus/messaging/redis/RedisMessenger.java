package me.kubbidev.multiversus.messaging.redis;

import me.kubbidev.multiversus.plugin.MultiPlugin;
import net.multiversus.api.messenger.IncomingMessageConsumer;
import net.multiversus.api.messenger.Messenger;
import net.multiversus.api.messenger.message.OutgoingMessage;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An implementation of {@link Messenger} using Redis.
 */
public class RedisMessenger implements Messenger {
    private static final String CHANNEL = "multiversus:update";

    private final MultiPlugin plugin;
    private final IncomingMessageConsumer consumer;

    private /* final */ UnifiedJedis jedis;
    private /* final */ Subscription sub;
    private boolean closing = false;

    public RedisMessenger(MultiPlugin plugin, IncomingMessageConsumer consumer) {
        this.plugin = plugin;
        this.consumer = consumer;
    }

    public void init(List<String> addresses, String username, String password, boolean ssl) {
        Set<HostAndPort> hosts = addresses.stream().map(RedisMessenger::parseAddress).collect(Collectors.toSet());
        this.init(new JedisCluster(hosts, jedisConfig(username, password, ssl)));
    }

    public void init(String address, String username, String password, boolean ssl) {
        this.init(new JedisPooled(parseAddress(address), jedisConfig(username, password, ssl)));
    }

    private void init(UnifiedJedis jedis) {
        this.jedis = jedis;
        this.sub = new Subscription(this);
        this.plugin.getBootstrap().getScheduler().executeAsync(this.sub);
    }

    private static JedisClientConfig jedisConfig(String username, String password, boolean ssl) {
        return DefaultJedisClientConfig.builder()
                .user(username)
                .password(password)
                .ssl(ssl)
                .timeoutMillis(Protocol.DEFAULT_TIMEOUT)
                .build();
    }

    private static HostAndPort parseAddress(String address) {
        String[] addressSplit = address.split(":");
        String host = addressSplit[0];
        int port = addressSplit.length > 1 ? Integer.parseInt(addressSplit[1]) : Protocol.DEFAULT_PORT;
        return new HostAndPort(host, port);
    }

    @Override
    public void sendOutgoingMessage(@NotNull OutgoingMessage outgoingMessage) {
        this.jedis.publish(CHANNEL, outgoingMessage.asEncodedString());
    }

    @Override
    public void close() {
        this.closing = true;
        this.sub.unsubscribe();
        this.jedis.close();
    }

    private static class Subscription extends JedisPubSub implements Runnable {
        private final RedisMessenger messenger;

        private Subscription(RedisMessenger messenger) {
            this.messenger = messenger;
        }

        @Override
        public void run() {
            boolean first = true;
            while (!this.messenger.closing && !Thread.interrupted() && this.isRedisAlive()) {
                try {
                    if (first) {
                        first = false;
                    } else {
                        this.messenger.plugin.getLogger().info("Redis pubsub connection re-established");
                    }

                    this.messenger.jedis.subscribe(this, CHANNEL); // blocking call
                } catch (Exception e) {
                    if (this.messenger.closing) {
                        return;
                    }

                    this.messenger.plugin.getLogger().warn("Redis pubsub connection dropped, trying to re-open the connection", e);
                    try {
                        unsubscribe();
                    } catch (Exception ignored) {

                    }

                    // Sleep for 5 seconds to prevent massive spam in console
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        @Override
        public void onMessage(String channel, String msg) {
            if (!channel.equals(CHANNEL)) {
                return;
            }
            this.messenger.consumer.consumeIncomingMessageAsString(msg);
        }

        private boolean isRedisAlive() {
            UnifiedJedis jedis = this.messenger.jedis;

            if (jedis instanceof JedisPooled) {
                return !((JedisPooled) jedis).getPool().isClosed();
            } else if (jedis instanceof JedisCluster) {
                return !((JedisCluster) jedis).getClusterNodes().isEmpty();
            } else {
                throw new RuntimeException("Unknown jedis type: " + jedis.getClass().getName());
            }
        }
    }
}