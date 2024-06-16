package me.kubbidev.multiversus.messaging.nats;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.nats.client.*;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.util.Throwing;
import net.multiversus.api.messenger.IncomingMessageConsumer;
import net.multiversus.api.messenger.Messenger;
import net.multiversus.api.messenger.message.OutgoingMessage;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * An implementation of Messenger for Nats messaging client.
 */
public class NatsMessenger implements Messenger {

    private static final String CHANNEL = "multiversus:update";

    private final MultiPlugin plugin;
    private final IncomingMessageConsumer consumer;
    private Connection connection;
    private Dispatcher messageDispatcher;

    public NatsMessenger(MultiPlugin plugin, IncomingMessageConsumer consumer) {
        this.plugin = plugin;
        this.consumer = consumer;
    }

    @Override
    public void sendOutgoingMessage(@NotNull OutgoingMessage outgoingMessage) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF(outgoingMessage.asEncodedString());
        this.connection.publish(CHANNEL, output.toByteArray());
    }

    public void init(String address, String username, String password, boolean ssl) {
        String[] addressSplit = address.split(":");
        String host = addressSplit[0];
        int port = addressSplit.length > 1 ? Integer.parseInt(addressSplit[1]) : Options.DEFAULT_PORT;

        this.connection = createConnection(builder -> {
            builder.server("nats://" + host + ":" + port)
                    .reconnectWait(Duration.ofSeconds(5))
                    .maxReconnects(Integer.MAX_VALUE)
                    .connectionName("Multiversus");

            if (username != null && password != null) {
                builder.userInfo(username, password);
            }

            if (ssl) {
                builder.secure();
            }
        });
        this.messageDispatcher = this.connection.createDispatcher(new Handler()).subscribe(CHANNEL);
    }

    private Connection createConnection(Throwing.Consumer<Options.Builder> config) {
        try {
            Options.Builder builder = new Options.Builder();
            config.accept(builder);
            return Nats.connect(builder.build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            this.connection.closeDispatcher(this.messageDispatcher);
            this.connection.close();
        } catch (InterruptedException e) {
            this.plugin.getLogger().warn("An error occurred during closing messenger.", e);
        }
    }

    private class Handler implements MessageHandler {

        @Override
        public void onMessage(Message message) {
            byte[] data = message.getData();
            ByteArrayDataInput input = ByteStreams.newDataInput(data);
            String messageAsString = input.readUTF();

            NatsMessenger.this.consumer.consumeIncomingMessageAsString(messageAsString);
        }
    }
}