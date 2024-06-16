package me.kubbidev.multiversus.plugin;

import me.kubbidev.multiversus.api.MultiversusApiProvider;
import me.kubbidev.multiversus.command.CommandManager;
import me.kubbidev.multiversus.command.abstraction.Command;
import me.kubbidev.multiversus.config.MultiConfiguration;
import me.kubbidev.multiversus.dependencies.DependencyManager;
import me.kubbidev.multiversus.event.EventDispatcher;
import me.kubbidev.multiversus.extension.SimpleExtensionManager;
import me.kubbidev.multiversus.locale.TranslationManager;
import me.kubbidev.multiversus.messaging.InternalMessagingService;
import me.kubbidev.multiversus.model.User;
import me.kubbidev.multiversus.model.manager.user.UserManager;
import me.kubbidev.multiversus.plugin.bootstrap.MultiBootstrap;
import me.kubbidev.multiversus.plugin.logging.PluginLogger;
import me.kubbidev.multiversus.plugin.util.AbstractConnectionListener;
import me.kubbidev.multiversus.sender.Sender;
import me.kubbidev.multiversus.storage.Storage;
import me.kubbidev.multiversus.storage.implementation.file.watcher.FileWatcher;
import me.kubbidev.multiversus.tasks.SyncTask;
import net.multiversus.api.platform.Health;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Main internal interface for Multiversus plugins, providing the base for
 * abstraction throughout the project.
 * <p>
 * All plugin platforms implement this interface.
 */
public interface MultiPlugin {

    /**
     * Gets the bootstrap plugin instance
     *
     * @return the bootstrap plugin
     */
    MultiBootstrap getBootstrap();

    /**
     * Gets the user manager instance for the platform
     *
     * @return the user manager
     */
    UserManager<? extends User> getUserManager();

    /**
     * Gets the plugin's configuration
     *
     * @return the plugin config
     */
    MultiConfiguration getConfiguration();

    /**
     * Gets the primary data storage instance. This is likely to be wrapped with extra layers for caching, etc.
     *
     * @return the storage handler instance
     */
    Storage getStorage();

    /**
     * Gets a wrapped logger instance for the platform.
     *
     * @return the plugin's logger
     */
    PluginLogger getLogger();

    /**
     * Gets the messaging service.
     *
     * @return the messaging service
     */
    Optional<InternalMessagingService> getMessagingService();

    /**
     * Sets the messaging service.
     *
     * @param service the service
     */
    void setMessagingService(InternalMessagingService service);

    /**
     * Gets the event dispatcher
     *
     * @return the event dispatcher
     */
    EventDispatcher getEventDispatcher();

    /**
     * Returns the class implementing the MultiversusAPI on this platform.
     *
     * @return the api
     */
    MultiversusApiProvider getApiProvider();

    /**
     * Gets the extension manager.
     *
     * @return the extension manager
     */
    SimpleExtensionManager getExtensionManager();

    /**
     * Gets the command manager
     *
     * @return the command manager
     */
    CommandManager getCommandManager();

    /**
     * Gets the connection listener.
     *
     * @return the connection listener
     */
    AbstractConnectionListener getConnectionListener();

    /**
     * Gets the instance providing locale translations for the plugin
     *
     * @return the translation manager
     */
    TranslationManager getTranslationManager();

    /**
     * Gets the dependency manager for the plugin
     *
     * @return the dependency manager
     */
    DependencyManager getDependencyManager();

    /**
     * Gets the file watcher running on the platform
     *
     * @return the file watcher
     */
    Optional<FileWatcher> getFileWatcher();

    /**
     * Runs a health check for the plugin.
     *
     * @return the result of the healthcheck
     */
    Health runHealthCheck();

    /**
     * Lookup a uuid from a username.
     *
     * @param username the username to lookup
     * @return an optional uuid, if found
     */
    Optional<UUID> lookupUniqueId(String username);

    /**
     * Lookup a username from a uuid.
     *
     * @param uniqueId the uuid to lookup
     * @return an optional username, if found
     */
    Optional<String> lookupUsername(UUID uniqueId);

    /**
     * Tests whether the given username is valid.
     *
     * @param username the username
     * @return true if valid
     */
    boolean testUsernameValidity(String username);

    /**
     * Gets a list of online Senders on the platform
     *
     * @return a {@link List} of senders online on the platform
     */
    Stream<Sender> getOnlineSenders();

    /**
     * Gets the console.
     *
     * @return the console sender of the instance
     */
    Sender getConsoleSender();

    default List<Command<?>> getExtraCommands() {
        return Collections.emptyList();
    }

    /**
     * Gets the sync task buffer of the platform, used for scheduling and running sync tasks.
     *
     * @return the sync task buffer instance
     */
    SyncTask.Buffer getSyncTaskBuffer();

    /**
     * Called at the end of the sync task.
     */
    default void performPlatformDataSync() {

    }
}
