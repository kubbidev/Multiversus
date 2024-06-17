package me.kubbidev.multiversus.core.skill.trigger;

import me.kubbidev.multiversus.FBukkitPlugin;
import me.kubbidev.multiversus.core.damage.AttackMetadata;
import me.kubbidev.multiversus.core.damage.EntityMetadata;
import me.kubbidev.multiversus.core.event.attack.EntityAttackEvent;
import me.kubbidev.multiversus.core.skill.Skill;
import me.kubbidev.multiversus.core.skill.SkillMetadata;
import me.kubbidev.multiversus.core.util.EquipmentSlot;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class TriggerMetadata {
    private final LivingEntity caster;
    private final EquipmentSlot actionHand;
    private final Location source;


    @Nullable
    private final Entity target;

    @Nullable
    private final Location targetLocation;

    @Nullable
    private final AttackMetadata attack;

    /**
     * The instantiation of an EntityMetadata can be quite intensive in computation,
     * especially because it can be up to 20 times a second for every entity in the server.
     * <p>
     * It performs a full stat map lookup and caches final stat values.
     * <p>
     * For this reason, it's best to NOT generate the EntityMetadata unless it has been
     * provided beforehand in the constructor, until it's finally asked for in the getter.
     */
    @Nullable
    private EntityMetadata cachedMetadata;

    public TriggerMetadata(LivingEntity caster) {
        this(caster, (LivingEntity) null);
    }

    public TriggerMetadata(LivingEntity caster, @Nullable LivingEntity target) {
        this(caster, EquipmentSlot.MAIN_HAND, caster.getLocation(), target, null, null, null);
    }

    public TriggerMetadata(LivingEntity caster, @Nullable Location targetLocation) {
        this(caster, EquipmentSlot.MAIN_HAND, caster.getLocation(), null, targetLocation, null, null);
    }

    public TriggerMetadata(LivingEntity caster, Location source, @Nullable Location targetLocation) {
        this(caster, EquipmentSlot.MAIN_HAND, source, null, targetLocation, null, null);
    }

    /**
     * The entity responsible for the attack is the one triggering the skill.
     */
    public TriggerMetadata(EntityAttackEvent event) {
        this(event.getAttacker(), event.getEntity(), event.getAttack());
    }

    public TriggerMetadata(EntityMetadata caster, @Nullable Entity target, @Nullable AttackMetadata attack) {
        this(caster.getEntity(), caster.getActionHand(), caster.getEntity().getLocation(), target, null, attack, caster);
    }

    public TriggerMetadata(LivingEntity caster, EquipmentSlot actionHand, Location source, @Nullable Entity target, @Nullable Location targetLocation, @Nullable AttackMetadata attack, @Nullable EntityMetadata cachedMetadata) {
        this.caster = caster;
        this.actionHand = actionHand;
        this.source = source;
        this.target = target;
        this.targetLocation = targetLocation;
        this.attack = attack;
        this.cachedMetadata = cachedMetadata;
    }

    public LivingEntity getCaster() {
        return this.caster;
    }

    public EquipmentSlot getActionHand() {
        return this.actionHand;
    }

    public Location getSource() {
        return this.source;
    }

    public @Nullable Entity getTarget() {
        return this.target;
    }

    public @Nullable Location getTargetLocation() {
        return this.targetLocation;
    }

    public @Nullable AttackMetadata getAttack() {
        return this.attack;
    }

    public EntityMetadata getCachedMetadata(FBukkitPlugin plugin) {
        if (this.cachedMetadata == null) {
            this.cachedMetadata = new EntityMetadata(plugin, this.caster, this.actionHand);
        }
        return this.cachedMetadata;
    }

    public SkillMetadata toSkillMetadata(Skill cast) {
        return new SkillMetadata(cast, getCachedMetadata(cast.getPlugin()), this.source, this.target, this.targetLocation, this.attack);
    }
}