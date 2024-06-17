package me.kubbidev.multiversus.core.modifier.skill;

import me.kubbidev.multiversus.core.metadata.MetadataProvider;
import me.kubbidev.multiversus.core.modifier.ModifierSource;
import me.kubbidev.multiversus.core.modifier.ModifierType;
import me.kubbidev.multiversus.core.modifier.instance.InstanceModifier;
import me.kubbidev.multiversus.core.skill.handler.SkillHandler;
import me.kubbidev.multiversus.core.util.EquipmentSlot;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A skill "modifier" modifies a specific parameter of a skill, in the same way that
 * a stat modifier modifies a stat for a player.
 * <p>
 * It can also be given a boolean formula, which determines which
 * skills the modifier will apply onto.
 */
public class SkillModifier extends InstanceModifier {

    /**
     * The list of all the skills this modifier will be applied to.
     * <p>
     * A skill modifier can target one skill or a set of skills like
     * giving for example +10% damage to all the passive skills.
     */
    private final List<SkillHandler<?>> skills;
    private final String parameter;

    public SkillModifier(String key, double value, List<SkillHandler<?>> skills, String parameter) {
        this(ModifierSource.OTHER, EquipmentSlot.OTHER, key, value, ModifierType.FLAT, skills, parameter);
    }

    public SkillModifier(String key, double value, ModifierType type, List<SkillHandler<?>> skills, String parameter) {
        this(ModifierSource.OTHER, EquipmentSlot.OTHER, key, value, type, skills, parameter);
    }

    public SkillModifier(ModifierSource source, EquipmentSlot slot, String key, double value, ModifierType type, List<SkillHandler<?>> skills, String parameter) {
        super(source, slot, key, value, type);
        this.skills = skills;
        this.parameter = parameter;
    }

    public SkillModifier(UUID uniqueId, ModifierSource source, EquipmentSlot slot, String key, double value, ModifierType type, List<SkillHandler<?>> skills, String parameter) {
        super(uniqueId, source, slot, key, value, type);
        this.skills = skills;
        this.parameter = parameter;
    }

    public List<SkillHandler<?>> getSkills() {
        return this.skills;
    }

    public String getParameter() {
        return this.parameter;
    }

    /**
     * Used to add a constant to some existing stat modifier, usually an
     * integer, for instance it is used when a skill buff trigger is triggered multiple times.
     *
     * @param offset The offset added.
     * @return A new instance of {@link SkillModifier} with modified value
     */
    public SkillModifier add(double offset) {
        return new SkillModifier(
                getUniqueId(),
                getSource(),
                getSlot(),
                getKey(), getValue() + offset,
                getType(), new ArrayList<>(this.skills), this.parameter);
    }

    @Override
    public void register(LivingEntity entity) {
        SkillModifierMap modifierMap = MetadataProvider.getModifierMap(entity);
        for (SkillHandler<?> handler : this.skills) {
            modifierMap.getInstance(handler, this.parameter).registerModifier(this);
        }
    }

    @Override
    public void unregister(LivingEntity entity) {
        SkillModifierMap modifierMap = MetadataProvider.getModifierMap(entity);
        for (SkillHandler<?> handler : this.skills) {
            modifierMap.getInstance(handler, this.parameter).removeModifier(getUniqueId());
        }
    }
}