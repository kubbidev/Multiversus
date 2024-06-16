package me.kubbidev.multiversus.commands.misc;

import me.kubbidev.multiversus.command.abstraction.CommandException;
import me.kubbidev.multiversus.command.abstraction.SingleCommand;
import me.kubbidev.multiversus.command.access.CommandPermission;
import me.kubbidev.multiversus.command.spec.CommandSpec;
import me.kubbidev.multiversus.command.util.ArgumentList;
import me.kubbidev.multiversus.locale.Message;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.sender.Sender;
import me.kubbidev.multiversus.util.Predicates;

import java.util.Locale;
import java.util.stream.Collectors;

public class TranslationsCommand extends SingleCommand {

    public TranslationsCommand() {
        super(CommandSpec.TRANSLATIONS, "Translations", CommandPermission.TRANSLATIONS, Predicates.alwaysFalse());
    }

    @Override
    public void execute(MultiPlugin plugin, Sender sender, ArgumentList args, String label) throws CommandException {
        Message.TRANSLATIONS_SEARCHING.send(sender);
        Message.INSTALLED_TRANSLATIONS.send(sender, plugin.getTranslationManager().getInstalledLocales().stream().map(Locale::toLanguageTag).sorted().collect(Collectors.toList()));
    }
}