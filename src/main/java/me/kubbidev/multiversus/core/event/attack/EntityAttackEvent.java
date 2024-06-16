package me.kubbidev.multiversus.core.event.attack;

import com.google.common.base.Preconditions;
import me.kubbidev.multiversus.core.damage.AttackMetadata;
import me.kubbidev.multiversus.core.damage.EntityMetadata;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * An attack that is called by an entity.
 */
public class EntityAttackEvent extends AttackEvent implements Cancellable {
    private final EntityMetadata attacker;

    /**
     * Called whenever an entity deals damage to another entity.
     *
     * @param event  The corresponding damage event.
     * @param attack The generated attack result which can be edited.
     */
    public EntityAttackEvent(EntityDamageEvent event, AttackMetadata attack) {
        super(event, attack);
        Preconditions.checkArgument(attack.hasAttacker(), "Attack was not performed by an entity");
        this.attacker = attack.getAttacker();
    }

    public EntityMetadata getAttacker() {
        return this.attacker;
    }
}