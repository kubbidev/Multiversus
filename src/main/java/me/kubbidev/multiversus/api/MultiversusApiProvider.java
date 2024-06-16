package me.kubbidev.multiversus.api;

import me.kubbidev.multiversus.api.implementation.ApiMessagingService;
import me.kubbidev.multiversus.api.implementation.ApiPlatform;
import me.kubbidev.multiversus.api.implementation.ApiUserManager;
import me.kubbidev.multiversus.config.ConfigKeys;
import me.kubbidev.multiversus.messaging.MultiMessagingService;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.plugin.bootstrap.BootstrappedWithLoader;
import me.kubbidev.multiversus.plugin.bootstrap.MultiBootstrap;
import me.kubbidev.multiversus.plugin.logging.PluginLogger;
import net.multiversus.api.Multiversus;
import net.multiversus.api.MultiversusProvider;
import net.multiversus.api.event.EventBus;

import net.multiversus.api.messaging.MessagingService;
import net.multiversus.api.messenger.MessengerProvider;
import net.multiversus.api.model.user.UserManager;
import net.multiversus.api.platform.Health;
import net.multiversus.api.platform.Platform;
import net.multiversus.api.platform.PluginMetadata;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implements the Multiversus API using the plugin instance
 */
public class MultiversusApiProvider implements Multiversus {

    private final MultiPlugin plugin;

    private final ApiPlatform platform;
    private final UserManager userManager;

    public MultiversusApiProvider(MultiPlugin plugin) {
        this.plugin = plugin;

        this.platform = new ApiPlatform(plugin);
        this.userManager = new ApiUserManager(plugin, plugin.getUserManager());
    }

    public void ensureApiWasLoadedByPlugin() {
        MultiBootstrap bootstrap = this.plugin.getBootstrap();
        ClassLoader pluginClassLoader;
        if (bootstrap instanceof BootstrappedWithLoader) {
            pluginClassLoader = ((BootstrappedWithLoader) bootstrap).getLoader().getClass().getClassLoader();
        } else {
            pluginClassLoader = bootstrap.getClass().getClassLoader();
        }

        for (Class<?> apiClass : new Class[]{Multiversus.class, MultiversusProvider.class}) {
            ClassLoader apiClassLoader = apiClass.getClassLoader();

            if (!apiClassLoader.equals(pluginClassLoader)) {
                String guilty = "unknown";
                try {
                    guilty = bootstrap.identifyClassLoader(apiClassLoader);
                } catch (Exception e) {
                    // ignore
                }

                PluginLogger logger = this.plugin.getLogger();
                logger.warn("It seems that the Multiversus API has been (class)loaded by a plugin other than Multiversus!");
                logger.warn("The API was loaded by " + apiClassLoader + " (" + guilty + ") and the " +
                        "Multiversus plugin was loaded by " + pluginClassLoader.toString() + ".");
                logger.warn("This indicates that the other plugin has incorrectly \"shaded\" the " +
                        "Multiversus API into its jar file. This can cause errors at runtime and should be fixed.");
                return;
            }
        }
    }

    @Override
    public @NotNull UserManager getUserManager() {
        return this.userManager;
    }

    @Override
    public @NotNull Platform getPlatform() {
        return this.platform;
    }

    @Override
    public @NotNull PluginMetadata getPluginMetadata() {
        return this.platform;
    }

    @Override
    public @NotNull EventBus getEventBus() {
        return  this.plugin.getEventDispatcher().getEventBus();
    }

    @Override
    public @NotNull Optional<MessagingService> getMessagingService() {
        return this.plugin.getMessagingService().map(ApiMessagingService::new);
    }

    @Override
    public @NotNull CompletableFuture<Void> runUpdateTask() {
        return this.plugin.getSyncTaskBuffer().request();
    }

    @Override
    public @NotNull Health runHealthCheck() {
        return this.plugin.runHealthCheck();
    }

    @Override
    public void registerMessengerProvider(@NotNull MessengerProvider messengerProvider) {
        if (this.plugin.getConfiguration().get(ConfigKeys.MESSAGING_SERVICE).equals("custom")) {
            this.plugin.setMessagingService(new MultiMessagingService(this.plugin, messengerProvider));
        }
    }
}