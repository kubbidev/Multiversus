package me.kubbidev.multiversus.core.modifier.skill;

import me.kubbidev.multiversus.core.skill.handler.SkillHandler;

import java.util.Objects;

public class SkillParameterIdentifier {
    private final SkillHandler<?> handler;
    private final String parameter;

    public SkillParameterIdentifier(SkillHandler<?> handler, String parameter) {
        this.handler = handler;
        this.parameter = parameter;
    }

    public SkillHandler<?> getHandler() {
        return this.handler;
    }

    public String getParameter() {
        return this.parameter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SkillParameterIdentifier)) {
            return false;
        }
        SkillParameterIdentifier other = (SkillParameterIdentifier) o;
        return this.handler.equals(other.handler) &&
                this.parameter.equals(other.parameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.handler, this.parameter);
    }
}