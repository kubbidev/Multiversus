package me.kubbidev.multiversus.core.modifier.skill;

import me.kubbidev.multiversus.core.skill.Skill;
import me.kubbidev.multiversus.core.skill.handler.SkillHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SkillModifierMap {
    /**
     * This map enables to calculate the skill buffs associated to a particular skill and a particular {@link SkillModifier}
     * without having to parse other modifiers.
     * <p>
     * In particular this is done every time a skill is cast.
     */
    private final Map<SkillParameterIdentifier, SkillModifierInstance> instances = new HashMap<>();

    public double calculateValue(Skill cast, String parameter) {
        return getInstance(cast.getHandler(), parameter).getTotal(cast.getParameter(parameter));
    }

    /**
     * @return The {@link SkillModifierInstance}s that have been manipulated so far since the
     * entity has spawn.
     * <p>
     * {@link SkillModifierInstance}s are completely flushed when the server restarts.
     */
    public Collection<SkillModifierInstance> getInstances() {
        return this.instances.values();
    }

    public SkillModifierInstance getInstance(SkillHandler<?> handler, String skill) {
        SkillParameterIdentifier identifier = new SkillParameterIdentifier(handler, skill);
        // return the modifier instance of the skill handler or new one if not found.
        return this.instances.computeIfAbsent(identifier, id -> new SkillModifierInstance(id.getHandler(), id.getParameter()));
    }
}