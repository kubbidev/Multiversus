package me.kubbidev.multiversus.brigadier;

import com.mojang.brigadier.tree.LiteralCommandNode;
import me.kubbidev.multiversus.FBukkitPlugin;
import me.kubbidev.multiversus.sender.Sender;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import me.lucko.commodore.file.CommodoreFileReader;
import org.bukkit.command.Command;

import java.io.InputStream;

/**
 * Registers Multiversus command data to brigadier using {@link Commodore}.
 */
public final class MultiBrigadier {
    private MultiBrigadier() {
    }

    public static void register(FBukkitPlugin plugin, Command pluginCommand) throws Exception {
        Commodore commodore = CommodoreProvider.getCommodore(plugin.getLoader());
        try (InputStream is = plugin.getBootstrap().getResourceStream("multiversus.commodore")) {
            if (is == null) {
                throw new Exception("Brigadier command data missing from jar");
            }

            LiteralCommandNode<?> commandNode = CommodoreFileReader.INSTANCE.parse(is);
            commodore.register(pluginCommand, commandNode, player -> {
                Sender playerAsSender = plugin.getSenderFactory().wrap(player);
                return plugin.getCommandManager().hasPermissionForAny(playerAsSender);
            });
        }
    }

}