package me.kubbidev.multiversus;

import me.kubbidev.multiversus.command.CommandManager;
import me.kubbidev.multiversus.command.util.ArgumentTokenizer;
import me.kubbidev.multiversus.config.ConfigKeys;
import me.kubbidev.multiversus.sender.Sender;
import me.kubbidev.multiversus.util.CommandMapUtil;
import org.bukkit.Server;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class BukkitCommandExecutor extends CommandManager implements TabExecutor, Listener {
    private static final boolean SELECT_ENTITIES_SUPPORTED;

    static {
        boolean selectEntitiesSupported = false;
        try {
            Server.class.getMethod("selectEntities", CommandSender.class, String.class);
            selectEntitiesSupported = true;
        } catch (NoSuchMethodException e) {
            // ignore
        }
        SELECT_ENTITIES_SUPPORTED = selectEntitiesSupported;
    }

    protected final FBukkitPlugin plugin;
    protected final PluginCommand command;

    public BukkitCommandExecutor(FBukkitPlugin plugin, PluginCommand command) {
        super(plugin);
        this.plugin = plugin;
        this.command = command;
    }

    public void register() {
        this.command.setExecutor(this);
        this.command.setTabCompleter(this);
        this.plugin.getBootstrap().getServer().getPluginManager().registerEvents(this, this.plugin.getLoader());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Sender wrapped = this.plugin.getSenderFactory().wrap(sender);
        List<String> arguments = resolveSelectors(sender, ArgumentTokenizer.EXECUTE.tokenizeInput(args));
        executeCommand(wrapped, label, arguments);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Sender wrapped = this.plugin.getSenderFactory().wrap(sender);
        List<String> arguments = resolveSelectors(sender, ArgumentTokenizer.TAB_COMPLETE.tokenizeInput(args));
        return tabCompleteCommand(wrapped, arguments);
    }

    // Support Multiversus commands prefixed with a '/' from the console.
    @EventHandler(ignoreCancelled = true)
    public void onConsoleCommand(ServerCommandEvent e) {
        if (!(e.getSender() instanceof ConsoleCommandSender)) {
            return;
        }

        String buffer = e.getCommand();
        if (buffer.isEmpty() || buffer.charAt(0) != '/') {
            return;
        }

        buffer = buffer.substring(1);

        String commandLabel;
        int firstSpace = buffer.indexOf(' ');
        if (firstSpace == -1) {
            commandLabel = buffer;
        } else {
            commandLabel = buffer.substring(0, firstSpace);
        }

        Command command = CommandMapUtil.getCommandMap(this.plugin.getBootstrap().getServer()).getCommand(commandLabel);
        if (command != this.command) {
            return;
        }

        e.setCommand(buffer);
    }

    private List<String> resolveSelectors(CommandSender sender, List<String> args) {
        if (!SELECT_ENTITIES_SUPPORTED) {
            return args;
        }

        if (!this.plugin.getConfiguration().get(ConfigKeys.RESOLVE_COMMAND_SELECTORS)) {
            return args;
        }

        for (ListIterator<String> it = args.listIterator(); it.hasNext(); ) {
            String arg = it.next();
            if (arg.isEmpty() || arg.charAt(0) != '@') {
                continue;
            }

            List<Player> matchedPlayers;
            try {
                matchedPlayers = this.plugin.getBootstrap().getServer().selectEntities(sender, arg).stream()
                        .filter(e -> e instanceof Player)
                        .map(e -> (Player) e)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                this.plugin.getLogger().warn("Error parsing selector '" + arg + "' for " + sender + " executing " + args, e);
                continue;
            }

            if (matchedPlayers.isEmpty()) {
                continue;
            }

            if (matchedPlayers.size() > 1) {
                this.plugin.getLogger().warn("Error parsing selector '" + arg + "' for " + sender + " executing " + args +
                        ": ambiguous result (more than one player matched) - " + matchedPlayers);
                continue;
            }

            Player player = matchedPlayers.getFirst();
            it.set(player.getUniqueId().toString());
        }

        return args;
    }
}