package me.kubbidev.multiversus.util;

import com.google.common.collect.Range;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Predicate;

/**
 * A collection of predicate utilities used mostly in command classes
 */
public final class Predicates {
    private Predicates() {
    }

    @SuppressWarnings("rawtypes")
    private static final Predicate FALSE = new Predicate() {
        @Override
        public boolean test(Object o) {
            return false;
        }

        @Override
        public @NotNull Predicate and(@NotNull Predicate other) {
            return this;
        }

        @Override
        public @NotNull Predicate or(@NotNull Predicate other) {
            return other;
        }

        @Override
        public @NotNull Predicate negate() {
            return TRUE;
        }
    };
    @SuppressWarnings("rawtypes")
    private static final Predicate TRUE = new Predicate() {
        @Override
        public boolean test(Object o) {
            return true;
        }

        @Override
        public @NotNull Predicate and(@NotNull Predicate other) {
            return other;
        }

        @Override
        public @NotNull Predicate or(@NotNull Predicate other) {
            return this;
        }

        @Override
        public @NotNull Predicate negate() {
            return FALSE;
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> alwaysFalse() {
        return FALSE;
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> alwaysTrue() {
        return TRUE;
    }

    public static Predicate<Integer> notInRange(int start, int end) {
        Range<Integer> range = Range.closed(start, end);
        return value -> !range.contains(value);
    }

    public static Predicate<Integer> inRange(int start, int end) {
        Range<Integer> range = Range.closed(start, end);
        return range::contains;
    }

    public static <T> Predicate<T> not(T t) {
        return obj -> !t.equals(obj);
    }

    public static <T> Predicate<T> is(T t) {
        return t::equals;
    }

    public static Predicate<String> startsWithIgnoreCase(String prefix) {
        return string -> {
            if (string.length() < prefix.length()) {
                return false;
            }
            return string.regionMatches(true, 0, prefix, 0, prefix.length());
        };
    }

    public static Predicate<String> containsIgnoreCase(String substring) {
        return string -> {
            if (string.length() < substring.length()) {
                return false;
            }
            return string.toLowerCase(Locale.ROOT).contains(substring.toLowerCase(Locale.ROOT));
        };
    }

}