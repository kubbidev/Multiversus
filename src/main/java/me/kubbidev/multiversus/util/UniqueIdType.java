package me.kubbidev.multiversus.util;

import me.kubbidev.multiversus.plugin.MultiPlugin;
import net.multiversus.api.event.player.lookup.UniqueIdDetermineTypeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.UUID;

/**
 * Encapsulates the type of a players unique id.
 */
public final class UniqueIdType {

    public static final UniqueIdType AUTHENTICATED = new UniqueIdType(
            UniqueIdDetermineTypeEvent.TYPE_AUTHENTICATED,
            NamedTextColor.DARK_GREEN,
            "multiversus.command.user.info.uuid-type.mojang",
            "multiversus.command.user.info.uuid-type.desc.mojang"
    );

    public static final UniqueIdType UNAUTHENTICATED = new UniqueIdType(
            UniqueIdDetermineTypeEvent.TYPE_UNAUTHENTICATED,
            NamedTextColor.DARK_GRAY,
            "multiversus.command.user.info.uuid-type.not-mojang",
            "multiversus.command.user.info.uuid-type.desc.not-mojang"
    );

    public static final UniqueIdType NPC = new UniqueIdType(
            UniqueIdDetermineTypeEvent.TYPE_NPC,
            NamedTextColor.GOLD,
            "multiversus.command.user.info.uuid-type.npc",
            "multiversus.command.user.info.uuid-type.desc.npc"
    );

    public static final UniqueIdType UNKNOWN = new UniqueIdType(
            UniqueIdDetermineTypeEvent.TYPE_UNKNOWN,
            NamedTextColor.RED,
            "multiversus.command.user.info.uuid-type.unknown",
            "multiversus.command.user.info.uuid-type.desc.unknown"
    );

    public static UniqueIdType determineType(UUID uniqueId, MultiPlugin plugin) {
        // determine initial type based on the uuid version
        String type;
        switch (uniqueId.version()) {
            case 4:
                type = UniqueIdDetermineTypeEvent.TYPE_AUTHENTICATED;
                break;
            case 3:
                type = UniqueIdDetermineTypeEvent.TYPE_UNAUTHENTICATED;
                break;
            case 2:
                // if the uuid is version 2, assume it is an NPC
                // see: https://github.com/LuckPerms/LuckPerms/issues/1470
                // and https://github.com/LuckPerms/LuckPerms/issues/1470#issuecomment-475403162
                type = UniqueIdDetermineTypeEvent.TYPE_NPC;
                break;
            default:
                type = UniqueIdDetermineTypeEvent.TYPE_UNKNOWN;
                break;
        }

        // call the event
        type = plugin.getEventDispatcher().dispatchUniqueIdDetermineType(uniqueId, type);

        switch (type) {
            case UniqueIdDetermineTypeEvent.TYPE_AUTHENTICATED:
                return AUTHENTICATED;
            case UniqueIdDetermineTypeEvent.TYPE_UNAUTHENTICATED:
                return UNAUTHENTICATED;
            case UniqueIdDetermineTypeEvent.TYPE_NPC:
                return NPC;
            case UniqueIdDetermineTypeEvent.TYPE_UNKNOWN:
                return UNKNOWN;
            default:
                return new UniqueIdType(type);
        }
    }

    private final String type;
    private final Component component;

    // constructor used for built-in types
    private UniqueIdType(String type, TextColor displayColor, String translationKey, String translationKeyHover) {
        this.type = type;
        this.component = Component.translatable()
                .key(translationKey)
                .color(displayColor)
                .hoverEvent(HoverEvent.showText(Component.translatable(
                        translationKeyHover,
                        NamedTextColor.DARK_GRAY
                )))
                .build();
    }

    // constructor used for types provided via the API
    private UniqueIdType(String type) {
        this.type = type;
        this.component = Component.text()
                .content(type)
                .color(NamedTextColor.GOLD)
                .hoverEvent(HoverEvent.showText(Component.translatable(
                        "multiversus.command.user.info.uuid-type.desc.api",
                        NamedTextColor.GRAY
                )))
                .build();
    }

    public String getType() {
        return this.type;
    }

    public Component describe() {
        return this.component;
    }
}