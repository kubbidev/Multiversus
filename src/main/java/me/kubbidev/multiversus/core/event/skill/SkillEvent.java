package me.kubbidev.multiversus.core.event.skill;

import me.kubbidev.multiversus.core.event.LivingEntityEvent;
import me.kubbidev.multiversus.core.skill.Skill;
import me.kubbidev.multiversus.core.skill.SkillMetadata;
import me.kubbidev.multiversus.core.skill.result.SkillResult;

public abstract class SkillEvent extends LivingEntityEvent {
    private final SkillMetadata skillMeta;
    private final SkillResult result;

    public SkillEvent(SkillMetadata skillMeta, SkillResult result) {
        super(skillMeta.getEntity());
        this.skillMeta = skillMeta;
        this.result = result;
    }

    public SkillMetadata getSkillMeta() {
        return this.skillMeta;
    }

    public SkillResult getResult() {
        return this.result;
    }

    public Skill getSkill() {
        return this.skillMeta.getCast();
    }
}