package me.kubbidev.multiversus.plugin.util;

import me.kubbidev.multiversus.model.User;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import net.multiversus.api.model.PlayerSaveResult;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract listener utility for handling new player connections
 */
public abstract class AbstractConnectionListener {
    private final MultiPlugin plugin;
    private final Set<UUID> uniqueConnections = ConcurrentHashMap.newKeySet();

    protected AbstractConnectionListener(MultiPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the unique players which have connected to the server since it started.
     *
     * @return the unique connections
     */
    public Set<UUID> getUniqueConnections() {
        return this.uniqueConnections;
    }

    protected void recordConnection(UUID uniqueId) {
        this.uniqueConnections.add(uniqueId);
    }

    public User loadUser(UUID uniqueId, String username) {
        long startTime = System.currentTimeMillis();

        // register with the housekeeper to avoid accidental unloads
        this.plugin.getUserManager().getHouseKeeper().registerUsage(uniqueId);

        // save uuid data.
        PlayerSaveResult saveResult = this.plugin.getStorage().savePlayerData(uniqueId, username).join();

        // fire UserFirstLogin event
        if (saveResult.includes(PlayerSaveResult.Outcome.CLEAN_INSERT)) {
            this.plugin.getEventDispatcher().dispatchUserFirstLogin(uniqueId, username);
        }

        // most likely because ip forwarding is not setup correctly
        // print a warning to the console
        if (saveResult.includes(PlayerSaveResult.Outcome.OTHER_UNIQUE_IDS_PRESENT_FOR_USERNAME)) {
            Set<UUID> otherUuids = saveResult.getOtherUniqueIds();

            this.plugin.getLogger().warn("Multiversus already has data for player '" + username + "' - but this data is stored under a different UUID.");
            this.plugin.getLogger().warn("'" + username + "' has previously used the unique ids " + otherUuids + " but is now connecting with '" + uniqueId + "'");

            if (uniqueId.version() == 4) {
                this.plugin.getLogger().warn("The UUID the player is connecting with now is Mojang-assigned (type 4). This implies that one of the other servers in your network is not authenticating correctly.");
                this.plugin.getLogger().warn("If you're using BungeeCord/Velocity, please ensure that IP-Forwarding is setup correctly on all of your backend servers!");
            } else {
                this.plugin.getLogger().warn("The UUID the player is connecting with now is NOT Mojang-assigned (type " + uniqueId.version() + "). This implies that THIS server is not authenticating correctly, but one (or more) of the other servers/proxies in the network are.");
                this.plugin.getLogger().warn("If you're using BungeeCord/Velocity, please ensure that IP-Forwarding is setup correctly on all of your backend servers!");
            }
        }

        User user = this.plugin.getStorage().loadUser(uniqueId, username).join();
        if (user == null) {
            throw new NullPointerException("User is null");
        }

        long time = System.currentTimeMillis() - startTime;
        if (time >= 1000) {
            this.plugin.getLogger().warn("Processing login for " + username + " took " + time + "ms.");
        }

        return user;
    }

    public void handleDisconnect(UUID uniqueId) {
        // Register with the housekeeper, so the User's instance will stick
        // around for a bit after they disconnect
        this.plugin.getUserManager().getHouseKeeper().registerUsage(uniqueId);
    }
}