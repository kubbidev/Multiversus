package me.kubbidev.multiversus.storage.implementation.sql.connection.file;

import me.kubbidev.multiversus.storage.StorageMetadata;
import me.kubbidev.multiversus.storage.implementation.sql.connection.ConnectionFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstract {@link ConnectionFactory} using a file based database driver.
 */
abstract class FlatfileConnectionFactory implements ConnectionFactory {

    /** The current open connection, if any */
    private NonClosableConnection connection;
    /** The path to the database file */
    private final Path file;

    FlatfileConnectionFactory(Path file) {
        this.file = file;
    }

    /**
     * Creates a connection to the database.
     *
     * @param file the database file
     * @return the connection
     * @throws SQLException if any error occurs
     */
    protected abstract Connection createConnection(Path file) throws SQLException;

    @Override
    public synchronized Connection getConnection() throws SQLException {
        NonClosableConnection connection = this.connection;
        if (connection == null || connection.isClosed()) {
            connection = new NonClosableConnection(createConnection(this.file));
            this.connection = connection;
        }
        return connection;
    }

    @Override
    public void shutdown() throws Exception {
        if (this.connection != null) {
            this.connection.shutdown();
        }
    }

    /**
     * Gets the path of the file the database driver actually ends up writing to.
     *
     * @return the write file
     */
    protected Path getWriteFile() {
        return this.file;
    }

    @Override
    public StorageMetadata getMeta() {
        StorageMetadata metadata = new StorageMetadata();

        Path databaseFile = getWriteFile();
        if (Files.exists(databaseFile)) {
            try {
                long length = Files.size(databaseFile);
                metadata.sizeBytes(length);
            } catch (IOException e) {
                // ignore
            }
        }

        return metadata;
    }
}