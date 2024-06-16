package me.kubbidev.multiversus.storage.implementation.sql.connection.hikari;

import me.kubbidev.multiversus.storage.misc.StorageCredentials;

import java.util.function.Function;

public class MariaDbConnectionFactory extends DriverBasedHikariConnectionFactory {
    public MariaDbConnectionFactory(StorageCredentials configuration) {
        super(configuration);
    }

    @Override
    public String getImplementationName() {
        return "MariaDB";
    }

    @Override
    protected String defaultPort() {
        return "3306";
    }

    @Override
    protected String driverClassName() {
        return "org.mariadb.jdbc.Driver";
    }

    @Override
    protected String driverJdbcIdentifier() {
        return "mariadb";
    }

    @Override
    public Function<String, String> getStatementProcessor() {
        return s -> s.replace('\'', '`'); // use backticks for quotes
    }
}