package me.kubbidev.multiversus.core.event.attack.fake;

import com.google.common.base.Function;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;

@SuppressWarnings("removal")
public abstract class FakeEntityDamageByEntityEvent extends EntityDamageByEntityEvent {

    public FakeEntityDamageByEntityEvent(Entity damager, Entity victim, EntityDamageEvent.DamageCause cause, double damage) {
        super(damager, victim, cause, damage);
    }

    @SuppressWarnings("deprecation")
    public FakeEntityDamageByEntityEvent(Entity damager, Entity victim, EntityDamageEvent.DamageCause cause, Map<DamageModifier, Double> modifiers, Map<DamageModifier, ? extends Function<? super Double, Double>> modifierFunctions) {
        super(damager, victim, cause, modifiers, modifierFunctions);
    }
}