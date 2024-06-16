package me.kubbidev.multiversus.plugin;

import me.kubbidev.multiversus.api.ApiRegistrationUtil;
import me.kubbidev.multiversus.api.MultiversusApiProvider;
import me.kubbidev.multiversus.config.ConfigKeys;
import me.kubbidev.multiversus.config.MultiConfiguration;
import me.kubbidev.multiversus.config.generic.adapter.ConfigurationAdapter;
import me.kubbidev.multiversus.config.generic.adapter.EnvironmentVariableConfigAdapter;
import me.kubbidev.multiversus.config.generic.adapter.MultiConfigurationAdapter;
import me.kubbidev.multiversus.config.generic.adapter.SystemPropertyConfigAdapter;
import me.kubbidev.multiversus.dependencies.Dependency;
import me.kubbidev.multiversus.dependencies.DependencyManager;
import me.kubbidev.multiversus.dependencies.DependencyManagerImpl;
import me.kubbidev.multiversus.event.AbstractEventBus;
import me.kubbidev.multiversus.event.EventDispatcher;
import me.kubbidev.multiversus.event.gen.GeneratedEventClass;
import me.kubbidev.multiversus.extension.SimpleExtensionManager;
import me.kubbidev.multiversus.locale.Message;
import me.kubbidev.multiversus.locale.TranslationManager;
import me.kubbidev.multiversus.messaging.InternalMessagingService;
import me.kubbidev.multiversus.messaging.MessagingFactory;
import me.kubbidev.multiversus.plugin.logging.PluginLogger;
import me.kubbidev.multiversus.plugin.util.HealthCheckResult;
import me.kubbidev.multiversus.storage.Storage;
import me.kubbidev.multiversus.storage.StorageFactory;
import me.kubbidev.multiversus.storage.StorageMetadata;
import me.kubbidev.multiversus.storage.implementation.file.watcher.FileWatcher;
import me.kubbidev.multiversus.storage.misc.DataConstraints;
import me.kubbidev.multiversus.tasks.SyncTask;
import net.multiversus.api.Multiversus;
import net.multiversus.api.platform.Health;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class AbstractMultiPlugin implements MultiPlugin {

    // init during load
    private DependencyManager dependencyManager;
    private TranslationManager translationManager;

    // init during enable
    private MultiConfiguration configuration;
    private FileWatcher fileWatcher = null;
    private Storage storage;
    private InternalMessagingService messagingService = null;
    private SyncTask.Buffer syncTaskBuffer;
    private MultiversusApiProvider apiProvider;
    private EventDispatcher eventDispatcher;
    private SimpleExtensionManager extensionManager;

    private boolean running = false;

    /**
     * Performs the initial actions to load the plugin
     */
    public final void load() {
        // load dependencies
        this.dependencyManager = createDependencyManager();
        this.dependencyManager.loadDependencies(getGlobalDependencies());

        // load translations
        this.translationManager = new TranslationManager(this);
        this.translationManager.reload();
    }

    public final void enable() {
        // load the sender factory instance
        setupSenderFactory();

        // send the startup banner
        Message.STARTUP_BANNER.send(getConsoleSender(), getBootstrap());

        // load configuration
        getLogger().info("Loading configuration...");
        ConfigurationAdapter configFileAdapter = provideConfigurationAdapter();
        this.configuration = new MultiConfiguration(this, new MultiConfigurationAdapter(this,
                new SystemPropertyConfigAdapter(this),
                new EnvironmentVariableConfigAdapter(this),
                configFileAdapter
        ));

        // now the configuration is loaded, we can create a storage factory and load initial dependencies
        StorageFactory storageFactory = new StorageFactory(this);
        this.dependencyManager.loadStorageDependencies(
                storageFactory.getRequiredTypes(),
                getConfiguration().get(ConfigKeys.REDIS_ENABLED),
                getConfiguration().get(ConfigKeys.RABBITMQ_ENABLED),
                getConfiguration().get(ConfigKeys.NATS_ENABLED)
        );

        // register listeners
        registerPlatformListeners();

        // initialise the storage
        // first, setup the file watcher, if enabled
        if (getConfiguration().get(ConfigKeys.WATCH_FILES)) {
            try {
                this.fileWatcher = new FileWatcher(this, getBootstrap().getDataDirectory());
            } catch (Throwable e) {
                // catch throwable here, seems some JVMs throw UnsatisfiedLinkError when trying
                // to create a watch service.
                getLogger().warn("Error occurred whilst trying to create a file watcher:", e);
            }
        }

        // initialise storage
        this.storage = storageFactory.getInstance();
        this.messagingService = provideMessagingFactory().getInstance();

        // setup the update task buffer
        this.syncTaskBuffer = new SyncTask.Buffer(this);

        // register commands
        registerCommands();

        // setup user manager
        setupManagers();

        // setup platform hooks
        setupPlatformHooks();

        // register with the Multiversus API
        this.apiProvider = new MultiversusApiProvider(this);
        this.apiProvider.ensureApiWasLoadedByPlugin();
        this.eventDispatcher = new EventDispatcher(provideEventBus(this.apiProvider));
        getBootstrap().getScheduler().executeAsync(GeneratedEventClass::preGenerate);
        ApiRegistrationUtil.registerProvider(this.apiProvider);
        registerApiOnPlatform(this.apiProvider);

        // setup extension manager
        this.extensionManager = new SimpleExtensionManager(this);
        this.extensionManager.loadExtensions(getBootstrap().getConfigDirectory().resolve("extensions"));

        // schedule update tasks
        int syncMins = getConfiguration().get(ConfigKeys.SYNC_TIME);
        if (syncMins > 0) {
            getBootstrap().getScheduler().asyncRepeating(() -> this.syncTaskBuffer.request(), syncMins, TimeUnit.MINUTES);
        }

        // run an update instantly.
        getLogger().info("Performing initial data load...");
        try {
            new SyncTask(this).run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // perform any platform-specific final setup tasks
        performFinalSetup();

        // mark as running
        this.running = true;

        Duration timeTaken = Duration.between(getBootstrap().getStartupTime(), Instant.now());
        getLogger().info("Successfully enabled. (took " + timeTaken.toMillis() + "ms)");
    }

    public final void disable() {
        getLogger().info("Starting shutdown process...");

        // cancel delayed/repeating tasks
        getBootstrap().getScheduler().shutdownScheduler();

        // unload extensions
        this.extensionManager.close();

        // mark as not running
        this.running = false;

        // remove any hooks into the platform
        removePlatformHooks();

        // close messaging service
        if (this.messagingService != null) {
            getLogger().info("Closing messaging service...");
            this.messagingService.close();
        }

        // close storage
        getLogger().info("Closing storage...");
        this.storage.shutdown();

        // close file watcher
        if (this.fileWatcher != null) {
            this.fileWatcher.close();
        }

        // unregister api
        ApiRegistrationUtil.unregisterProvider();

        // shutdown async executor pool
        getBootstrap().getScheduler().shutdownExecutor();

        // close isolated loaders for non-relocated dependencies
        getDependencyManager().close();

        // close classpath appender
        getBootstrap().getClassPathAppender().close();

        getLogger().info("Goodbye!");
    }

    // hooks called during load

    protected DependencyManager createDependencyManager() {
        return new DependencyManagerImpl(this);
    }

    protected Set<Dependency> getGlobalDependencies() {
        return EnumSet.of(
                Dependency.CAFFEINE,
                Dependency.OKIO,
                Dependency.OKHTTP,
                Dependency.BYTEBUDDY,
                Dependency.EVENT
        );
    }

    // hooks called during enable

    protected abstract void setupSenderFactory();

    protected abstract ConfigurationAdapter provideConfigurationAdapter();

    protected abstract void registerPlatformListeners();

    protected abstract MessagingFactory<?> provideMessagingFactory();

    protected abstract void registerCommands();

    protected abstract void setupManagers();

    protected abstract void setupPlatformHooks();

    protected abstract AbstractEventBus<?> provideEventBus(MultiversusApiProvider apiProvider);

    protected abstract void registerApiOnPlatform(Multiversus api);

    protected abstract void performFinalSetup();

    // hooks called during disable

    protected void removePlatformHooks() {}

    protected Path resolveConfig(String fileName) {
        Path configFile = getBootstrap().getConfigDirectory().resolve(fileName);

        // if the config doesn't exist, create it based on the template in the resources dir
        if (!Files.exists(configFile)) {
            try {
                Files.createDirectories(configFile.getParent());
            } catch (IOException e) {
                // ignore
            }

            try (InputStream is = getBootstrap().getResourceStream(fileName)) {
                Files.copy(is, configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return configFile;
    }

    @Override
    public PluginLogger getLogger() {
        return getBootstrap().getPluginLogger();
    }

    @Override
    public void setMessagingService(InternalMessagingService messagingService) {
        if (this.messagingService == null) {
            this.messagingService = messagingService;
        }
    }

    @Override
    public Health runHealthCheck() {
        if (!this.running) {
            return HealthCheckResult.unhealthy(Collections.emptyMap());
        }

        StorageMetadata meta = this.storage.getMeta();
        if (meta.connected() != null && !meta.connected()) {
            return HealthCheckResult.unhealthy(Collections.singletonMap("reason", "storage disconnected"));
        }

        Map<String, Object> map = new LinkedHashMap<>();
        if (meta.connected() != null) {
            map.put("storageConnected", meta.connected());
        }
        if (meta.ping() != null) {
            map.put("storagePing", meta.ping());
        }
        if (meta.sizeBytes() != null) {
            map.put("storageSizeBytes", meta.sizeBytes());
        }

        return HealthCheckResult.healthy(map);
    }

    @Override
    public Optional<UUID> lookupUniqueId(String username) {
        // get a result from the DB cache
        UUID uniqueId = getStorage().getPlayerUniqueId(username.toLowerCase(Locale.ROOT)).join();

        // fire the event
        uniqueId = getEventDispatcher().dispatchUniqueIdLookup(username, uniqueId);

        // try the servers cache
        if (uniqueId == null && getConfiguration().get(ConfigKeys.USE_SERVER_UUID_CACHE)) {
            uniqueId = getBootstrap().lookupUniqueId(username).orElse(null);
        }

        return Optional.ofNullable(uniqueId);
    }

    @Override
    public Optional<String> lookupUsername(UUID uniqueId) {
        // get a result from the DB cache
        String username = getStorage().getPlayerName(uniqueId).join();

        // fire the event
        username = getEventDispatcher().dispatchUsernameLookup(uniqueId, username);

        // try the servers cache
        if (username == null && getConfiguration().get(ConfigKeys.USE_SERVER_UUID_CACHE)) {
            username = getBootstrap().lookupUsername(uniqueId).orElse(null);
        }

        return Optional.ofNullable(username);
    }

    @Override
    public boolean testUsernameValidity(String username) {
        // if the username doesn't even pass the lenient test, don't bother going any further
        // it's either empty, or too long to fit in the sql column
        if (!DataConstraints.PLAYER_USERNAME_TEST_LENIENT.test(username)) {
            return false;
        }

        // if invalid usernames are allowed in the config, set valid to true, otherwise, use the more strict test
        boolean valid = getConfiguration().get(ConfigKeys.ALLOW_INVALID_USERNAMES) || DataConstraints.PLAYER_USERNAME_TEST.test(username);

        // fire the event & return
        return getEventDispatcher().dispatchUsernameValidityCheck(username, valid);
    }

    @Override
    public DependencyManager getDependencyManager() {
        return this.dependencyManager;
    }

    @Override
    public TranslationManager getTranslationManager() {
        return this.translationManager;
    }

    @Override
    public MultiConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public Optional<FileWatcher> getFileWatcher() {
        return Optional.ofNullable(this.fileWatcher);
    }

    @Override
    public Storage getStorage() {
        return this.storage;
    }

    @Override
    public Optional<InternalMessagingService> getMessagingService() {
        return Optional.ofNullable(this.messagingService);
    }

    @Override
    public SyncTask.Buffer getSyncTaskBuffer() {
        return this.syncTaskBuffer;
    }

    @Override
    public MultiversusApiProvider getApiProvider() {
        return this.apiProvider;
    }

    @Override
    public SimpleExtensionManager getExtensionManager() {
        return this.extensionManager;
    }

    @Override
    public EventDispatcher getEventDispatcher() {
        return this.eventDispatcher;
    }

    public static String getPluginName() {
        LocalDate date = LocalDate.now();
        if (date.getMonth() == Month.APRIL && date.getDayOfMonth() == 1) {
            return "Singleversus";
        }
        return "Multiversus";
    }
}
