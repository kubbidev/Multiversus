package me.kubbidev.multiversus.backup;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.kubbidev.multiversus.locale.Message;
import me.kubbidev.multiversus.model.User;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.sender.Sender;
import me.kubbidev.multiversus.util.CompletableFutures;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles import operations
 */
public class Importer implements Runnable {

    private final MultiPlugin plugin;
    private final Set<Sender> notify;
    private final JsonObject data;

    public Importer(MultiPlugin plugin, Sender executor, JsonObject data) {
        this.plugin = plugin;

        if (executor.isConsole()) {
            this.notify = ImmutableSet.of(executor);
        } else {
            this.notify = ImmutableSet.of(executor, plugin.getConsoleSender());
        }
        this.data = data;
    }

    private static final class UserData {
        private final String username;

        UserData(String username) {
            this.username = username;
        }
    }

    private void processUser(UUID uuid, UserData userData) {
        User user = this.plugin.getStorage().loadUser(uuid, userData.username).join();
        this.plugin.getStorage().saveUser(user).join();
        this.plugin.getUserManager().getHouseKeeper().cleanup(user.getUniqueId());
    }

    private Set<Map.Entry<String, JsonElement>> getDataSection(String id) {
        if (this.data.has(id)) {
            return this.data.get(id).getAsJsonObject().entrySet();
        } else {
            return Collections.emptySet();
        }
    }

    private void parseExportData(Map<UUID, UserData> users) {
        for (Map.Entry<String, JsonElement> user : getDataSection("users")) {
            JsonObject jsonData = user.getValue().getAsJsonObject();

            UUID uuid = UUID.fromString(user.getKey());
            String username = null;

            if (jsonData.has("username")) {
                username = jsonData.get("username").getAsString();
            }
            users.put(uuid, new UserData(username));
        }
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        this.notify.forEach(Message.IMPORT_START::send);

        // start an update task in the background - we'll #join this later
        CompletableFuture<Void> updateTask = CompletableFuture.runAsync(() -> this.plugin.getSyncTaskBuffer().requestDirectly());

        this.notify.forEach(s -> Message.IMPORT_INFO.send(s, "Reading data..."));

        Map<UUID, UserData> users = new HashMap<>();
        parseExportData(users);

        this.notify.forEach(s -> Message.IMPORT_INFO.send(s, "Waiting for initial update task to complete..."));

        // join the update task future before scheduling command executions
        updateTask.join();

        this.notify.forEach(s -> Message.IMPORT_INFO.send(s, "Setting up data processor..."));

        // create a threadpool for the processing
        ExecutorService executor = Executors.newFixedThreadPool(16, new ThreadFactoryBuilder().setNameFormat("multiversus-importer-%d").build());

        // A set of futures, which are really just the processes we need to wait for.
        Set<CompletableFuture<Void>> futures = new HashSet<>();

        int total = 0;
        AtomicInteger processedCount = new AtomicInteger(0);

        for (Map.Entry<UUID, UserData> user : users.entrySet()) {
            futures.add(CompletableFuture.completedFuture(user).thenAcceptAsync(ent -> {
                processUser(ent.getKey(), ent.getValue());
                processedCount.incrementAndGet();
            }, executor));
            total++;
        }

        // all of the threads have been scheduled now and are running. we just need to wait for them all to complete
        CompletableFuture<Void> overallFuture = CompletableFutures.allOf(futures);

        this.notify.forEach(s -> Message.IMPORT_INFO.send(s, "All data entries have been processed and scheduled for import - now waiting for the execution to complete."));

        while (true) {
            try {
                overallFuture.get(2, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException e) {
                // abnormal error - just break
                e.printStackTrace();
                break;
            } catch (TimeoutException e) {
                // still executing - send a progress report and continue waiting
                sendProgress(processedCount.get(), total);
                continue;
            }

            // process is complete
            break;
        }

        executor.shutdown();

        long endTime = System.currentTimeMillis();
        double seconds = (endTime - startTime) / 1000.0;

        this.notify.forEach(s -> Message.IMPORT_END_COMPLETE.send(s, seconds));
    }

    private void sendProgress(int processedCount, int total) {
        int percent = processedCount * 100 / total;
        this.notify.forEach(s -> Message.IMPORT_PROGRESS.send(s, percent, processedCount, total));
    }
}