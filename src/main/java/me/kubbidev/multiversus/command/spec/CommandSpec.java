package me.kubbidev.multiversus.command.spec;

import me.kubbidev.multiversus.util.ImmutableCollectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * An enumeration of the command defintion/usage messages used in the plugin.
 */
@SuppressWarnings("SpellCheckingInspection")
public enum CommandSpec {

    USER("/%s user <user>"),

    SYNC("/%s sync"),
    INFO("/%s info"),
    IMPORT("/%s import <file>",
            arg("file", true)
    ),
    EXPORT("/%s export <file>",
            arg("file", true),
            arg("without-users", "--without-users", false)
    ),
    RELOAD_CONFIG("/%s reloadconfig"),
    TRANSLATIONS("/%s translations"),

    USER_INFO;

    private final String usage;
    private final List<Argument> args;

    CommandSpec(String usage, PartialArgument... args) {
        this.usage = usage;
        this.args = args.length == 0 ? null : Arrays.stream(args)
                .map(builder -> {
                    String key = builder.id.replace(".", "").replace(' ', '-');
                    TranslatableComponent description = Component.translatable("multiversus.usage." + key() + ".argument." + key);
                    return new Argument(builder.name, builder.required, description);
                })
                .collect(ImmutableCollectors.toList());
    }

    CommandSpec(PartialArgument... args) {
        this(null, args);
    }

    public TranslatableComponent description() {
        return Component.translatable("multiversus.usage." + this.key() + ".description");
    }

    public String usage() {
        return this.usage;
    }

    public List<Argument> args() {
        return this.args;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    private static PartialArgument arg(String id, String name, boolean required) {
        return new PartialArgument(id, name, required);
    }

    private static PartialArgument arg(String name, boolean required) {
        return new PartialArgument(name, name, required);
    }

    private static final class PartialArgument {
        private final String id;
        private final String name;
        private final boolean required;

        private PartialArgument(String id, String name, boolean required) {
            this.id = id;
            this.name = name;
            this.required = required;
        }
    }

}