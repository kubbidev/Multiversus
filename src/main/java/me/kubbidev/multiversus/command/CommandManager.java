package me.kubbidev.multiversus.command;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.kubbidev.multiversus.command.abstraction.Command;
import me.kubbidev.multiversus.command.abstraction.CommandException;
import me.kubbidev.multiversus.command.tabcomplete.CompletionSupplier;
import me.kubbidev.multiversus.command.tabcomplete.TabCompleter;
import me.kubbidev.multiversus.command.tabcomplete.TabCompletions;
import me.kubbidev.multiversus.command.util.ArgumentList;
import me.kubbidev.multiversus.commands.misc.*;
import me.kubbidev.multiversus.commands.user.UserParentCommand;
import me.kubbidev.multiversus.config.ConfigKeys;
import me.kubbidev.multiversus.locale.Message;
import me.kubbidev.multiversus.plugin.AbstractMultiPlugin;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.plugin.scheduler.SchedulerAdapter;
import me.kubbidev.multiversus.plugin.scheduler.SchedulerTask;
import me.kubbidev.multiversus.sender.Sender;
import me.kubbidev.multiversus.util.ExpiringSet;
import me.kubbidev.multiversus.util.ImmutableCollectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Root command manager for the '/multiversus' command.
 */
public class CommandManager {

    private final MultiPlugin plugin;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("multiversus-command-executor")
            .build()
    );
    private final AtomicBoolean executingCommand = new AtomicBoolean(false);
    private final ExpiringSet<UUID> playerRateLimit = new ExpiringSet<>(500, TimeUnit.MILLISECONDS);
    private final TabCompletions tabCompletions;
    private final Map<String, Command<?>> mainCommands;

    public CommandManager(MultiPlugin plugin) {
        this.plugin = plugin;
        this.tabCompletions = new TabCompletions(plugin);
        this.mainCommands = ImmutableList.<Command<?>>builder()
                .add(new UserParentCommand())
                .addAll(plugin.getExtraCommands())
                .add(new SyncCommand())
                .add(new InfoCommand())
                .add(new ImportCommand())
                .add(new ExportCommand())
                .add(new ReloadConfigCommand())
                .add(new TranslationsCommand())
                .build()
                .stream()
                .collect(ImmutableCollectors.toMap(c -> c.getName().toLowerCase(Locale.ROOT), Function.identity()));
    }

    public MultiPlugin getPlugin() {
        return this.plugin;
    }

    public TabCompletions getTabCompletions() {
        return this.tabCompletions;
    }

    @VisibleForTesting
    public Map<String, Command<?>> getMainCommands() {
        return this.mainCommands;
    }

    public CompletableFuture<Void> executeCommand(Sender sender, String label, List<String> args) {
        UUID uniqueId = sender.getUniqueId();
        if (this.plugin.getConfiguration().get(ConfigKeys.COMMANDS_RATE_LIMIT) && !sender.isConsole() && !this.playerRateLimit.add(uniqueId)) {
            this.plugin.getLogger().warn("Player '" + uniqueId + "' is spamming Multiversus commands. Ignoring further inputs.");
            return CompletableFuture.completedFuture(null);
        }

        SchedulerAdapter scheduler = this.plugin.getBootstrap().getScheduler();
        List<String> argsCopy = new ArrayList<>(args);

        // if the executingCommand flag is set, there is another command executing at the moment
        if (this.executingCommand.get()) {
            Message.ALREADY_EXECUTING_COMMAND.send(sender);
        }

        // a reference to the thread being used to execute the command
        AtomicReference<Thread> executorThread = new AtomicReference<>();
        // a reference to the timeout task scheduled to catch if this command takes too long to execute
        AtomicReference<SchedulerTask> timeoutTask = new AtomicReference<>();

        // schedule the actual execution of the command using the command executor service
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            // set flags
            executorThread.set(Thread.currentThread());
            this.executingCommand.set(true);

            // actually try to execute the command
            try {
                execute(sender, label, args);
            } catch (Throwable e) {
                // catch any exception
                this.plugin.getLogger().severe("Exception whilst executing command: " + args, e);
            } finally {
                // unset flags
                this.executingCommand.set(false);
                executorThread.set(null);

                // cancel the timeout task
                SchedulerTask timeout;
                if ((timeout = timeoutTask.get()) != null) {
                    timeout.cancel();
                }
            }
        }, this.executor);

        // schedule another task to catch if the command doesn't complete after 10 seconds
        timeoutTask.set(scheduler.asyncLater(() -> {
            if (!future.isDone()) {
                handleCommandTimeout(executorThread, argsCopy);
            }
        }, 10, TimeUnit.SECONDS));

        return future;
    }

    private void handleCommandTimeout(AtomicReference<Thread> thread, List<String> args) {
        Thread executorThread = thread.get();
        if (executorThread == null) {
            this.plugin.getLogger().warn("Command execution " + args + " has not completed - is another command execution blocking it?");
        } else {
            String stackTrace = Arrays.stream(executorThread.getStackTrace())
                    .map(el -> "  " + el.toString())
                    .collect(Collectors.joining("\n"));
            this.plugin.getLogger().warn("Command execution " + args + " has not completed. Trace: \n" + stackTrace);
        }
    }

    public boolean hasPermissionForAny(Sender sender) {
        return this.mainCommands.values().stream().anyMatch(c -> c.shouldDisplay() && c.isAuthorized(sender));
    }

    private void execute(Sender sender, String label, List<String> arguments) {
        applyConvenienceAliases(arguments, true);

        // Handle no arguments
        if (arguments.isEmpty() || arguments.size() == 1 && arguments.get(0).trim().isEmpty()) {
            sender.sendMessage(Message.prefixed(Component.text()
                    .color(NamedTextColor.DARK_GREEN)
                    .append(Component.text("Running "))
                    .append(Component.text(AbstractMultiPlugin.getPluginName(), NamedTextColor.AQUA))
                    .append(Component.space())
                    .append(Component.text("v" + this.plugin.getBootstrap().getVersion(), NamedTextColor.AQUA))
                    .append(Message.FULL_STOP)
            ));

            if (hasPermissionForAny(sender)) {
                Message.VIEW_AVAILABLE_COMMANDS_PROMPT.send(sender, label);
                return;
            }

            Message.NO_PERMISSION_FOR_SUBCOMMANDS.send(sender);
            return;
        }

        // Look for the main command.
        Command<?> main = this.mainCommands.get(arguments.get(0).toLowerCase(Locale.ROOT));

        // Main command not found
        if (main == null) {
            sendCommandUsage(sender, label);
            return;
        }

        // console can't execute the command
        if (!main.isConsole() && sender.isConsole()) {
            Message.CONSOLE_NOT_ALLOWED_COMMAND.send(sender);
            return;
        }

        // Check the Sender has permission to use the main command.
        if (!main.isAuthorized(sender)) {
            sendCommandUsage(sender, label);
            return;
        }

        arguments.remove(0); // remove the main command arg.

        // Check the correct number of args were given for the main command
        if (main.getArgumentCheck().test(arguments.size())) {
            main.sendDetailedUsage(sender, label);
            return;
        }

        // Try to execute the command.
        try {
            main.execute(this.plugin, sender, null, new ArgumentList(arguments), label);
        } catch (CommandException e) {
            e.handle(sender, label, main);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public List<String> tabCompleteCommand(Sender sender, List<String> arguments) {
        applyConvenienceAliases(arguments, false);

        List<Command<?>> mains = this.mainCommands.values().stream()
                .filter(Command::shouldDisplay)
                .filter(m -> m.isAuthorized(sender))
                .collect(Collectors.toList());

        return TabCompleter.create()
                .at(0, CompletionSupplier.startsWith(() -> mains.stream().map(c -> c.getName().toLowerCase(Locale.ROOT))))
                .from(1, partial -> mains.stream()
                        .filter(m -> m.getName().equalsIgnoreCase(arguments.get(0)))
                        .findFirst()
                        .map(cmd -> cmd.tabComplete(this.plugin, sender, new ArgumentList(arguments.subList(1, arguments.size()))))
                        .orElse(Collections.emptyList())
                )
                .complete(arguments);
    }

    private void sendCommandUsage(Sender sender, String label) {
        sender.sendMessage(Message.prefixed(Component.text()
                .color(NamedTextColor.DARK_GREEN)
                .append(Component.text("Running "))
                .append(Component.text(AbstractMultiPlugin.getPluginName(), NamedTextColor.AQUA))
                .append(Component.space())
                .append(Component.text("v" + this.plugin.getBootstrap().getVersion(), NamedTextColor.AQUA))
                .append(Message.FULL_STOP)
        ));

        this.mainCommands.values().stream()
                .filter(Command::shouldDisplay)
                .filter(c -> c.isAuthorized(sender))
                .forEach(c -> sender.sendMessage(Component.text()
                        .append(Component.text('>', NamedTextColor.DARK_AQUA))
                        .append(Component.space())
                        .append(Component.text(String.format(c.getUsage(), label), NamedTextColor.GREEN))
                        .clickEvent(ClickEvent.suggestCommand(String.format(c.getUsage(), label)))
                        .build()
                ));
    }

    /**
     * Applies "convenience" aliases to the given cmd line arguments.
     *
     * @param args the current args list
     * @param rewriteLastArgument if the last argument should be rewritten -
     *                            this is false when the method is called on tab completions
     */
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private static void applyConvenienceAliases(List<String> args, boolean rewriteLastArgument) {
        // '/m u' --> '/m user' etc
        //     ^          ^^^^
        if (!args.isEmpty() && (rewriteLastArgument || args.size() >= 2)) {
            replaceArgs(args, 0, arg -> {
                switch (arg) {
                    case "u": return "user";
                    case "i": return "info";
                    default: return null;
                }
            });
        }

        // '/m user kubbidev i set --> /m user kubbidev info set' etc
        //                   ^                          ^^^^
        if (args.size() >= 3 && (rewriteLastArgument || args.size() >= 4)) {
            String arg0 = args.get(0).toLowerCase(Locale.ROOT);
            if (arg0.equals("user")) {
                replaceArgs(args, 2, arg -> {
                    switch (arg) {
                        case "i": return "info";
                        default: return null;
                    }
                });
            }
        }
    }

    private static void replaceArgs(List<String> args, int i, Function<String, String> rewrites) {
        String arg = args.get(i).toLowerCase(Locale.ROOT);
        String rewrite = rewrites.apply(arg);
        if (rewrite != null) {
            args.remove(i);
            args.add(i, rewrite);
        }
    }
}