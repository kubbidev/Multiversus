package me.kubbidev.multiversus.core.skill.result.def;

import me.kubbidev.multiversus.core.skill.result.SkillResult;

public class SimpleSkillResult implements SkillResult {
    private final boolean success;

    public SimpleSkillResult(boolean success) {
        this.success = success;
    }

    public SimpleSkillResult() {
        this(true);
    }

    @Override
    public boolean isSuccessful() {
        return this.success;
    }
}