package me.kubbidev.multiversus.command.abstraction;

import me.kubbidev.multiversus.command.spec.CommandSpec;
import me.kubbidev.multiversus.command.util.ArgumentList;
import me.kubbidev.multiversus.command.tabcomplete.CompletionSupplier;
import me.kubbidev.multiversus.command.tabcomplete.TabCompleter;
import me.kubbidev.multiversus.locale.Message;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.sender.Sender;
import me.kubbidev.multiversus.util.Predicates;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public abstract class ParentCommand<T, I> extends Command<Void> {

    /**
     * Child sub commands
     */
    private final List<Command<T>> children;
    /**
     * The type of parent command
     */
    private final Type type;

    public ParentCommand(CommandSpec spec, String name, Type type, List<Command<T>> children) {
        this(spec, name, true, type, children);
    }

    public ParentCommand(CommandSpec spec, String name, boolean console, Type type, List<Command<T>> children) {
        super(spec, name, console, null, Predicates.alwaysFalse());
        this.children = children;
        this.type = type;
    }

    public @NotNull List<Command<T>> getChildren() {
        return this.children;
    }

    @Override
    public void execute(MultiPlugin plugin, Sender sender, Void ignored, ArgumentList args, String label) throws CommandException {
        // check if required argument and/or subcommand is missing
        if (args.size() < this.type.minArgs) {
            sendUsage(sender, label);
            return;
        }

        Command<T> sub = getChildren().stream()
                .filter(s -> s.getName().equalsIgnoreCase(args.get(this.type.cmdIndex)))
                .findFirst()
                .orElse(null);

        if (sub == null) {
            Message.COMMAND_NOT_RECOGNISED.send(sender);
            return;
        }

        // console can't execute the command
        if (!sub.isConsole() && sender.isConsole()) {
            Message.CONSOLE_NOT_ALLOWED_COMMAND.send(sender);
            return;
        }

        if (!sub.isAuthorized(sender)) {
            Message.COMMAND_NO_PERMISSION.send(sender);
            return;
        }

        if (sub.getArgumentCheck().test(args.size() - this.type.minArgs)) {
            sub.sendDetailedUsage(sender, label);
            return;
        }

        String targetArgument = args.get(0);
        I targetId = null;
        if (this.type == Type.TAKES_ARGUMENT_FOR_TARGET) {
            targetId = parseTarget(targetArgument, plugin, sender);
            if (targetId == null) {
                return;
            }
        }

        ReentrantLock lock = getLockForTarget(targetId);
        lock.lock();
        try {
            T target = getTarget(targetId, plugin, sender);
            if (target == null) {
                return;
            }

            try {
                sub.execute(plugin, sender, target, args.subList(this.type.minArgs, args.size()), label);
            } catch (CommandException e) {
                e.handle(sender, label, sub);
            }

            cleanup(target, plugin);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<String> tabComplete(MultiPlugin plugin, Sender sender, ArgumentList args) {
        switch (this.type) {
            case TAKES_ARGUMENT_FOR_TARGET:
                return TabCompleter.create()
                        .at(0, CompletionSupplier.startsWith(() -> getTargets(plugin).stream()))
                        .at(1, CompletionSupplier.startsWith(() -> getChildren().stream()
                                .filter(s -> s.isAuthorized(sender))
                                .map(s -> s.getName().toLowerCase(Locale.ROOT))
                        ))
                        .from(2, partial -> getChildren().stream()
                                .filter(s -> s.isAuthorized(sender))
                                .filter(s -> s.getName().equalsIgnoreCase(args.get(1)))
                                .findFirst()
                                .map(cmd -> cmd.tabComplete(plugin, sender, args.subList(2, args.size())))
                                .orElse(Collections.emptyList())
                        )
                        .complete(args);
            case NO_TARGET_ARGUMENT:
                return TabCompleter.create()
                        .at(0, CompletionSupplier.startsWith(() -> getChildren().stream()
                                .filter(s -> s.isAuthorized(sender))
                                .map(s -> s.getName().toLowerCase(Locale.ROOT))
                        ))
                        .from(1, partial -> getChildren().stream()
                                .filter(s -> s.isAuthorized(sender))
                                .filter(s -> s.getName().equalsIgnoreCase(args.get(0)))
                                .findFirst()
                                .map(cmd -> cmd.tabComplete(plugin, sender, args.subList(1, args.size())))
                                .orElse(Collections.emptyList())
                        )
                        .complete(args);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void sendUsage(Sender sender, String label) {
        List<Command<?>> subs = getChildren().stream()
                .filter(s -> s.isAuthorized(sender))
                .collect(Collectors.toList());

        if (!subs.isEmpty()) {
            Message.MAIN_COMMAND_USAGE_HEADER.send(sender, getName(), String.format(getUsage(), label));
            for (Command<?> s : subs) {
                s.sendUsage(sender, label);
            }
        } else {
            Message.COMMAND_NO_PERMISSION.send(sender);
        }
    }

    @Override
    public void sendDetailedUsage(Sender sender, String label) {
        sendUsage(sender, label);
    }

    @Override
    public boolean isAuthorized(Sender sender) {
        return getChildren().stream().anyMatch(sc -> sc.isAuthorized(sender));
    }

    protected abstract List<String> getTargets(MultiPlugin plugin);

    protected abstract I parseTarget(String target, MultiPlugin plugin, Sender sender);

    protected abstract ReentrantLock getLockForTarget(I target);

    protected abstract T getTarget(I target, MultiPlugin plugin, Sender sender);

    protected abstract void cleanup(T t, MultiPlugin plugin);

    public enum Type {
        // e.g. /multiversus log sub-command....
        NO_TARGET_ARGUMENT(0),
        // e.g. /multiversus user <USER> sub-command....
        TAKES_ARGUMENT_FOR_TARGET(1);

        private final int cmdIndex;
        private final int minArgs;

        Type(int cmdIndex) {
            this.cmdIndex = cmdIndex;
            this.minArgs = cmdIndex + 1;
        }
    }
}