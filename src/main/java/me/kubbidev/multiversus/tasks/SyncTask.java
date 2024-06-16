package me.kubbidev.multiversus.tasks;

import me.kubbidev.multiversus.cache.BufferedRequest;
import me.kubbidev.multiversus.plugin.MultiPlugin;

import java.util.concurrent.TimeUnit;

/**
 * System wide sync task for Multiversus.
 *
 * <p>Ensures that all local data is consistent with the storage.</p>
 */
public class SyncTask implements Runnable {
    private final MultiPlugin plugin;

    public SyncTask(MultiPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Runs the update task
     *
     * <p>Called <b>async</b>.</p>
     */
    @Override
    public void run() {
        if (this.plugin.getEventDispatcher().dispatchPreSync(false)) {
            return;
        }

        // Reload all online users.
        this.plugin.getUserManager().loadAllUsers().join();

        this.plugin.performPlatformDataSync();
        this.plugin.getEventDispatcher().dispatchPostSync();
    }

    public static class Buffer extends BufferedRequest<Void> {
        private final MultiPlugin plugin;

        public Buffer(MultiPlugin plugin) {
            super(500L, TimeUnit.MILLISECONDS, plugin.getBootstrap().getScheduler());
            this.plugin = plugin;
        }

        @Override
        protected Void perform() {
            new SyncTask(this.plugin).run();
            return null;
        }
    }
}