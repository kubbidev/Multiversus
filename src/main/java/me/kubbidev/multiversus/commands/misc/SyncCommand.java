package me.kubbidev.multiversus.commands.misc;

import me.kubbidev.multiversus.command.access.CommandPermission;
import me.kubbidev.multiversus.command.spec.CommandSpec;
import me.kubbidev.multiversus.command.util.ArgumentList;
import me.kubbidev.multiversus.command.abstraction.CommandException;
import me.kubbidev.multiversus.command.abstraction.SingleCommand;
import me.kubbidev.multiversus.locale.Message;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.sender.Sender;
import me.kubbidev.multiversus.util.Predicates;

public class SyncCommand extends SingleCommand {
    public SyncCommand() {
        super(CommandSpec.SYNC, "Sync", CommandPermission.SYNC, Predicates.alwaysFalse());
    }

    @Override
    public void execute(MultiPlugin plugin, Sender sender, ArgumentList args, String label) throws CommandException {
        Message.UPDATE_TASK_REQUEST.send(sender);
        plugin.getSyncTaskBuffer().request().join();
        Message.UPDATE_TASK_COMPLETE.send(sender);
    }
}