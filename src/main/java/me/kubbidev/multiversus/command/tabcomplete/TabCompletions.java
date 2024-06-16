package me.kubbidev.multiversus.command.tabcomplete;

import me.kubbidev.multiversus.plugin.MultiPlugin;

/**
 * Common completion suppliers used by the plugin
 */
public final class TabCompletions {

    private static final CompletionSupplier BOOLEAN = CompletionSupplier.startsWith("true", "false");

    // bit of a weird pattern, but meh it kinda works, reduces the boilerplate
    // of calling the commandmanager + tabcompletions getters every time


    public TabCompletions(MultiPlugin plugin) {
    }

    public static CompletionSupplier booleans() {
        return BOOLEAN;
    }
}