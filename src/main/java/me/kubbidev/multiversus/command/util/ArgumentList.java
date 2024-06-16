package me.kubbidev.multiversus.command.util;

import com.google.common.collect.ForwardingList;
import me.kubbidev.multiversus.commands.user.UserParentCommand;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.sender.Sender;
import me.kubbidev.multiversus.util.DurationParser;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * A list of {@link String} arguments, with extra methods to help
 * with parsing.
 */
public class ArgumentList extends ForwardingList<String> {

    private final List<String> backingList;

    public ArgumentList(List<String> backingList) {
        this.backingList = backingList;
    }

    @Override
    protected @NotNull List<String> delegate() {
        return this.backingList;
    }

    public boolean indexOutOfBounds(int index) {
        return index < 0 || index >= size();
    }

    @Override
    public String get(int index) throws IndexOutOfBoundsException {
        return super.get(index).replace("{SPACE}", " ");
    }

    public String getOrDefault(int index, String defaultValue) {
        if (indexOutOfBounds(index)) {
            return defaultValue;
        }
        return get(index);
    }

    @Override
    public @NotNull ArgumentList subList(int fromIndex, int toIndex) {
        return new ArgumentList(super.subList(fromIndex, toIndex));
    }

    public int getIntOrDefault(int index, int defaultValue) {
        if (indexOutOfBounds(index)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(get(index));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String getLowercase(int index, Predicate<? super String> test) throws ArgumentException.DetailedUsage {
        String arg = get(index).toLowerCase(Locale.ROOT);
        if (!test.test(arg)) {
            throw new ArgumentException.DetailedUsage();
        }
        return arg;
    }

    public boolean getBooleanOrInsert(int index, boolean defaultValue) {
        if (!indexOutOfBounds(index)) {
            String arg = get(index);
            if (arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("false")) {
                return Boolean.parseBoolean(arg);
            }
        }

        add(index, Boolean.toString(defaultValue));
        return defaultValue;
    }

    public UUID getUserTarget(int index, MultiPlugin plugin, Sender sender) {
        String arg = get(index);
        return UserParentCommand.parseTargetUniqueId(arg, plugin, sender);
    }

    public Duration getDuration(int index) throws ArgumentException {
        String arg = get(index);
        return parseDuration(arg).orElseThrow(() -> new ArgumentException.InvalidDate(arg));
    }

    public Duration getDurationOrDefault(int index, Duration defaultValue) throws ArgumentException {
        if (indexOutOfBounds(index)) {
            return defaultValue;
        }

        return parseDuration(get(index)).orElse(defaultValue);
    }

    private static Optional<Duration> parseDuration(String input) throws ArgumentException.PastDate {
        try {
            long number = Long.parseLong(input);
            Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
            Duration duration = checkPastDate(Duration.between(now, Instant.ofEpochSecond(number)));
            return Optional.of(duration);
        } catch (NumberFormatException e) {
            // ignore
        }

        try {
            Duration duration = checkPastDate(DurationParser.parseDuration(input));
            return Optional.of(duration);
        } catch (IllegalArgumentException e) {
            // ignore
        }

        return Optional.empty();
    }

    private static Duration checkPastDate(Duration duration) throws ArgumentException.PastDate {
        if (duration.isNegative()) {
            throw new ArgumentException.PastDate();
        }
        return duration;
    }
}