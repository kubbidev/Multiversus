package me.kubbidev.multiversus.commands.misc;

import com.google.gson.JsonObject;
import me.kubbidev.multiversus.backup.Importer;
import me.kubbidev.multiversus.command.access.CommandPermission;
import me.kubbidev.multiversus.command.spec.CommandSpec;
import me.kubbidev.multiversus.command.abstraction.CommandException;
import me.kubbidev.multiversus.command.abstraction.SingleCommand;
import me.kubbidev.multiversus.command.util.ArgumentList;
import me.kubbidev.multiversus.locale.Message;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.sender.Sender;
import me.kubbidev.multiversus.util.Predicates;
import me.kubbidev.multiversus.util.gson.GsonProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;

public class ImportCommand extends SingleCommand {
    private final AtomicBoolean running = new AtomicBoolean(false);

    public ImportCommand() {
        super(CommandSpec.IMPORT, "Import", CommandPermission.IMPORT, Predicates.notInRange(1, 3));
    }

    @Override
    public void execute(MultiPlugin plugin, Sender sender, ArgumentList args, String label) throws CommandException {
        if (this.running.get()) {
            Message.IMPORT_ALREADY_RUNNING.send(sender);
            return;
        }

        String fileName = args.get(0);
        Path dataDirectory = plugin.getBootstrap().getDataDirectory();
        Path path = dataDirectory.resolve(fileName);

        if (!path.getParent().equals(dataDirectory) || path.getFileName().toString().equals("config.yml")) {
            Message.FILE_NOT_WITHIN_DIRECTORY.send(sender, path.toString());
            return;
        }

        // try auto adding the '.json.gz' extension
        if (!Files.exists(path) && !fileName.contains(".")) {
            Path pathWithDefaultExtension = path.resolveSibling(fileName + ".json.gz");
            if (Files.exists(pathWithDefaultExtension)) {
                path = pathWithDefaultExtension;
            }
        }

        if (!Files.exists(path)) {
            Message.IMPORT_FILE_DOESNT_EXIST.send(sender, path.toString());
            return;
        }

        if (!Files.isReadable(path)) {
            Message.IMPORT_FILE_NOT_READABLE.send(sender, path.toString());
            return;
        }

        if (!this.running.compareAndSet(false, true)) {
            Message.IMPORT_ALREADY_RUNNING.send(sender);
            return;
        }

        JsonObject data;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(path)), StandardCharsets.UTF_8))) {
            data = GsonProvider.normal().fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            plugin.getLogger().warn("Error whilst reading from the import file", e);
            Message.IMPORT_FILE_READ_FAILURE.send(sender);
            this.running.set(false);
            return;
        }

        Importer importer = new Importer(plugin, sender, data);

        // Run the importer in its own thread.
        plugin.getBootstrap().getScheduler().executeAsync(() -> {
            try {
                importer.run();
            } finally {
                this.running.set(false);
            }
        });
    }

    public boolean isRunning() {
        return this.running.get();
    }
}