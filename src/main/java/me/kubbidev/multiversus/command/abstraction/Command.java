package me.kubbidev.multiversus.command.abstraction;

import me.kubbidev.multiversus.command.access.CommandPermission;
import me.kubbidev.multiversus.command.spec.Argument;
import me.kubbidev.multiversus.command.spec.CommandSpec;
import me.kubbidev.multiversus.command.util.ArgumentList;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.sender.Sender;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * An abstract command class
 *
 * @param <T> the argument type required by the command
 */
public abstract class Command<T> {

    /**
     * The commands specification.
     * <p>
     * Contains details about usage, description, etc
     */
    private final @NotNull CommandSpec spec;

    /**
     * The name of the command. Should be properly capitalised.
     */
    private final @NotNull String name;

    /**
     * Tells if the console command sender could execute this command
     */
    private final boolean console;

    /**
     * The permission required to use this command. Nullable.
     */
    private final @Nullable CommandPermission permission;

    /**
     * A predicate used for testing the size of the arguments list passed to this command
     */
    private final @NotNull Predicate<Integer> argumentCheck;

    public Command(@NotNull CommandSpec spec, @NotNull String name, @Nullable CommandPermission permission, @NotNull Predicate<Integer> argumentCheck) {
        this(spec, name, true, permission, argumentCheck);
    }

    public Command(@NotNull CommandSpec spec, @NotNull String name, boolean console, @Nullable CommandPermission permission, @NotNull Predicate<Integer> argumentCheck) {
        this.spec = spec;
        this.name = name;
        this.console = console;
        this.permission = permission;
        this.argumentCheck = argumentCheck;
    }

    /**
     * Gets the commands spec.
     *
     * @return the command spec
     */
    public @NotNull CommandSpec getSpec() {
        return this.spec;
    }

    /**
     * Gets the short name of this command
     *
     * <p>The result should be appropriately capitalised.</p>
     *
     * @return the command name
     */
    public @NotNull String getName() {
        return this.name;
    }

    /**
     * Gets if the console command sender could execute the command
     *
     * @return true, otherwise false
     */
    public boolean isConsole() {
        return this.console;
    }

    /**
     * Gets the permission required by this command, if present
     *
     * @return the command permission
     */
    public @NotNull Optional<CommandPermission> getPermission() {
        return Optional.ofNullable(this.permission);
    }

    /**
     * Gets the predicate used to validate the number of arguments provided to
     * the command on execution
     *
     * @return the argument checking predicate
     */
    public @NotNull Predicate<Integer> getArgumentCheck() {
        return this.argumentCheck;
    }

    /**
     * Gets the commands description.
     *
     * @return the description
     */
    public Component getDescription() {
        return getSpec().description();
    }

    /**
     * Gets the usage of this command.
     * Will only return a non empty result for main commands.
     *
     * @return the usage of this command.
     */
    public String getUsage() {
        String usage = getSpec().usage();
        return usage == null ? "" : usage;
    }

    /**
     * Gets the arguments required by this command
     *
     * @return the commands arguments
     */
    public Optional<List<Argument>> getArgs() {
        return Optional.ofNullable(getSpec().args());
    }

    // Main execution method for the command.
    public abstract void execute(MultiPlugin plugin, Sender sender, T target, ArgumentList args, String label) throws CommandException;


    // Tab completion method - default implementation is provided as some commands do not provide tab completions.
    public List<String> tabComplete(MultiPlugin plugin, Sender sender, ArgumentList args) {
        return Collections.emptyList();
    }

    /**
     * Sends a brief command usage message to the Sender.
     * If this command has child commands, the children are listed. Otherwise, a basic usage message is sent.
     *
     * @param sender the sender to send the usage to
     * @param label the label used when executing the command
     */
    public abstract void sendUsage(Sender sender, String label);

    /**
     * Sends a detailed command usage message to the Sender.
     * If this command has child commands, nothing is sent. Otherwise, a detailed messaging containing a description
     * and argument usage is sent.
     *
     * @param sender the sender to send the usage to
     * @param label the label used when executing the command
     */
    public abstract void sendDetailedUsage(Sender sender, String label);

    /**
     * Returns true if the sender is authorised to use this command
     * <p>
     * Commands with children are likely to override this method to check for permissions based upon whether
     * a sender has access to any sub commands.
     *
     * @param sender the sender
     * @return true if the sender has permission to use this command
     */
    public boolean isAuthorized(Sender sender) {
        return this.permission == null || this.permission.isAuthorized(sender);
    }

    /**
     * Gets if this command should be displayed in command listings, or "hidden"
     *
     * @return if the command should be displayed
     */
    public boolean shouldDisplay() {
        return true;
    }

}