package me.kubbidev.multiversus.command.abstraction;

import me.kubbidev.multiversus.command.access.CommandPermission;
import me.kubbidev.multiversus.command.spec.Argument;
import me.kubbidev.multiversus.command.spec.CommandSpec;
import me.kubbidev.multiversus.command.util.ArgumentList;
import me.kubbidev.multiversus.locale.Message;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.sender.Sender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents a single "main" command (one without any children)
 */
public abstract class SingleCommand extends Command<Void> {

    public SingleCommand(CommandSpec spec, String name, CommandPermission permission, Predicate<Integer> argumentCheck) {
        super(spec, name, permission, argumentCheck);
    }

    public SingleCommand(CommandSpec spec, String name, boolean console, CommandPermission permission, Predicate<Integer> argumentCheck) {
        super(spec, name, console, permission, argumentCheck);
    }

    @Override
    public final void execute(MultiPlugin plugin, Sender sender, Void ignored, ArgumentList args, String label) throws CommandException {
        execute(plugin, sender, args, label);
    }

    public abstract void execute(MultiPlugin plugin, Sender sender, ArgumentList args, String label) throws CommandException;

    @Override
    public void sendUsage(Sender sender, String label) {
        TextComponent.Builder builder = Component.text()
                .append(Component.text('>', NamedTextColor.DARK_AQUA))
                .append(Component.space())
                .append(Component.text(getName().toLowerCase(Locale.ROOT), NamedTextColor.GREEN));

        if (getArgs().isPresent()) {
            List<Component> argUsages = getArgs().get().stream()
                    .map(Argument::asPrettyString)
                    .collect(Collectors.toList());

            builder.append(Component.text(" - ", NamedTextColor.DARK_AQUA))
                    .append(Component.join(JoinConfiguration.spaces(), argUsages));
        }

        sender.sendMessage(builder.build());
    }

    @Override
    public void sendDetailedUsage(Sender sender, String label) {
        Message.COMMAND_USAGE_DETAILED_HEADER.send(sender, getName(), getDescription());
        if (getArgs().isPresent()) {
            Message.COMMAND_USAGE_DETAILED_ARGS_HEADER.send(sender);
            for (Argument arg : getArgs().get()) {
                Message.COMMAND_USAGE_DETAILED_ARG.send(sender, arg.asPrettyString(), arg.getDescription());
            }
        }
    }
}