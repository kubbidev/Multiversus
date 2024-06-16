package me.kubbidev.multiversus.core.event.attack;

import me.kubbidev.multiversus.core.damage.AttackMetadata;
import me.kubbidev.multiversus.core.event.LivingEntityEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EntityKillEntityEvent extends LivingEntityEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private final LivingEntity target;
    private final AttackMetadata attack;

    public EntityKillEntityEvent(AttackMetadata attack, LivingEntity target) {
        super(Objects.requireNonNull(attack.getAttacker(), "attacker").getEntity());

        this.attack = attack;
        this.target = target;
    }

    public LivingEntity getTarget() {
        return this.target;
    }

    public AttackMetadata getAttack() {
        return this.attack;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}