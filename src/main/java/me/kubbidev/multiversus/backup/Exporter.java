package me.kubbidev.multiversus.backup;

import com.google.gson.JsonObject;
import me.kubbidev.multiversus.locale.Message;
import me.kubbidev.multiversus.model.User;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.sender.Sender;
import me.kubbidev.multiversus.storage.Storage;
import me.kubbidev.multiversus.util.CompletableFutures;
import me.kubbidev.multiversus.util.gson.GsonProvider;
import me.kubbidev.multiversus.util.gson.JObject;
import net.kyori.adventure.text.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

/**
 * Handles export operations
 */
public abstract class Exporter implements Runnable {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    protected final MultiPlugin plugin;
    private final Sender executor;
    private final boolean includeUsers;
    protected final ProgressLogger log;

    protected Exporter(MultiPlugin plugin, Sender executor, boolean includeUsers) {
        this.plugin = plugin;
        this.executor = executor;
        this.includeUsers = includeUsers;

        this.log = new ProgressLogger();
        this.log.addListener(plugin.getConsoleSender());
        this.log.addListener(executor);
    }

    @Override
    public void run() {
        JsonObject json = new JsonObject();
        json.add("metadata", new JObject()
                .add("generatedBy", this.executor.getName())
                .add("generatedAt", DATE_FORMAT.format(new Date(System.currentTimeMillis())))
                .toJson());

        if (this.includeUsers) {
            this.log.log("Gathering user data...");
            json.add("users", exportUsers());
        }

        processOutput(json);
    }

    protected abstract void processOutput(JsonObject json);

    private JsonObject exportUsers() {
        // Users are migrated in separate threads.
        // This is because there are likely to be a lot of them, and because we can.
        // It's a big speed improvement, since the database/files are split up and can handle concurrent reads.

        this.log.log("Finding a list of unique users to export.");

        // Find all of the unique users we need to export
        Storage ds = this.plugin.getStorage();
        Set<UUID> users = ds.getUniqueUsers().join();
        this.log.log("Found " + users.size() + " unique users to export.");

        // create a threadpool to process the users concurrently
        ExecutorService executor = Executors.newFixedThreadPool(32);

        // A set of futures, which are really just the processes we need to wait for.
        Set<CompletableFuture<Void>> futures = new HashSet<>();

        AtomicInteger userCount = new AtomicInteger(0);
        Map<UUID, JsonObject> out = Collections.synchronizedMap(new TreeMap<>());

        // iterate through each user.
        for (UUID uuid : users) {
            // register a task for the user, and schedule it's execution with the pool
            futures.add(CompletableFuture.runAsync(() -> {
                User user = this.plugin.getStorage().loadUser(uuid, null).join();
                out.put(user.getUniqueId(), new JObject()
                        .consume(obj -> user.getUsername().ifPresent(username -> obj.add("username", username)))
                        .toJson());
                this.plugin.getUserManager().getHouseKeeper().cleanup(user.getUniqueId());
                userCount.incrementAndGet();
            }, executor));
        }

        // all of the threads have been scheduled now and are running. we just need to wait for them all to complete
        CompletableFuture<Void> overallFuture = CompletableFutures.allOf(futures);

        while (true) {
            try {
                overallFuture.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException e) {
                // abnormal error - just break
                e.printStackTrace();
                break;
            } catch (TimeoutException e) {
                // still executing - send a progress report and continue waiting
                this.log.logProgress("Exported " + userCount.get() + " users so far.");
                continue;
            }

            // process is complete
            break;
        }

        executor.shutdown();

        JsonObject outJson = new JsonObject();
        for (Map.Entry<UUID, JsonObject> entry : out.entrySet()) {
            outJson.add(entry.getKey().toString(), entry.getValue());
        }
        return outJson;
    }

    public static final class SaveFile extends Exporter {
        private final Path filePath;

        public SaveFile(MultiPlugin plugin, Sender executor, Path filePath, boolean includeUsers) {
            super(plugin, executor, includeUsers);
            this.filePath = filePath;
        }

        @Override
        protected void processOutput(JsonObject json) {
            this.log.log("Finished gathering data, writing file...");

            try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(this.filePath)), StandardCharsets.UTF_8))) {
                GsonProvider.normal().toJson(json, out);
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.log.getListeners().forEach(l -> Message.EXPORT_FILE_SUCCESS.send(l, this.filePath.toFile().getAbsolutePath()));
        }
    }

    protected static final class ProgressLogger {
        private final Set<Sender> listeners = new HashSet<>();

        public void addListener(Sender sender) {
            this.listeners.add(sender);
        }

        public Set<Sender> getListeners() {
            return this.listeners;
        }

        public void log(String msg) {
            dispatchMessage(Message.EXPORT_LOG, msg);
        }

        public void logProgress(String msg) {
            dispatchMessage(Message.EXPORT_LOG_PROGRESS, msg);
        }

        private void dispatchMessage(Message.Args1<String> messageType, String content) {
            final Component message = messageType.build(content);
            for (Sender s : this.listeners) {
                s.sendMessage(message);
            }
        }
    }
}