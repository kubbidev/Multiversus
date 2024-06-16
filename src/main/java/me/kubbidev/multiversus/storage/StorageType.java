package me.kubbidev.multiversus.storage;

import com.google.common.collect.ImmutableList;

import java.util.List;

public enum StorageType {

    // Config file based
    YAML("YAML", "yaml", "yml"),
    JSON("JSON", "json", "flatfile"),
    HOCON("HOCON", "hocon"),
    TOML("TOML", "toml"),
    YAML_COMBINED("YAML Combined", "yaml-combined"),
    JSON_COMBINED("JSON Combined", "json-combined"),
    HOCON_COMBINED("HOCON Combined", "hocon-combined"),
    TOML_COMBINED("TOML Combined", "toml-combined"),

    // Remote databases
    MONGODB("MongoDB", "mongodb"),
    MARIADB("MariaDB", "mariadb"),
    MYSQL("MySQL", "mysql"),
    POSTGRESQL("PostgreSQL", "postgresql"),

    // Local databases
    SQLITE("SQLite", "sqlite"),
    H2("H2", "h2"),

    // Custom
    CUSTOM("Custom", "custom");

    private final String name;

    private final List<String> identifiers;

    StorageType(String name, String... identifiers) {
        this.name = name;
        this.identifiers = ImmutableList.copyOf(identifiers);
    }

    public static StorageType parse(String name, StorageType def) {
        for (StorageType t : values()) {
            for (String id : t.getIdentifiers()) {
                if (id.equalsIgnoreCase(name)) {
                    return t;
                }
            }
        }
        return def;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getIdentifiers() {
        return this.identifiers;
    }
}