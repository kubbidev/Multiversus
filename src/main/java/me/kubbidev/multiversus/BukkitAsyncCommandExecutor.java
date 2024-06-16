package me.kubbidev.multiversus;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import me.kubbidev.multiversus.command.util.ArgumentTokenizer;
import me.kubbidev.multiversus.sender.Sender;
import me.kubbidev.multiversus.util.CommandMapUtil;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;

import java.util.List;

public class BukkitAsyncCommandExecutor extends BukkitCommandExecutor {
    public BukkitAsyncCommandExecutor(FBukkitPlugin plugin, PluginCommand command) {
        super(plugin, command);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAsyncTabComplete(AsyncTabCompleteEvent e) {
        if (!e.isCommand()) {
            return;
        }

        String buffer = e.getBuffer();
        if (buffer.isEmpty()) {
            return;
        }

        if (buffer.charAt(0) == '/') {
            buffer = buffer.substring(1);
        }

        int firstSpace = buffer.indexOf(' ');
        if (firstSpace < 0) {
            return;
        }

        String commandLabel = buffer.substring(0, firstSpace);
        Command command = CommandMapUtil.getCommandMap(this.plugin.getBootstrap().getServer()).getCommand(commandLabel);
        if (command != this.command) {
            return;
        }

        Sender wrapped = this.plugin.getSenderFactory().wrap(e.getSender());
        List<String> arguments = ArgumentTokenizer.TAB_COMPLETE.tokenizeInput(buffer.substring(firstSpace + 1));
        List<String> completions = tabCompleteCommand(wrapped, arguments);

        e.setCompletions(completions);
        e.setHandled(true);
    }

}