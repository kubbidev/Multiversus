package me.kubbidev.multiversus.messaging.postgres;

import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.plugin.scheduler.SchedulerTask;
import me.kubbidev.multiversus.storage.implementation.sql.SqlStorage;
import net.multiversus.api.messenger.IncomingMessageConsumer;
import net.multiversus.api.messenger.Messenger;
import net.multiversus.api.messenger.message.OutgoingMessage;
import org.jetbrains.annotations.NotNull;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.postgresql.util.PSQLException;

import java.net.SocketException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An implementation of {@link Messenger} using Postgres.
 */
public class PostgresMessenger implements Messenger {

    private static final String CHANNEL = "multiversus:update";

    private final MultiPlugin plugin;
    private final SqlStorage sqlStorage;
    private final IncomingMessageConsumer consumer;

    private NotificationListener listener;
    private SchedulerTask checkConnectionTask;

    public PostgresMessenger(MultiPlugin plugin, SqlStorage sqlStorage, IncomingMessageConsumer consumer) {
        this.plugin = plugin;
        this.sqlStorage = sqlStorage;
        this.consumer = consumer;
    }

    public void init() {
        checkAndReopenConnection(true);
        this.checkConnectionTask = this.plugin.getBootstrap().getScheduler().asyncRepeating(() -> checkAndReopenConnection(false), 5, TimeUnit.SECONDS);
    }

    @Override
    public void sendOutgoingMessage(@NotNull OutgoingMessage outgoingMessage) {
        try (Connection connection = this.sqlStorage.getConnectionFactory().getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("SELECT pg_notify(?, ?)")) {
                ps.setString(1, CHANNEL);
                ps.setString(2, outgoingMessage.asEncodedString());
                ps.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            this.checkConnectionTask.cancel();
            if (this.listener != null) {
                this.listener.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks the connection, and re-opens it if necessary.
     *
     * @return true if the connection is now alive, false otherwise
     */
    private boolean checkAndReopenConnection(boolean firstStartup) {
        boolean listenerActive = this.listener != null && this.listener.isListening();
        if (listenerActive) {
            return true;
        }

        // (re)create

        if (!firstStartup) {
            this.plugin.getLogger().warn("Postgres listen/notify connection dropped, trying to re-open the connection");
        }

        try {
            this.listener = new NotificationListener();
            this.plugin.getBootstrap().getScheduler().executeAsync(() -> {
                this.listener.listenAndBind();
                if (!firstStartup) {
                    this.plugin.getLogger().info("Postgres listen/notify connection re-established");
                }
            });
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private class NotificationListener implements AutoCloseable {
        private static final int RECEIVE_TIMEOUT_MILLIS = 1000;

        private final AtomicBoolean open = new AtomicBoolean(true);
        private final AtomicReference<Thread> listeningThread = new AtomicReference<>();

        public void listenAndBind() {
            try (Connection connection = PostgresMessenger.this.sqlStorage.getConnectionFactory().getConnection()) {
                try (Statement s = connection.createStatement()) {
                    s.execute("LISTEN \"" + CHANNEL + "\"");
                }

                PGConnection pgConnection = connection.unwrap(PGConnection.class);
                this.listeningThread.set(Thread.currentThread());

                while (this.open.get()) {
                    PGNotification[] notifications = pgConnection.getNotifications(RECEIVE_TIMEOUT_MILLIS);
                    if (notifications != null) {
                        for (PGNotification notification : notifications) {
                            handleNotification(notification);
                        }
                    }
                }

            } catch (PSQLException e) {
                if (!(e.getCause() instanceof SocketException && e.getCause().getMessage().equals("Socket closed"))) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                this.listeningThread.set(null);
            }
        }

        public boolean isListening() {
            return this.listeningThread.get() != null;
        }

        public void handleNotification(PGNotification notification) {
            if (!CHANNEL.equals(notification.getName())) {
                return;
            }
            PostgresMessenger.this.consumer.consumeIncomingMessageAsString(notification.getParameter());
        }

        @Override
        public void close() throws Exception {
            if (this.open.compareAndSet(true, false)) {
                Thread thread = this.listeningThread.get();
                if (thread != null) {
                    thread.interrupt();
                }
            }
        }
    }
}