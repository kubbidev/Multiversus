package me.kubbidev.multiversus.storage.misc;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class DataConstraints {
    private DataConstraints() {}

    public static final int MAX_PLAYER_USERNAME_LENGTH = 16;
    public static final Pattern PLAYER_USERNAME_INVALID_CHAR_MATCHER = Pattern.compile("[^A-Za-z0-9_]");

    public static final Predicate<String> PLAYER_USERNAME_TEST = s -> !s.isEmpty() && s.length() <= MAX_PLAYER_USERNAME_LENGTH && !PLAYER_USERNAME_INVALID_CHAR_MATCHER.matcher(s).find();
    public static final Predicate<String> PLAYER_USERNAME_TEST_LENIENT = s -> !s.isEmpty() && s.length() <= MAX_PLAYER_USERNAME_LENGTH;

}