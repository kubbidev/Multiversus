package me.kubbidev.multiversus.listeners;

import me.kubbidev.multiversus.FBukkitPlugin;
import me.kubbidev.multiversus.config.ConfigKeys;
import me.kubbidev.multiversus.locale.Message;
import me.kubbidev.multiversus.locale.TranslationManager;
import me.kubbidev.multiversus.model.User;
import me.kubbidev.multiversus.plugin.util.AbstractConnectionListener;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class BukkitConnectionListener extends AbstractConnectionListener implements Listener {
    private static final Predicate<? super String> IS_CRAFTBUKKIT_PREDICATE = Pattern.compile("^(?:git|\\d+)-Bukkit-[0-9a-f]{7}(?: .*)?$").asPredicate();

    private final FBukkitPlugin plugin;

    private final boolean detectedCraftBukkitOfflineMode;

    private final Set<UUID> deniedAsyncLogin = Collections.synchronizedSet(new HashSet<>());
    private final Set<UUID> deniedLogin = Collections.synchronizedSet(new HashSet<>());

    public BukkitConnectionListener(FBukkitPlugin plugin) {
        super(plugin);
        this.plugin = plugin;

        // check for craftbukkit + offline mode combination
        String version = plugin.getBootstrap().getServer().getVersion();
        boolean onlineMode = plugin.getBootstrap().getServer().getOnlineMode();

        if (!onlineMode && IS_CRAFTBUKKIT_PREDICATE.test(version)) {
            printCraftBukkitOfflineModeError();
            this.detectedCraftBukkitOfflineMode = true;
        } else {
            this.detectedCraftBukkitOfflineMode = false;
        }
    }

    private void printCraftBukkitOfflineModeError() {
        this.plugin.getLogger().warn("It appears that your server is running CraftBukkit and configured in offline (cracked) mode.");
        this.plugin.getLogger().warn("Due to a CraftBukkit limitation, Multiversus cannot function correctly in this setup.");
        this.plugin.getLogger().warn("To resolve this, please either a) upgrade from CraftBukkit to Spigot or Paper, or b) enable online-mode.");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
        /* Called when the player first attempts a connection with the server.
           Listening on LOW priority to allow plugins to modify username / UUID data here. (auth plugins)
           Also, give other plugins a chance to cancel the event. */

        /* wait for the plugin to enable. because these events are fired async, they can be called before
           the plugin has enabled.  */
        try {
            this.plugin.getBootstrap().getEnableLatch().await(60, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        if (this.plugin.getConfiguration().get(ConfigKeys.DEBUG_LOGINS)) {
            this.plugin.getLogger().info("Processing pre-login for " + e.getUniqueId() + " - " + e.getName());
        }

        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            // another plugin has disallowed the login.
            this.plugin.getLogger().info("Another plugin has cancelled the connection for " + e.getUniqueId() + " - " + e.getName() + ". No data will be loaded.");
            this.deniedAsyncLogin.add(e.getUniqueId());
            return;
        }

        /* Actually process the login for the connection.
           We do this here to delay the login until the data is ready.
           If the login gets cancelled later on, then this will be cleaned up.

           This includes:
           - loading uuid data
           - creating a user instance in the UserManager for this connection.
           - setting up cached data. */
        try {
            User user = loadUser(e.getUniqueId(), e.getName());
            recordConnection(e.getUniqueId());
            this.plugin.getEventDispatcher().dispatchPlayerLoginProcess(e.getUniqueId(), e.getName(), user);
        } catch (Exception ex) {
            this.plugin.getLogger().severe("Exception occurred whilst loading data for " + e.getUniqueId() + " - " + e.getName(), ex);

            // deny the connection
            this.deniedAsyncLogin.add(e.getUniqueId());

            Component reason = TranslationManager.render(Message.LOADING_DATABASE_ERROR.build());
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, reason);
            this.plugin.getEventDispatcher().dispatchPlayerLoginProcess(e.getUniqueId(), e.getName(), null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPreLoginMonitor(AsyncPlayerPreLoginEvent e) {
        /* Listen to see if the event was cancelled after we initially handled the connection
           If the connection was cancelled here, we need to do something to clean up the data that was loaded. */

        // Check to see if this connection was denied at LOW.
        if (this.deniedAsyncLogin.remove(e.getUniqueId())) {
            // their data was never loaded at LOW priority, now check to see if they have been magically allowed since then.

            // This is a problem, as they were denied at low priority, but are now being allowed.
            if (e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                this.plugin.getLogger().severe("Player connection was re-allowed for " + e.getUniqueId());
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.empty());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        /* Called when the player starts logging into the server.
           At this point, the users data should be present and loaded. */

        Player player = e.getPlayer();

        if (this.plugin.getConfiguration().get(ConfigKeys.DEBUG_LOGINS)) {
            this.plugin.getLogger().info("Processing login for " + player.getUniqueId() + " - " + player.getName());
        }

        User user = this.plugin.getUserManager().getIfLoaded(player.getUniqueId());

        /* User instance is null for whatever reason. Could be that it was unloaded between asyncpre and now. */
        if (user == null) {
            this.deniedLogin.add(player.getUniqueId());

            if (!getUniqueConnections().contains(player.getUniqueId())) {

                this.plugin.getLogger().warn("User " + player.getUniqueId() + " - " + player.getName() +
                        " doesn't have data pre-loaded, they have never been processed during pre-login in this session." +
                        " - denying login.");

                if (this.detectedCraftBukkitOfflineMode) {
                    printCraftBukkitOfflineModeError();

                    Component reason = TranslationManager.render(Message.LOADING_STATE_ERROR_CB_OFFLINE_MODE.build(), player.locale());
                    e.disallow(PlayerLoginEvent.Result.KICK_OTHER, reason);
                    return;
                }

            } else {
                this.plugin.getLogger().warn("User " + player.getUniqueId() + " - " + player.getName() +
                        " doesn't currently have data pre-loaded, but they have been processed before in this session." +
                        " - denying login.");
            }

            Component reason = TranslationManager.render(Message.LOADING_STATE_ERROR.build(), player.locale());
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, reason);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLoginMonitor(PlayerLoginEvent e) {
        /* Listen to see if the event was cancelled after we initially handled the login
           If the connection was cancelled here, we need to do something to clean up the data that was loaded. */

        // Check to see if this connection was denied at LOW. Even if it was denied at LOW, their data will still be present.
        if (this.deniedLogin.remove(e.getPlayer().getUniqueId())) {
            // This is a problem, as they were denied at low priority, but are now being allowed.
            if (e.getResult() == PlayerLoginEvent.Result.ALLOWED) {
                this.plugin.getLogger().severe("Player connection was re-allowed for " + e.getPlayer().getUniqueId());
                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.empty());
            }
        }
    }

    // Wait until the last priority to unload, so plugins can still perform checks on this event
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        handleDisconnect(player.getUniqueId());
    }
}