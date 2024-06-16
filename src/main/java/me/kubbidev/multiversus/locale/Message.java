package me.kubbidev.multiversus.locale;

import me.kubbidev.multiversus.plugin.AbstractMultiPlugin;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.plugin.bootstrap.MultiBootstrap;
import me.kubbidev.multiversus.sender.Sender;
import me.kubbidev.multiversus.storage.StorageMetadata;
import me.kubbidev.multiversus.util.DurationFormatter;
import net.multiversus.api.util.Tristate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.Style.style;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

/**
 * A collection of formatted messages used by the plugin.
 */
public interface Message {

    DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd '@' HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    TextComponent OPEN_BRACKET = Component.text('(');
    TextComponent CLOSE_BRACKET = Component.text(')');
    TextComponent FULL_STOP = Component.text('.');

    Component PREFIX_COMPONENT = text()
            .color(GRAY)
            .append(text('['))
            .append(text()
                    .decoration(BOLD, true)
                    .append(text('M', AQUA))
                    .append(text('L', DARK_AQUA))
            )
            .append(text(']'))
            .build();

    static TextComponent prefixed(ComponentLike component) {
        return text()
                .append(PREFIX_COMPONENT)
                .append(space())
                .append(component)
                .build();
    }

    Args1<MultiBootstrap> STARTUP_BANNER = bootstrap -> {
        Component infoLine1 = text()
                .append(text(AbstractMultiPlugin.getPluginName(), DARK_GREEN))
                .append(space())
                .append(text("v" + bootstrap.getVersion(), AQUA))
                .build();

        Component infoLine2 = text()
                .color(DARK_GRAY)
                .append(text("Running on "))
                .append(text(bootstrap.getType().getFriendlyName()))
                .append(text(" - "))
                .append(text(bootstrap.getServerBrand()))
                .build();

        // "              "
        // "  |\/| |      "
        // "  |  | |___   "

        return joinNewline(
                text()
                        .append(text("       ", AQUA))
                        .append(text("       ", DARK_AQUA))
                        .build(),
                text()
                        .append(text("  |\\/| ", AQUA))
                        .append(text("|      ", DARK_AQUA))
                        .append(infoLine1)
                        .build(),
                text()
                        .append(text("  |  | ", AQUA))
                        .append(text("|___   ", DARK_AQUA))
                        .append(infoLine2)
                        .build(),
                empty()
        );
    };

    Args2<String, Component> COMMAND_USAGE_DETAILED_HEADER = (name, usage) -> joinNewline(
            // "&3&lCommand Usage &3- &b{}"
            // "&b> &7{}"
            prefixed(text()
                    .append(translatable("multiversus.commandsystem.usage.usage-header", DARK_AQUA, BOLD))
                    .append(text(" - ", DARK_AQUA))
                    .append(text(name, AQUA))),
            prefixed(text()
                    .append(text('>', AQUA))
                    .append(space())
                    .append(text().color(GRAY).append(usage)))
    );

    Args0 COMMAND_USAGE_DETAILED_ARGS_HEADER = () -> prefixed(translatable()
            // "&3Arguments:"
            .key("multiversus.commandsystem.usage.arguments-header")
            .color(DARK_AQUA)
            .append(text(':'))
    );

    Args2<Component, Component> COMMAND_USAGE_DETAILED_ARG = (arg, usage) -> prefixed(text()
            // "&b- {}&3 -> &7{}"
            .append(text('-', AQUA))
            .append(space())
            .append(arg)
            .append(text(" -> ", DARK_AQUA))
            .append(text().color(GRAY).append(usage))
    );

    Args1<String> REQUIRED_ARGUMENT = name -> text()
            .color(DARK_GRAY)
            .append(text('<'))
            .append(text(name, GRAY))
            .append(text('>'))
            .build();

    Args1<String> OPTIONAL_ARGUMENT = name -> text()
            .color(DARK_GRAY)
            .append(text('['))
            .append(text(name, GRAY))
            .append(text(']'))
            .build();

    Args1<String> VIEW_AVAILABLE_COMMANDS_PROMPT = label -> prefixed(translatable()
            // "&3Use &a/{} help &3to view available commands."
            .key("multiversus.commandsystem.available-commands")
            .color(DARK_AQUA)
            .arguments(text('/' + label + " help", GREEN))
            .append(FULL_STOP)
    );

    Args0 NO_PERMISSION_FOR_SUBCOMMANDS = () -> prefixed(translatable()
            // "&3You do not have permission to use any sub commands."
            .key("multiversus.commandsystem.no-permission-subcommands")
            .color(DARK_AQUA)
            .append(FULL_STOP)
    );

    Args0 ALREADY_EXECUTING_COMMAND = () -> prefixed(translatable()
            // "&7Another command is being executed, waiting for it to finish..."
            .key("multiversus.commandsystem.already-executing-command")
            .color(GRAY)
    );

    Args0 CONSOLE_NOT_ALLOWED_COMMAND = () -> prefixed(translatable()
            // "&cCould not execute this command as the console."
            .key("multiversus.commandsystem.console-not-allowed-command")
            .append(FULL_STOP)
            .color(RED)
    );

    Args0 COMMAND_NOT_RECOGNISED = () -> prefixed(translatable()
            // "&cCommand not recognised."
            .key("multiversus.commandsystem.command-not-recognised")
            .color(RED)
            .append(FULL_STOP)
    );

    Args0 COMMAND_NO_PERMISSION = () -> prefixed(translatable()
            // "&cYou do not have permission to use this command!"
            .key("multiversus.commandsystem.no-permission")
            .color(RED)
    );

    Args2<String, String> MAIN_COMMAND_USAGE_HEADER = (name, usage) -> prefixed(text()
            // "&b{} Sub Commands: &7({} ...)"
            .color(AQUA)
            .append(text(name))
            .append(space())
            .append(translatable("multiversus.commandsystem.usage.sub-commands-header"))
            .append(text(": "))
            .append(text()
                    .color(GRAY)
                    .append(OPEN_BRACKET)
                    .append(text(usage))
                    .append(text(" ..."))
                    .append(CLOSE_BRACKET)
            ));

    Args1<String> ILLEGAL_DATE_ERROR = invalid -> prefixed(translatable()
            // "&cCould not parse date &4{}&c."
            .key("multiversus.command.misc.date-parse-error")
            .color(RED)
            .arguments(text(invalid, DARK_RED))
            .append(FULL_STOP)
    );

    Args0 PAST_DATE_ERROR = () -> prefixed(translatable()
            // "&cYou cannot set a date in the past!"
            .key("multiversus.command.misc.date-in-past-error")
            .color(RED)
    );

    Args1<String> USER_NOT_FOUND = id -> prefixed(translatable()
            // "&cA user for &4{}&c could not be found."
            .key("multiversus.command.misc.loading.error.user-not-found")
            .color(RED)
            .arguments(text(id, DARK_RED))
            .append(FULL_STOP)
    );

    Args1<String> USER_INVALID_ENTRY = invalid -> prefixed(translatable()
            // "&4{}&c is not a valid username/uuid."
            .key("multiversus.command.misc.loading.error.user-invalid")
            .color(RED)
            .arguments(text(invalid, DARK_RED))
            .append(FULL_STOP)
    );

    Args0 LOADING_DATABASE_ERROR = () -> prefixed(translatable()
            // "&cA database error occurred whilst loading data. Please try again later. If you are a server admin, please check the console for any errors."
            .key("multiversus.login.loading-database-error")
            .color(RED)
            .append(FULL_STOP)
            .append(space())
            .append(translatable("multiversus.login.try-again"))
            .append(FULL_STOP)
            .append(space())
            .append(translatable("multiversus.login.server-admin-check-console-errors"))
            .append(FULL_STOP)
    );

    Args0 LOADING_STATE_ERROR = () -> prefixed(translatable()
            // "&cData for your user was not loaded during the pre-login stage - unable to continue. Please try again later. If you are a server admin, please check the console for any errors."
            .key("multiversus.login.data-not-loaded-at-pre")
            .color(RED)
            .append(text(" - "))
            .append(translatable("multiversus.login.unable-to-continue"))
            .append(FULL_STOP)
            .append(space())
            .append(translatable("multiversus.login.try-again"))
            .append(FULL_STOP)
            .append(space())
            .append(translatable("multiversus.login.server-admin-check-console-errors"))
            .append(FULL_STOP)
    );

    Args0 LOADING_STATE_ERROR_CB_OFFLINE_MODE = () -> prefixed(translatable()
            // "&cData for your user was not loaded during the pre-login stage - this is likely due to a conflict between CraftBukkit and the online-mode setting. Please check the server console for more information."
            .key("multiversus.login.data-not-loaded-at-pre")
            .color(RED)
            .append(text(" - "))
            .append(translatable("multiversus.login.craftbukkit-offline-mode-error"))
            .append(FULL_STOP)
            .append(space())
            .append(translatable("multiversus.login.server-admin-check-console-info"))
            .append(FULL_STOP)
    );

    Args0 UPDATE_TASK_REQUEST = () -> prefixed(translatable()
            // "&bAn update task has been requested. Please wait..."
            .color(AQUA)
            .key("multiversus.command.update-task.request")
            .append(FULL_STOP)
    );

    Args0 UPDATE_TASK_COMPLETE = () -> prefixed(translatable()
            // "&aUpdate task complete."
            .color(GREEN)
            .key("multiversus.command.update-task.complete")
            .append(FULL_STOP)
    );

    Args0 RELOAD_CONFIG_SUCCESS = () -> prefixed(translatable()
            // "&aThe configuration file was reloaded. &7(some options will only apply after the server has restarted)"
            .key("multiversus.command.reload-config.success")
            .color(GREEN)
            .append(FULL_STOP)
            .append(space())
            .append(text()
                    .color(GRAY)
                    .append(OPEN_BRACKET)
                    .append(translatable("multiversus.command.reload-config.restart-note"))
                    .append(CLOSE_BRACKET)
            )
    );

    Args2<MultiPlugin, StorageMetadata> INFO = (plugin, storageMeta) -> joinNewline(
            // "&2Running &bMultiversus v{}&2 by &bkubbidev&2."
            // "&f-  &3Platform: &f{}"
            // "&f-  &3Server Brand: &f{}"
            // "&f-  &3Server Version:"
            // "     &f{}"
            // "&f-  &bStorage:"
            // "     &3Type: &f{}"
            // "     &3Some meta value: {}"
            // "&f-  &3Extensions:"
            // "     &f{}"
            // "&f-  &bMessaging: &f{}"
            // "&f-  &bInstance:"
            // "     &3Online Players: &a{} &7(&a{}&7 unique)"
            // "     &3Uptime: &7{}"
            // "     &3Local Data: &a{} &7users",
            prefixed(translatable()
                    .key("multiversus.command.info.running-plugin")
                    .color(DARK_GREEN)
                    .append(space())
                    .append(text(AbstractMultiPlugin.getPluginName(), AQUA))
                    .append(space())
                    .append(text("v" + plugin.getBootstrap().getVersion(), AQUA))
                    .append(text(" by "))
                    .append(text("kubbidev", AQUA))
                    .append(FULL_STOP)),
            prefixed(text()
                    .color(DARK_AQUA)
                    .append(text("-  ", WHITE))
                    .append(translatable("multiversus.command.info.platform-key"))
                    .append(text(": "))
                    .append(text(plugin.getBootstrap().getType().getFriendlyName(), WHITE))),
            prefixed(text()
                    .color(DARK_AQUA)
                    .append(text("-  ", WHITE))
                    .append(translatable("multiversus.command.info.server-brand-key"))
                    .append(text(": "))
                    .append(text(plugin.getBootstrap().getServerBrand(), WHITE))),
            prefixed(text()
                    .color(DARK_AQUA)
                    .append(text("-  ", WHITE))
                    .append(translatable("multiversus.command.info.server-version-key"))
                    .append(text(':'))),
            prefixed(text()
                    .color(WHITE)
                    .append(text("     "))
                    .append(text(plugin.getBootstrap().getServerVersion()))),
            prefixed(text()
                    .color(AQUA)
                    .append(text("-  ", WHITE))
                    .append(translatable("multiversus.command.info.storage-key"))
                    .append(text(':'))),
            prefixed(text()
                    .apply(builder -> {
                        builder.append(text()
                                .color(DARK_AQUA)
                                .append(text("     "))
                                .append(translatable("multiversus.command.info.storage-type-key"))
                                .append(text(": "))
                                .append(text(plugin.getStorage().getName(), WHITE))
                        );

                        if (storageMeta.connected() != null) {
                            builder.append(newline());
                            builder.append(prefixed(text()
                                    .color(DARK_AQUA)
                                    .append(text("     "))
                                    .append(translatable("multiversus.command.info.storage.meta.connected-key"))
                                    .append(text(": "))
                                    .append(formatBoolean(storageMeta.connected()))
                            ));
                        }

                        if (storageMeta.ping() != null) {
                            builder.append(newline());
                            builder.append(prefixed(text()
                                    .color(DARK_AQUA)
                                    .append(text("     "))
                                    .append(translatable("multiversus.command.info.storage.meta.ping-key"))
                                    .append(text(": "))
                                    .append(text(storageMeta.ping() + "ms", GREEN))
                            ));
                        }

                        if (storageMeta.sizeBytes() != null) {
                            DecimalFormat format = new DecimalFormat("#.##");
                            String size = format.format(storageMeta.sizeBytes() / 1048576D) + "MB";

                            builder.append(newline());
                            builder.append(prefixed(text()
                                    .color(DARK_AQUA)
                                    .append(text("     "))
                                    .append(translatable("multiversus.command.info.storage.meta.file-size-key"))
                                    .append(text(": "))
                                    .append(text(size, GREEN))
                            ));
                        }
                    })),
            prefixed(text()
                    .color(AQUA)
                    .append(text("-  ", WHITE))
                    .append(translatable("multiversus.command.info.extensions-key"))
                    .append(text(':'))),
            prefixed(text()
                    .color(WHITE)
                    .append(text("     "))
                    .append(formatStringList(plugin.getExtensionManager().getLoadedExtensions().stream()
                            .map(e -> e.getClass().getSimpleName())
                            .collect(Collectors.toList())))),
            prefixed(text()
                    .color(AQUA)
                    .append(text("-  ", WHITE))
                    .append(translatable("multiversus.command.info.messaging-key"))
                    .append(text(": "))
                    .append(plugin.getMessagingService().<Component>map(s -> Component.text(s.getName(), WHITE)).orElse(translatable("multiversus.command.misc.none", WHITE)))),
            prefixed(text()
                    .color(AQUA)
                    .append(text("-  ", WHITE))
                    .append(translatable("multiversus.command.info.instance-key"))
                    .append(text(':'))),
            prefixed(text()
                    .color(DARK_AQUA)
                    .append(text("     "))
                    .append(translatable("multiversus.command.info.online-players-key"))
                    .append(text(": "))
                    .append(text(plugin.getBootstrap().getPlayerCount(), WHITE))
                    .append(space())
                    .append(text()
                            .color(GRAY)
                            .append(OPEN_BRACKET)
                            .append(translatable()
                                    .key("multiversus.command.info.online-players-unique")
                                    .arguments(text(plugin.getConnectionListener().getUniqueConnections().size(), GREEN))
                            )
                            .append(CLOSE_BRACKET)
                    )),
            prefixed(text()
                    .color(DARK_AQUA)
                    .append(text("     "))
                    .append(translatable("multiversus.command.info.uptime-key"))
                    .append(text(": "))
                    .append(text().color(GRAY).append(DurationFormatter.CONCISE_LOW_ACCURACY.format(Duration.between(plugin.getBootstrap().getStartupTime(), Instant.now()))))),
            prefixed(text()
                    .color(DARK_AQUA)
                    .append(text("     "))
                    .append(translatable("multiversus.command.info.local-data-key"))
                    .append(text(": "))
                    .append(translatable()
                            .key("multiversus.command.info.local-data")
                            .color(GRAY)
                            .arguments(
                                    Component.text(plugin.getUserManager().getAll().size(), GREEN)
                            )
                    ))
    );

    Args0 EXPORT_ALREADY_RUNNING = () -> prefixed(text()
            // "&cAnother export process is already running. Please wait for it to finish and try again."
            .color(RED)
            .append(translatable("multiversus.command.export.already-running"))
            .append(FULL_STOP)
            .append(space())
            .append(translatable("multiversus.command.misc.wait-to-finish"))
            .append(FULL_STOP)
    );

    Args1<String> EXPORT_FILE_ALREADY_EXISTS = file -> prefixed(text()
            // "&cError: File &4{}&c already exists."
            .color(RED)
            .append(translatable("multiversus.command.export.error-term"))
            .append(text(": "))
            .append(translatable("multiversus.command.export.file.already-exists", text(file, DARK_RED)))
            .append(FULL_STOP)
    );

    Args0 EXPORT_FILE_FAILURE = () -> prefixed(translatable()
            // "&cAn unexpected error occured whilst writing to the file."
            .key("multiversus.command.export.file-unexpected-error-writing")
            .color(RED)
            .append(FULL_STOP)
    );

    Args1<String> EXPORT_FILE_NOT_WRITABLE = file -> prefixed(text()
            // "&cError: File &4{}&c is not writable."
            .color(RED)
            .append(translatable("multiversus.command.export.error-term"))
            .append(text(": "))
            .append(translatable("multiversus.command.export.file.not-writable", text(file, DARK_RED)))
            .append(FULL_STOP)
    );

    Args1<String> EXPORT_LOG = msg -> prefixed(text()
            // "&3EXPORT &3&l> &f{}"
            .append(translatable("multiversus.logs.export-prefix", DARK_AQUA))
            .append(space())
            .append(text('>', DARK_AQUA, BOLD))
            .append(space())
            .append(text(msg, WHITE))
    );

    Args1<String> EXPORT_LOG_PROGRESS = msg -> prefixed(text()
            // "&3EXPORT &3&l> &7{}"
            .append(translatable("multiversus.logs.export-prefix", DARK_AQUA))
            .append(space())
            .append(text('>', DARK_AQUA, BOLD))
            .append(space())
            .append(text(msg, GRAY))
    );

    Args1<String> EXPORT_FILE_SUCCESS = file -> prefixed(translatable()
            // "&aSuccessfully exported to &b{}&a."
            .key("multiversus.command.export.file.success")
            .color(GREEN)
            .arguments(text(file, AQUA))
            .append(FULL_STOP)
    );

    Args0 IMPORT_ALREADY_RUNNING = () -> prefixed(text()
            // "&cAnother import process is already running. Please wait for it to finish and try again."
            .color(RED)
            .append(translatable("multiversus.command.import.already-running"))
            .append(FULL_STOP)
            .append(space())
            .append(translatable("multiversus.command.misc.wait-to-finish"))
            .append(FULL_STOP)
    );

    Args1<String> FILE_NOT_WITHIN_DIRECTORY = file -> prefixed(text()
            // "&cError: File &4{}&c must be a direct child of the data directory."
            .color(RED)
            .append(translatable("multiversus.command.import.error-term"))
            .append(text(": "))
            .append(translatable("multiversus.command.misc.file-must-be-in-data", text(file, DARK_RED)))
            .append(FULL_STOP)
    );

    Args1<String> IMPORT_FILE_DOESNT_EXIST = file -> prefixed(text()
            // "&cError: File &4{}&c does not exist."
            .color(RED)
            .append(translatable("multiversus.command.import.error-term"))
            .append(text(": "))
            .append(translatable("multiversus.command.import.file.doesnt-exist", text(file, DARK_RED)))
            .append(FULL_STOP)
    );

    Args1<String> IMPORT_FILE_NOT_READABLE = file -> prefixed(text()
            // "&cError: File &4{}&c is not readable."
            .color(RED)
            .append(translatable("multiversus.command.import.error-term"))
            .append(text(": "))
            .append(translatable("multiversus.command.import.file.not-readable", text(file, DARK_RED)))
            .append(FULL_STOP)
    );


    Args0 IMPORT_FILE_READ_FAILURE = () -> prefixed(text()
            // "&cAn unexpected error occured whilst reading from the import file. (is it the correct format?)"
            .color(RED)
            .append(translatable("multiversus.command.import.file.unexpected-error-reading"))
            .append(FULL_STOP)
            .append(space())
            .append(text()
                    .append(OPEN_BRACKET)
                    .append(translatable("multiversus.command.import.file.correct-format"))
                    .append(CLOSE_BRACKET)
            )
    );

    Args3<Integer, Integer, Integer> IMPORT_PROGRESS = (percent, processed, total) -> prefixed(text()
            // "&b(Import) &b-> &f{}&f% complete &7- &b{}&f/&b{} &foperations complete."
            .append(text()
                    .color(AQUA)
                    .append(OPEN_BRACKET)
                    .append(translatable("multiversus.command.import.term"))
                    .append(CLOSE_BRACKET)
            )
            .append(text(" -> ", AQUA))
            .append(translatable("multiversus.command.import.progress.percent", WHITE, text(percent)))
            .append(text(" - ", GRAY))
            .append(translatable()
                    .key("multiversus.command.import.progress.operations")
                    .color(WHITE)
                    .arguments(text(processed, AQUA), text(total, AQUA))
                    .append(FULL_STOP)
            )
    );

    Args0 IMPORT_START = () -> prefixed(text()
            // "&b(Import) &b-> &fStarting import process."
            .color(WHITE)
            .append(text()
                    .color(AQUA)
                    .append(OPEN_BRACKET)
                    .append(translatable("multiversus.command.import.term"))
                    .append(CLOSE_BRACKET)
            )
            .append(text(" -> ", AQUA))
            .append(translatable("multiversus.command.import.starting"))
            .append(FULL_STOP)
    );

    Args1<String> IMPORT_INFO = msg -> prefixed(text()
            // "&b(Import) &b-> &f{}."
            .color(WHITE)
            .append(text()
                    .color(AQUA)
                    .append(OPEN_BRACKET)
                    .append(translatable("multiversus.command.import.term"))
                    .append(CLOSE_BRACKET)
            )
            .append(text(" -> ", AQUA))
            .append(text(msg))
            .append(FULL_STOP)
    );

    Args1<Double> IMPORT_END_COMPLETE = seconds -> prefixed(text()
            // "&b(Import) &a&lCOMPLETED &7- took &b{} &7seconds."
            .color(GRAY)
            .append(text()
                    .color(AQUA)
                    .append(OPEN_BRACKET)
                    .append(translatable("multiversus.command.import.term"))
                    .append(CLOSE_BRACKET)
            )
            .append(space())
            .append(translatable("multiversus.command.import.completed", GREEN, BOLD))
            .append(text(" - "))
            .append(translatable("multiversus.command.import.duration", text(seconds, AQUA)))
            .append(FULL_STOP)
    );

    Args4<String, String, Component, Boolean> USER_INFO_GENERAL = (username, uuid, uuidType, online) -> joinNewline(
            // "&b&l> &bUser Info: &f{}"
            // "&f- &3UUID: &f{}"
            // "&f    &7(type: {}&7)"
            // "&f- &3Status: {}"
            prefixed(text()
                    .color(AQUA)
                    .append(text('>', style(BOLD)))
                    .append(space())
                    .append(translatable("multiversus.command.user.info.title"))
                    .append(text(": "))
                    .append(text(username, WHITE))),
            prefixed(text()
                    .color(DARK_AQUA)
                    .append(text("- ", WHITE))
                    .append(translatable("multiversus.command.user.info.uuid-key"))
                    .append(text(": "))
                    .append(text(uuid, WHITE))),
            prefixed(text()
                    .color(GRAY)
                    .append(text("    "))
                    .append(OPEN_BRACKET)
                    .append(translatable("multiversus.command.user.info.uuid-type-key"))
                    .append(text(": "))
                    .append(uuidType)
                    .append(CLOSE_BRACKET)),
            prefixed(text()
                    .color(DARK_AQUA)
                    .append(text("- ", WHITE))
                    .append(translatable("multiversus.command.user.info.status-key"))
                    .append(text(": "))
                    .append(online ? translatable("multiversus.command.user.info.status.online", GREEN) : translatable("multiversus.command.user.info.status.offline", RED)))
    );

    Args0 TRANSLATIONS_SEARCHING = () -> prefixed(translatable()
            // "&7Searching for available translations, please wait..."
            .key("multiversus.command.translations.searching")
            .color(GRAY)
    );

    Args1<Collection<String>> INSTALLED_TRANSLATIONS = locales -> prefixed(translatable()
            // "&aInstalled Translations:"
            .key("multiversus.command.translations.installed-translations")
            .color(GREEN)
            .append(text(':'))
            .append(space())
            .append(formatStringList(locales))
    );

    static Component formatStringList(Collection<String> strings) {
        Iterator<String> it = strings.iterator();
        if (!it.hasNext()) {
            return translatable("multiversus.command.misc.none", AQUA); // "&bNone"
        }

        TextComponent.Builder builder = text().color(DARK_AQUA).content(it.next());

        while (it.hasNext()) {
            builder.append(text(", ", GRAY));
            builder.append(text(it.next()));
        }

        return builder.build();
    }

    static Component formatBoolean(boolean bool) {
        return bool ? text("true", GREEN) : text("false", RED);
    }

    static Component formatTristate(Tristate tristate) {
        switch (tristate) {
            case TRUE:
                return text("true", GREEN);
            case FALSE:
                return text("false", RED);
            default:
                return text("undefined", GRAY);
        }
    }

    static Component joinNewline(final ComponentLike... components) {
        return join(JoinConfiguration.newlines(), components);
    }

    interface Args0 {
        Component build();

        default void send(Sender sender) {
            sender.sendMessage(build());
        }
    }

    interface Args1<A0> {
        Component build(A0 arg0);

        default void send(Sender sender, A0 arg0) {
            sender.sendMessage(build(arg0));
        }
    }

    interface Args2<A0, A1> {
        Component build(A0 arg0, A1 arg1);

        default void send(Sender sender, A0 arg0, A1 arg1) {
            sender.sendMessage(build(arg0, arg1));
        }
    }

    interface Args3<A0, A1, A2> {
        Component build(A0 arg0, A1 arg1, A2 arg2);

        default void send(Sender sender, A0 arg0, A1 arg1, A2 arg2) {
            sender.sendMessage(build(arg0, arg1, arg2));
        }
    }

    interface Args4<A0, A1, A2, A3> {
        Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3);

        default void send(Sender sender, A0 arg0, A1 arg1, A2 arg2, A3 arg3) {
            sender.sendMessage(build(arg0, arg1, arg2, arg3));
        }
    }

    interface Args5<A0, A1, A2, A3, A4> {
        Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4);

        default void send(Sender sender, A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4) {
            sender.sendMessage(build(arg0, arg1, arg2, arg3, arg4));
        }
    }

    interface Args6<A0, A1, A2, A3, A4, A5> {
        Component build(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5);

        default void send(Sender sender, A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5) {
            sender.sendMessage(build(arg0, arg1, arg2, arg3, arg4, arg5));
        }
    }
}
