package me.kubbidev.multiversus.plugin.bootstrap;

import me.kubbidev.multiversus.plugin.classpath.ClassPathAppender;
import me.kubbidev.multiversus.plugin.logging.PluginLogger;
import me.kubbidev.multiversus.plugin.scheduler.SchedulerAdapter;
import net.multiversus.api.platform.Platform;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Bootstrap plugin interface
 *
 * <p>Instances of this interface are responsible for loading the
 * "Multiversus plugin" on their respective platforms.</p>
 */
public interface MultiBootstrap {

    /**
     * Gets the plugin logger
     *
     * @return the logger
     */
    PluginLogger getPluginLogger();

    /**
     * Gets an adapter for the platforms scheduler
     *
     * @return the scheduler
     */
    SchedulerAdapter getScheduler();

    /**
     * Gets a {@link ClassPathAppender} for this bootstrap plugin
     *
     * @return a class path appender
     */
    ClassPathAppender getClassPathAppender();

    /**
     * Returns a countdown latch which {@link CountDownLatch#countDown() counts down}
     * after the plugin has loaded.
     *
     * @return a loading latch
     */
    CountDownLatch getLoadLatch();

    /**
     * Returns a countdown latch which {@link CountDownLatch#countDown() counts down}
     * after the plugin has enabled.
     *
     * @return an enable latch
     */
    CountDownLatch getEnableLatch();

    /**
     * Gets a string of the plugin's version
     *
     * @return the version of the plugin
     */
    String getVersion();

    /**
     * Gets the time when the plugin first started in millis.
     *
     * @return the enable time
     */
    Instant getStartupTime();

    /**
     * Gets the platform type this instance of Multiversus is running on.
     *
     * @return the platform type
     */
    Platform.Type getType();

    /**
     * Gets the name or "brand" of the running platform
     *
     * @return the server brand
     */
    String getServerBrand();

    /**
     * Gets the version of the running platform
     *
     * @return the server version
     */
    String getServerVersion();

    /**
     * Gets the plugins main data storage directory
     *
     * <p>Bukkit: ./plugins/Multiversus</p>
     * <p>BungeeCord: ./plugins/Multiversus</p>
     * <p>Sponge: ./multiversus/</p>
     * <p>Velocity: ./plugins/multiversus</p>
     * <p>Fabric: ./mods/Multiversus</p>
     * <p>Forge: ./config/multiversus</p>
     *
     * @return the platforms data folder
     */
    Path getDataDirectory();

    /**
     * Gets the plugins configuration directory
     *
     * @return the config directory
     */
    default Path getConfigDirectory() {
        return getDataDirectory();
    }

    /**
     * Gets a bundled resource file from the jar
     *
     * @param path the path of the file
     * @return the file as an input stream
     */
    default InputStream getResourceStream(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    /**
     * Gets a player object linked to this User. The returned object must be the same type
     * as the instance used in the platforms ContextManager
     *
     * @param uniqueId the users unique id
     * @return a player object, or null, if one couldn't be found.
     */
    Optional<?> getPlayer(UUID uniqueId);

    /**
     * Lookup a uuid from a username, using the servers internal uuid cache.
     *
     * @param username the username to lookup
     * @return an optional uuid, if found
     */
    Optional<UUID> lookupUniqueId(String username);

    /**
     * Lookup a username from a uuid, using the servers internal uuid cache.
     *
     * @param uniqueId the uuid to lookup
     * @return an optional username, if found
     */
    Optional<String> lookupUsername(UUID uniqueId);

    /**
     * Gets the number of users online on the platform
     *
     * @return the number of users
     */
    int getPlayerCount();

    /**
     * Gets the usernames of the users online on the platform
     *
     * @return a {@link List} of usernames
     */
    Collection<String> getPlayerList();

    /**
     * Gets the UUIDs of the users online on the platform
     *
     * @return a {@link Set} of UUIDs
     */
    Collection<UUID> getOnlinePlayers();

    /**
     * Checks if a user is online
     *
     * @param uniqueId the users external uuid
     * @return true if the user is online
     */
    boolean isPlayerOnline(UUID uniqueId);

    /**
     * Attempts to identify the plugin behind the given classloader.
     *
     * <p>Used for giving more helpful log messages when things break.</p>
     *
     * @param classLoader the classloader to identify
     * @return the name of the classloader source
     * @throws Exception anything
     */
    default @Nullable String identifyClassLoader(ClassLoader classLoader) throws Exception {
        return null;
    }
}
