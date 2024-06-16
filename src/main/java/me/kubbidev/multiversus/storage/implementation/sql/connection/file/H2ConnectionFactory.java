package me.kubbidev.multiversus.storage.implementation.sql.connection.file;

import me.kubbidev.multiversus.dependencies.Dependency;
import me.kubbidev.multiversus.plugin.MultiPlugin;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Properties;
import java.util.function.Function;

public class H2ConnectionFactory extends FlatfileConnectionFactory {
    private Constructor<?> connectionConstructor;

    public H2ConnectionFactory(Path file) {
        super(file);
    }

    @Override
    public String getImplementationName() {
        return "H2";
    }

    @Override
    public void init(MultiPlugin plugin) {
        ClassLoader classLoader = plugin.getDependencyManager().obtainClassLoaderWith(EnumSet.of(Dependency.H2_DRIVER));
        try {
            Class<?> connectionClass = classLoader.loadClass("org.h2.jdbc.JdbcConnection");
            this.connectionConstructor = connectionClass.getConstructor(String.class, Properties.class, String.class, Object.class, boolean.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Connection createConnection(Path file) throws SQLException {
        try {
            return (Connection) this.connectionConstructor.newInstance("jdbc:h2:" + file.toString(), new Properties(), null, null, false);
        } catch (ReflectiveOperationException e) {
            if (e.getCause() instanceof SQLException) {
                throw (SQLException) e.getCause();
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Path getWriteFile() {
        // h2 appends '.mv.db' to the end of the database name
        Path writeFile = super.getWriteFile();
        return writeFile.getParent().resolve(writeFile.getFileName().toString() + ".mv.db");
    }

    @Override
    public Function<String, String> getStatementProcessor() {
        return s -> s.replace('\'', '`')
                .replace("LIKE", "ILIKE")
                .replace("value", "`value`")
                .replace("``value``", "`value`");
    }
}