package me.kubbidev.multiversus.command.access;

import me.kubbidev.multiversus.sender.Sender;

/**
 * An enumeration of the permissions required to execute built in Multiversus commands.
 */
public enum CommandPermission {

    SYNC("sync", Type.NONE),
    INFO("info", Type.NONE),
    IMPORT("import", Type.NONE),
    EXPORT("export", Type.NONE),
    RELOAD_CONFIG("reloadconfig", Type.NONE),
    TRANSLATIONS("translations", Type.NONE),

    USER_INFO("info", Type.USER);

    public static final String ROOT = "multiversus.";

    private final String node;
    private final String permission;

    private final Type type;

    CommandPermission(String node, Type type) {
        this.type = type;

        if (type == Type.NONE) {
            this.node = node;
        } else {
            this.node = type.getTag() + "." + node;
        }

        this.permission = ROOT + this.node;
    }

    public String getNode() {
        return this.node;
    }

    public String getPermission() {
        return this.permission;
    }

    public boolean isAuthorized(Sender sender) {
        return sender.hasPermission(this);
    }

    public Type getType() {
        return this.type;
    }

    public enum Type {

        NONE(null),
        USER("user");

        private final String tag;

        Type(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return this.tag;
        }
    }

}