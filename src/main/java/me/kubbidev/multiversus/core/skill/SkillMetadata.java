package me.kubbidev.multiversus.core.skill;

import me.kubbidev.multiversus.FBukkitPlugin;
import me.kubbidev.multiversus.core.damage.AttackMetadata;
import me.kubbidev.multiversus.core.damage.EntityMetadata;
import me.kubbidev.multiversus.core.metadata.MetadataProvider;
import me.kubbidev.multiversus.core.util.EntityBody;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SkillMetadata {
    private final Skill cast;

    /**
     * Entity by which the skill was cast.
     */
    private final EntityMetadata caster;

    /**
     * Location at which the skill was cast.
     */
    private final Location source;

    /**
     * Some skills like projectiles or ray casts cache a target
     * entity which is later used in targeters.
     */
    @Nullable
    private final Entity targetEntity;

    /**
     * Some skills like ray casts cache a target location which is later
     * used in targeters.
     */
    @Nullable
    private final Location targetLocation;

    /**
     * The attack source if the skill was trigger from an attack, otherwise null.
     */
    @Nullable
    private final AttackMetadata attackSource;

    public SkillMetadata(Skill cast, EntityMetadata caster, Location source, @Nullable Entity targetEntity, @Nullable Location targetLocation, @Nullable AttackMetadata attackSource) {
        this.cast = cast;
        this.caster = caster;
        this.source = source;
        this.targetEntity = targetEntity;
        this.targetLocation = targetLocation;
        this.attackSource = attackSource;
    }

    public Skill getCast() {
        return this.cast;
    }

    public EntityMetadata getCaster() {
        return this.caster;
    }

    public LivingEntity getEntity() {
        return this.caster.getEntity();
    }

    public FBukkitPlugin getPlugin() {
        return this.caster.getPlugin();
    }

    public Location getSource() {
        return this.source.clone();
    }

    public boolean hasAttackSource() {
        return this.attackSource != null;
    }

    /**
     * @return The attack which triggered the skill.
     */
    public AttackMetadata getAttackSource() {
        return Objects.requireNonNull(this.attackSource, "Skill was not triggered by any attack");
    }

    /**
     * Retrieves a specific skill parameter value.
     * <p>
     * This applies to the original skill being cast.
     *
     * @param parameter Skill parameter name
     * @return Skill parameter final value, taking into account skill mods
     */
    public double getParameter(String parameter) {
        return MetadataProvider.getModifierMap(getEntity()).calculateValue(this.cast, parameter);
    }

    public Entity getTargetEntity() {
        return Objects.requireNonNull(this.targetEntity, "Skill has no target entity");
    }

    public @Nullable Entity getTargetEntityOrNull() {
        return this.targetEntity;
    }

    public boolean hasTargetEntity() {
        return this.targetEntity != null;
    }

    public Location getTargetLocation() {
        return Objects.requireNonNull(this.targetLocation, "Skill has no target location").clone();
    }

    public @Nullable Location getTargetLocationOrNull() {
        return this.targetLocation == null ? null : this.targetLocation.clone();
    }

    public boolean hasTargetLocation() {
        return this.targetLocation != null;
    }

    /**
     * Analog of {@link #getSkillEntity(boolean)}.
     * <p>
     * Used when a skill requires a location when no target is provided.
     *
     * @param sourceLocation If the source location should be prioritized.
     * @return Target location (and if it exists) OR location of target entity (and if it exists), source location otherwise
     */
    public Location getSkillLocation(boolean sourceLocation) {
        return sourceLocation ? this.source.clone() : this.targetLocation != null ? this.targetLocation.clone() : this.targetEntity != null ? EntityBody.BODY.getLocation(this.targetEntity) : this.source.clone();
    }

    /**
     * Analog of {@link #getSkillLocation(boolean)}.
     * <p>
     * Used when a skill requires an entity when no target is provided.
     *
     * @param caster If the skill caster should be prioritized.
     * @return Target entity if prioritized (and if it exists), skill caster otherwise
     */
    public Entity getSkillEntity(boolean caster) {
        return caster || this.targetEntity == null ? this.caster.getEntity() : targetEntity;
    }

    /**
     * Keeps the same skill caster.
     * <p>
     * Used when casting sub-skills with different targets.
     * <p>
     * This has the effect of keeping every skill data, put aside targets.
     * <p>
     * Data that is kept on cloning:
     * <br>- skill being cast
     * <br>- skill caster
     * <br>- attack source
     * <p>
     * Data being replaced on cloning:
     * <br>- source location
     * <br>- target entity
     * <br>- target location
     *
     * @return New skill metadata for other sub-skills
     */
    public SkillMetadata clone(Location source, @Nullable Entity targetEntity, @Nullable Location targetLocation) {
        return new SkillMetadata(this.cast, this.caster, source, targetEntity, targetLocation, this.attackSource);
    }

    public SkillMetadata clone(Location targetLocation) {
        return clone(this.source, this.targetEntity, targetLocation);
    }
}