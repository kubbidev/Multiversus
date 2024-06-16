package me.kubbidev.multiversus.core.manager;

import me.kubbidev.multiversus.core.event.attack.fake.DamageCheckEvent;
import me.kubbidev.multiversus.core.interaction.InteractionRestriction;
import me.kubbidev.multiversus.core.interaction.InteractionType;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public final class EntityManager {
    private final Set<InteractionRestriction> restrictions = new HashSet<>();

    /**
     * This should be called by plugins implementing player sets like parties, friends, factions....
     * any set that could support friendly fire.
     * <p>
     * This is also helpful to prevent players from interacting with
     * specific invulnerable entities like NPCs.
     *
     * @param restriction The new restriction for entities
     * @see InteractionRestriction
     */
    public void registerRestriction(InteractionRestriction restriction) {
        this.restrictions.add(restriction);
    }

    /**
     * Called whenever an entity tries to damage OR buff another entity.
     * <p>
     * This should be used by:
     * <br>- plugins which implement friendly fire player sets like parties, guilds, nations, factions....
     * <br>- plugins which implement custom invulnerable entities like NPCs, sentinels....
     *
     * @param source The entity targeting another entity
     * @param target The entity Entity being targeted
     * @param type   The type of interaction, whether it's positive (buff, heal) or negative (offense skill, attack)
     * @return True if the interaction between the two entity is possible, otherwise false (should be cancelled!)
     */
    public boolean canInteract(Entity source, Entity target, InteractionType type) {

        // simple verification
        if (source.equals(target) || target.isDead()
                || !(source instanceof LivingEntity)
                || !(target instanceof LivingEntity) || target instanceof ArmorStand)
            return false;

        // specific plugin restrictions
        for (InteractionRestriction restriction : this.restrictions) {
            if (!restriction.canTarget((LivingEntity) source, (LivingEntity) target, type)) {
                return false;
            }
        }

        // pvp interaction rules
        if (target instanceof Player) {
            boolean pvpEnabled = target.getWorld().getPVP();
            if (pvpEnabled) {
                pvpEnabled = new DamageCheckEvent(source, target, type).callEvent();
            }
            // if offense, cancel if the pvp is disabled
            return !(type.isOffense() && !pvpEnabled);
        }
        // TODO maybe verification for relationship between entities also?
        // TODO (support skills enabled on mob)
        return true;
    }
}