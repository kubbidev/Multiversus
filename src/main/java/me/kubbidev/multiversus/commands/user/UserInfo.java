package me.kubbidev.multiversus.commands.user;

import me.kubbidev.multiversus.command.abstraction.ChildCommand;
import me.kubbidev.multiversus.command.access.CommandPermission;
import me.kubbidev.multiversus.command.spec.CommandSpec;
import me.kubbidev.multiversus.command.util.ArgumentList;
import me.kubbidev.multiversus.command.abstraction.CommandException;
import me.kubbidev.multiversus.command.access.ArgumentPermissions;
import me.kubbidev.multiversus.locale.Message;
import me.kubbidev.multiversus.model.User;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.sender.Sender;
import me.kubbidev.multiversus.util.Predicates;
import me.kubbidev.multiversus.util.UniqueIdType;

public class UserInfo extends ChildCommand<User> {
    public UserInfo() {
        super(CommandSpec.USER_INFO, "info", CommandPermission.USER_INFO, Predicates.alwaysFalse());
    }

    @Override
    public void execute(MultiPlugin plugin, Sender sender, User target, ArgumentList args, String label) throws CommandException {
        if (ArgumentPermissions.checkViewPerms(plugin, sender, getPermission().get(), target)) {
            Message.COMMAND_NO_PERMISSION.send(sender);
            return;
        }

        Message.USER_INFO_GENERAL.send(sender,
                target.getUsername().orElse("Unknown"),
                target.getUniqueId().toString(),
                UniqueIdType.determineType(target.getUniqueId(), plugin).describe(),
                plugin.getBootstrap().isPlayerOnline(target.getUniqueId())
        );
    }
}