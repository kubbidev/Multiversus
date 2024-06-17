package me.kubbidev.multiversus.core.skill;

import me.kubbidev.multiversus.FBukkitPlugin;
import me.kubbidev.multiversus.core.skill.handler.SkillHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Can be used to cast a skill handler with configurable modifier input.
 */
public class SimpleSkill extends Skill {
    private final SkillHandler<?> handler;
    private final Map<String, Double> modifiers = new HashMap<>();

    public SimpleSkill(FBukkitPlugin plugin, SkillHandler<?> handler) {
        super(plugin);
        this.handler = handler;
    }

    @Override
    public boolean getResult(SkillMetadata meta) {
        return true;
    }

    @Override
    public void whenCast(SkillMetadata meta) {

    }

    @Override
    public SkillHandler<?> getHandler() {
        return this.handler;
    }

    @Override
    public double getParameter(String path) {
        return this.modifiers.getOrDefault(path, 0d);
    }

    public void registerModifier(String path, double value) {
        this.modifiers.put(path, value);
    }
}