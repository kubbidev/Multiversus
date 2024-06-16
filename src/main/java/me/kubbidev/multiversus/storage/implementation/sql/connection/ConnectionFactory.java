package me.kubbidev.multiversus.storage.implementation.sql.connection;

import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.storage.StorageMetadata;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public interface ConnectionFactory {

    String getImplementationName();

    void init(MultiPlugin plugin);

    void shutdown() throws Exception;

    StorageMetadata getMeta();

    Function<String, String> getStatementProcessor();

    Connection getConnection() throws SQLException;

}