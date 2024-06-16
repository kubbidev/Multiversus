package me.kubbidev.multiversus.dependencies.relocation;

import java.util.Objects;

public final class Relocation {
    private static final String RELOCATION_PREFIX = "me.kubbidev.multiversus.lib.";

    public static Relocation of(String id, String pattern) {
        return new Relocation(pattern.replace("{}", "."), RELOCATION_PREFIX + id);
    }

    private final String pattern;
    private final String relocatedPattern;

    private Relocation(String pattern, String relocatedPattern) {
        this.pattern = pattern;
        this.relocatedPattern = relocatedPattern;
    }

    public String getPattern() {
        return this.pattern;
    }

    public String getRelocatedPattern() {
        return this.relocatedPattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Relocation)) {
            return false;
        }
        Relocation other = (Relocation) o;
        return Objects.equals(this.pattern, other.pattern) &&
                Objects.equals(this.relocatedPattern, other.relocatedPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pattern, this.relocatedPattern);
    }
}