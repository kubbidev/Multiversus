package me.kubbidev.multiversus.core.modifier.skill;

import me.kubbidev.multiversus.core.modifier.instance.ModifiedInstance;
import me.kubbidev.multiversus.core.skill.handler.SkillHandler;

public class SkillModifierInstance extends ModifiedInstance<SkillModifier> {
    private final SkillHandler<?> handler;
    private final String parameter;

    public SkillModifierInstance(SkillHandler<?> handler, String parameter) {
        this.handler = handler;
        this.parameter = parameter;
    }

    public SkillHandler<?> getHandler() {
        return this.handler;
    }

    public String getParameter() {
        return this.parameter;
    }
}