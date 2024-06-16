package me.kubbidev.multiversus.util;

import me.kubbidev.multiversus.locale.TranslationManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Formats durations to a readable form
 */
public class DurationFormatter {
    public static final DurationFormatter LONG = new DurationFormatter(false);
    public static final DurationFormatter CONCISE = new DurationFormatter(true);
    public static final DurationFormatter CONCISE_LOW_ACCURACY = new DurationFormatter(true, 3);

    private static final ChronoUnit[] UNITS = new ChronoUnit[]{
            ChronoUnit.YEARS,
            ChronoUnit.MONTHS,
            ChronoUnit.WEEKS,
            ChronoUnit.DAYS,
            ChronoUnit.HOURS,
            ChronoUnit.MINUTES,
            ChronoUnit.SECONDS
    };

    private final boolean concise;
    private final int accuracy;

    public DurationFormatter(boolean concise) {
        this(concise, Integer.MAX_VALUE);
    }

    public DurationFormatter(boolean concise, int accuracy) {
        this.concise = concise;
        this.accuracy = accuracy;
    }

    /**
     * Formats {@code duration} as a string.
     *
     * @param duration the duration
     * @return the formatted string
     */
    public String formatString(Duration duration) {
        return PlainTextComponentSerializer.plainText().serialize(TranslationManager.render(format(duration)));
    }

    /**
     * Formats {@code duration} as a {@link Component}.
     *
     * @param duration the duration
     * @return the formatted component
     */
    public Component format(Duration duration) {
        long seconds = duration.getSeconds();
        TextComponent.Builder builder = Component.text();
        int outputSize = 0;

        for (ChronoUnit unit : UNITS) {
            long n = seconds / unit.getDuration().getSeconds();
            if (n > 0) {
                seconds -= unit.getDuration().getSeconds() * n;
                if (outputSize != 0) {
                    builder.append(Component.space());
                }
                builder.append(formatPart(n, unit));
                outputSize++;
            }
            if (seconds <= 0 || outputSize >= this.accuracy) {
                break;
            }
        }

        if (outputSize == 0) {
            return formatPart(0, ChronoUnit.SECONDS);
        }
        return builder.build();
    }

    // Translation keys are in the format:
    //   multiversus.duration.unit.years.plural={0} years
    //   multiversus.duration.unit.years.singular={0} year
    //   multiversus.duration.unit.years.short={0}y
    // ... and so on

    private TranslatableComponent formatPart(long amount, ChronoUnit unit) {
        String format = this.concise ? "short" : amount == 1 ? "singular" : "plural";
        String translationKey = "multiversus.duration.unit." + unit.name().toLowerCase(Locale.ROOT) + "." + format;
        return Component.translatable(translationKey, Component.text(amount));
    }

}