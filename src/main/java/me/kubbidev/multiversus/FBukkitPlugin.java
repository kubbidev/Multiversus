package me.kubbidev.multiversus;

import me.kubbidev.multiversus.api.MultiversusApiProvider;
import me.kubbidev.multiversus.brigadier.MultiBrigadier;
import me.kubbidev.multiversus.config.ConfigKeys;
import me.kubbidev.multiversus.config.generic.adapter.ConfigurationAdapter;
import me.kubbidev.multiversus.core.listener.AttackEventListener;
import me.kubbidev.multiversus.core.manager.DamageManager;
import me.kubbidev.multiversus.core.manager.EntityManager;
import me.kubbidev.multiversus.core.manager.FakeEventManager;
import me.kubbidev.multiversus.core.manager.IndicatorManager;
import me.kubbidev.multiversus.dependencies.Dependency;
import me.kubbidev.multiversus.event.AbstractEventBus;
import me.kubbidev.multiversus.listeners.BukkitConnectionListener;
import me.kubbidev.multiversus.messaging.MessagingFactory;
import me.kubbidev.multiversus.model.manager.user.StandardUserManager;
import me.kubbidev.multiversus.plugin.AbstractMultiPlugin;
import me.kubbidev.multiversus.plugin.util.AbstractConnectionListener;
import me.kubbidev.multiversus.sender.Sender;
import net.multiversus.api.Multiversus;
import net.multiversus.api.event.sync.ConfigReloadEvent;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Multiversus implementation for the Bukkit API.
 */
public class FBukkitPlugin extends AbstractMultiPlugin {
    private final FBukkitBootstrap bootstrap;

    private BukkitSenderFactory senderFactory;
    private BukkitConnectionListener connectionListener;
    private BukkitCommandExecutor commandManager;
    private StandardUserManager userManager;

    private DamageManager damageManager;
    private EntityManager entityManager;

    private final IndicatorManager indicatorManager = new IndicatorManager();
    private final FakeEventManager fakeEventManager = new FakeEventManager();

    public FBukkitPlugin(FBukkitBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public FBukkitBootstrap getBootstrap() {
        return this.bootstrap;
    }

    public JavaPlugin getLoader() {
        return this.bootstrap.getLoader();
    }

    @Override
    protected void setupSenderFactory() {
        this.senderFactory = new BukkitSenderFactory(this);
    }

    @Override
    protected Set<Dependency> getGlobalDependencies() {
        Set<Dependency> dependencies = super.getGlobalDependencies();
        if (isBrigadierSupported()) {
            dependencies.add(Dependency.COMMODORE);
            dependencies.add(Dependency.COMMODORE_FILE);
        }
        return dependencies;
    }

    @Override
    protected ConfigurationAdapter provideConfigurationAdapter() {
        return new BukkitConfigAdapter(this, resolveConfig("config.yml").toFile());
    }

    @Override
    protected void registerPlatformListeners() {
        this.connectionListener = new BukkitConnectionListener(this);
        this.bootstrap.getServer().getPluginManager().registerEvents(this.connectionListener, this.bootstrap.getLoader());

        this.damageManager = new DamageManager(this);
        this.bootstrap.getServer().getPluginManager().registerEvents(this.damageManager, this.bootstrap.getLoader());
        this.bootstrap.getServer().getPluginManager().registerEvents(new AttackEventListener(this), this.bootstrap.getLoader());
    }

    @Override
    protected MessagingFactory<?> provideMessagingFactory() {
        return new BukkitMessagingFactory(this);
    }

    @Override
    protected void registerCommands() {
        PluginCommand command = this.bootstrap.getLoader().getCommand("multiversus");
        if (command == null) {
            getLogger().severe("Unable to register /multiversus command with the server");
            return;
        }

        if (isAsyncTabCompleteSupported()) {
            this.commandManager = new BukkitAsyncCommandExecutor(this, command);
        } else {
            this.commandManager = new BukkitCommandExecutor(this, command);
        }

        this.commandManager.register();

        // setup brigadier
        if (isBrigadierSupported() && getConfiguration().get(ConfigKeys.REGISTER_COMMAND_LIST_DATA)) {
            try {
                MultiBrigadier.register(this, command);
            } catch (Exception e) {
                if (!(e instanceof RuntimeException && e.getMessage().contains("not supported by the server"))) {
                    e.printStackTrace();
                }
            }
        }

        this.bootstrap.getServer().getCommandMap().getKnownCommands()
                .remove("callback");
    }

    @Override
    protected void setupManagers() {
        this.userManager = new StandardUserManager(this);
        this.entityManager = new EntityManager();

        // load indicators from configuration file
        this.indicatorManager.load(this);
    }

    @Override
    protected void setupPlatformHooks() {

    }

    @Override
    protected AbstractEventBus<?> provideEventBus(MultiversusApiProvider apiProvider) {
        return new BukkitEventBus(this, apiProvider);
    }

    @Override
    protected void registerApiOnPlatform(Multiversus api) {
        this.bootstrap.getServer().getServicesManager().register(Multiversus.class, api, this.bootstrap.getLoader(), ServicePriority.Normal);
    }

    @Override
    protected void performFinalSetup() {
        // Load any online users (in the case of a reload)
        for (Player player : this.bootstrap.getServer().getOnlinePlayers()) {
            this.bootstrap.getScheduler().executeAsync(() -> {
                try {
                    this.connectionListener.loadUser(player.getUniqueId(), player.getName());
                } catch (Exception e) {
                    getLogger().severe("Exception occurred whilst loading data for " +
                            player.getUniqueId() + " - " + player.getName(), e);
                }
            });
        }
        //noinspection resource
        getEventDispatcher().getEventBus().subscribe(ConfigReloadEvent.class, this::onConfigReload);
    }

    @Override
    protected void removePlatformHooks() {
        // Unload players
        for (Player player : this.bootstrap.getServer().getOnlinePlayers()) {
            getUserManager().unload(player.getUniqueId());
        }
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean isBrigadierSupported() {
        return classExists("com.mojang.brigadier.CommandDispatcher");
    }

    private static boolean isAsyncTabCompleteSupported() {
        return classExists("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent");
    }

    private void onConfigReload(ConfigReloadEvent e) {
        // reload indicators configuration as well
        this.indicatorManager.reload(this);
    }

    @Override
    public Stream<Sender> getOnlineSenders() {
        List<Player> players = new ArrayList<>(this.bootstrap.getServer().getOnlinePlayers());
        return Stream.concat(
                Stream.of(getConsoleSender()),
                players.stream().map(p -> getSenderFactory().wrap(p))
        );
    }

    @Override
    public Sender getConsoleSender() {
        return getSenderFactory().wrap(this.bootstrap.getConsole());
    }

    public BukkitSenderFactory getSenderFactory() {
        return this.senderFactory;
    }

    @Override
    public AbstractConnectionListener getConnectionListener() {
        return this.connectionListener;
    }

    @Override
    public BukkitCommandExecutor getCommandManager() {
        return this.commandManager;
    }

    @Override
    public StandardUserManager getUserManager() {
        return this.userManager;
    }

    public DamageManager getDamageManager() {
        return this.damageManager;
    }

    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    public FakeEventManager getFakeEventManager() {
        return this.fakeEventManager;
    }
}