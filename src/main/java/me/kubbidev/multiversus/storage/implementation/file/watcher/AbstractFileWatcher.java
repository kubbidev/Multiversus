package me.kubbidev.multiversus.storage.implementation.file.watcher;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility for "watching" for file changes using a {@link WatchService}.
 */
public abstract class AbstractFileWatcher implements AutoCloseable {

    /**
     * Get a {@link WatchKey} from the given {@link WatchService} in the given {@link Path directory}.
     *
     * @param watchService the watch service
     * @param directory the directory
     * @return the watch key
     * @throws IOException if unable to register
     */
    private static WatchKey register(WatchService watchService, Path directory) throws IOException {
        return directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
    }

    /** The watch service */
    private final WatchService service;

    /** A map of all registered watch keys */
    private final Map<WatchKey, Path> keys = Collections.synchronizedMap(new HashMap<>());

    /** If this file watcher should discover directories */
    private final boolean autoRegisterNewSubDirectories;

    /** The thread currently being used to wait for & process watch events */
    private final AtomicReference<Thread> processingThread = new AtomicReference<>();

    public AbstractFileWatcher(FileSystem fileSystem, boolean autoRegisterNewSubDirectories) throws IOException {
        this.service = fileSystem.newWatchService();
        this.autoRegisterNewSubDirectories = autoRegisterNewSubDirectories;
    }

    /**
     * Register a watch key in the given directory.
     *
     * @param directory the directory
     * @throws IOException if unable to register a key
     */
    public void register(Path directory) throws IOException {
        final WatchKey key = register(this.service, directory);
        this.keys.put(key, directory);
    }

    /**
     * Register a watch key recursively in the given directory.
     *
     * @param root the root directory
     * @throws IOException if unable to register a key
     */
    public void registerRecursively(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return super.preVisitDirectory(dir, attrs);
            }
        });
    }

    /**
     * Process an observed watch event.
     *
     * @param event the event
     * @param path the resolved event context
     */
    protected abstract void processEvent(WatchEvent<Path> event, Path path);

    /**
     * Processes {@link WatchEvent}s from the watch service until it is closed, or until
     * the thread is interrupted.
     */
    public final void runEventProcessingLoop() {
        if (!this.processingThread.compareAndSet(null, Thread.currentThread())) {
            throw new IllegalStateException("A thread is already processing events for this watcher.");
        }

        while (true) {
            // poll for a key from the watch service
            WatchKey key;
            try {
                key = this.service.take();
            } catch (InterruptedException | ClosedWatchServiceException e) {
                break;
            }

            // find the directory the key is watching
            Path directory = this.keys.get(key);
            if (directory == null) {
                key.cancel();
                continue;
            }

            // process each watch event the key has
            for (WatchEvent<?> ev : key.pollEvents()) {
                @SuppressWarnings("unchecked")
                WatchEvent<Path> event = (WatchEvent<Path>) ev;

                Path context = event.context();

                // ignore contexts with a name count of zero
                if (context == null || context.getNameCount() == 0) {
                    continue;
                }

                // resolve the context of the event against the directory being watched
                Path file = directory.resolve(context);

                // if the file is a regular file, send the event on to be processed
                if (Files.isRegularFile(file)) {
                    processEvent(event, file);
                }

                // handle recursive directory creation
                if (this.autoRegisterNewSubDirectories && event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)) {
                            registerRecursively(file);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // reset the key
            boolean valid = key.reset();
            if (!valid) {
                this.keys.remove(key);
            }
        }

        this.processingThread.compareAndSet(Thread.currentThread(), null);
    }

    @Override
    public void close() {
        try {
            this.service.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}