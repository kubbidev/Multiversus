package me.kubbidev.multiversus.commands.misc;

import me.kubbidev.multiversus.command.spec.CommandSpec;
import me.kubbidev.multiversus.command.abstraction.CommandException;
import me.kubbidev.multiversus.command.abstraction.SingleCommand;
import me.kubbidev.multiversus.command.access.CommandPermission;
import me.kubbidev.multiversus.command.util.ArgumentList;
import me.kubbidev.multiversus.locale.Message;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.sender.Sender;
import me.kubbidev.multiversus.util.Predicates;

public class ReloadConfigCommand extends SingleCommand {
    public ReloadConfigCommand() {
        super(CommandSpec.RELOAD_CONFIG, "ReloadConfig", CommandPermission.RELOAD_CONFIG, Predicates.alwaysFalse());
    }

    @Override
    public void execute(MultiPlugin plugin, Sender sender, ArgumentList args, String label) throws CommandException {
        plugin.getConfiguration().reload();
        Message.RELOAD_CONFIG_SUCCESS.send(sender);
    }
}