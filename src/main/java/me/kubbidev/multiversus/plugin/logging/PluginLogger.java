package me.kubbidev.multiversus.plugin.logging;

/**
 * Represents the logger instance being used by Multiversus on the platform.
 *
 * <p>Messages sent using the logger are sent prefixed with the Multiversus tag,
 * and on some implementations will be colored depending on the message type.</p>
 */
public interface PluginLogger {

    void info(String s);

    void warn(String s);

    void warn(String s, Throwable t);

    void severe(String s);

    void severe(String s, Throwable t);

}