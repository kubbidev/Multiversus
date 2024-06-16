package me.kubbidev.multiversus.storage.implementation.sql;

import me.kubbidev.multiversus.model.User;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.storage.StorageMetadata;
import me.kubbidev.multiversus.storage.implementation.StorageImplementation;
import me.kubbidev.multiversus.storage.implementation.sql.connection.ConnectionFactory;
import me.kubbidev.multiversus.storage.misc.PlayerSaveResultImpl;
import me.kubbidev.multiversus.util.Uuids;
import net.multiversus.api.model.PlayerSaveResult;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SqlStorage implements StorageImplementation {

    private static final String PLAYERS_SELECT_DISTINCT = "SELECT DISTINCT uuid FROM '{prefix}players'";

    private static final String PLAYER_SELECT_UUID_BY_USERNAME = "SELECT uuid FROM '{prefix}players' WHERE username=? LIMIT 1";
    private static final String PLAYER_SELECT_USERNAME_BY_UUID = "SELECT username FROM '{prefix}players' WHERE uuid=? LIMIT 1";
    private static final String PLAYER_UPDATE_USERNAME_FOR_UUID = "UPDATE '{prefix}players' SET username=? WHERE uuid=?";
    private static final String PLAYER_INSERT = "INSERT INTO '{prefix}players' (uuid, username) VALUES(?, ?)";
    private static final String PLAYER_DELETE = "DELETE FROM '{prefix}players' WHERE uuid=?";
    private static final String PLAYER_SELECT_ALL_UUIDS_BY_USERNAME = "SELECT uuid FROM '{prefix}players' WHERE username=? AND NOT uuid=?";
    private static final String PLAYER_DELETE_ALL_UUIDS_BY_USERNAME = "DELETE FROM '{prefix}players' WHERE username=? AND NOT uuid=?";
    private static final String PLAYER_SELECT_BY_UUID = "SELECT username FROM '{prefix}players' WHERE uuid=? LIMIT 1";
    private static final String PLAYER_SELECT_BY_UUID_MULTIPLE = "SELECT uuid, username FROM '{prefix}players' WHERE ";
//    private static final String PLAYER_SELECT_PRIMARY_GROUP_BY_UUID = "SELECT primary_group FROM '{prefix}players' WHERE uuid=? LIMIT 1";
//    private static final String PLAYER_UPDATE_PRIMARY_GROUP_BY_UUID = "UPDATE '{prefix}players' SET primary_group=? WHERE uuid=?";

    private final MultiPlugin plugin;

    private final ConnectionFactory connectionFactory;
    private final Function<String, String> statementProcessor;

    public SqlStorage(MultiPlugin plugin, ConnectionFactory connectionFactory, String tablePrefix) {
        this.plugin = plugin;
        this.connectionFactory = connectionFactory;
        this.statementProcessor = connectionFactory.getStatementProcessor().compose(s -> s.replace("{prefix}", tablePrefix));
    }

    @Override
    public MultiPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public String getImplementationName() {
        return this.connectionFactory.getImplementationName();
    }

    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }

    public Function<String, String> getStatementProcessor() {
        return this.statementProcessor;
    }

    @Override
    public void init() throws Exception {
        this.connectionFactory.init(this.plugin);

        boolean tableExists;
        try (Connection c = this.connectionFactory.getConnection()) {
            tableExists = tableExists(c, this.statementProcessor.apply("{prefix}players"));
        }

        if (!tableExists) {
            applySchema();
        }
    }

    private void applySchema() throws IOException, SQLException {
        List<String> statements;

        String schemaFileName = "me/kubbidev/multiversus/schema/" + this.connectionFactory.getImplementationName().toLowerCase(Locale.ROOT) + ".sql";
        try (InputStream is = this.plugin.getBootstrap().getResourceStream(schemaFileName)) {
            if (is == null) {
                throw new IOException("Couldn't locate schema file for " + this.connectionFactory.getImplementationName());
            }

            statements = SchemaReader.getStatements(is).stream()
                    .map(this.statementProcessor)
                    .collect(Collectors.toList());
        }

        try (Connection connection = this.connectionFactory.getConnection()) {
            boolean utf8mb4Unsupported = false;

            try (Statement s = connection.createStatement()) {
                for (String query : statements) {
                    s.addBatch(query);
                }

                try {
                    s.executeBatch();
                } catch (BatchUpdateException e) {
                    if (e.getMessage().contains("Unknown character set")) {
                        utf8mb4Unsupported = true;
                    } else {
                        throw e;
                    }
                }
            }

            // try again
            if (utf8mb4Unsupported) {
                try (Statement s = connection.createStatement()) {
                    for (String query : statements) {
                        s.addBatch(query.replace("utf8mb4", "utf8"));
                    }

                    s.executeBatch();
                }
            }
        }
    }

    @Override
    public void shutdown() {
        try {
            this.connectionFactory.shutdown();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Exception whilst disabling SQL storage", e);
        }
    }

    @Override
    public StorageMetadata getMeta() {
        return this.connectionFactory.getMeta();
    }

    @Override
    public User loadUser(UUID uniqueId, String username) throws SQLException {
        SqlPlayerData playerData;

        try (Connection c = this.connectionFactory.getConnection()) {
            playerData = selectPlayerData(c, uniqueId);
        }

        return createUser(uniqueId, username, playerData, true);
    }

    @Override
    public Map<UUID, User> loadUsers(Set<UUID> uniqueIds) throws Exception {
        Map<UUID, SqlPlayerData> playerDataMap;

        try (Connection c = this.connectionFactory.getConnection()) {
            playerDataMap = selectPlayerData(c, uniqueIds);
        }

        Map<UUID, User> users = new HashMap<>();
        for (UUID uniqueId : uniqueIds) {
            SqlPlayerData playerData = playerDataMap.get(uniqueId);
            users.put(uniqueId, createUser(uniqueId, null, playerData, false));
        }
        return users;
    }

    private User createUser(UUID uniqueId, String username, SqlPlayerData playerData, boolean saveAfterAudit) throws SQLException {
        User user = this.plugin.getUserManager().getOrMake(uniqueId, username);
        if (playerData != null) {
            user.setUsername(playerData.username, true);
        }

        if (saveAfterAudit) {
            saveUser(user);
        }

        return user;
    }

    @Override
    public void saveUser(User user) throws SQLException {
        try (Connection c = this.connectionFactory.getConnection()) {
            insertPlayerData(c, user.getUniqueId(), new SqlPlayerData(
                    user.getUsername().orElse("null").toLowerCase(Locale.ROOT)
            ));
        }
    }

    @Override
    public Set<UUID> getUniqueUsers() throws SQLException {
        Set<UUID> uuids = new HashSet<>();
        try (Connection c = this.connectionFactory.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYERS_SELECT_DISTINCT))) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID uuid = Uuids.fromString(rs.getString("uuid"));
                        if (uuid != null) {
                            uuids.add(uuid);
                        }
                    }
                }
            }
        }
        return uuids;
    }

    @Override
    public PlayerSaveResult savePlayerData(UUID uniqueId, String username) throws SQLException {
        username = username.toLowerCase(Locale.ROOT);
        String oldUsername = null;

        try (Connection c = this.connectionFactory.getConnection()) {
            SqlPlayerData existingPlayerData = selectPlayerData(c, uniqueId);
            if (existingPlayerData == null) {
                try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYER_INSERT))) {
                    ps.setString(1, uniqueId.toString());
                    ps.setString(2, username);
                    ps.execute();
                }
            } else {
                oldUsername = existingPlayerData.username;
                if (!username.equals(oldUsername)) {
                    try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYER_UPDATE_USERNAME_FOR_UUID))) {
                        ps.setString(1, username);
                        ps.setString(2, uniqueId.toString());
                        ps.execute();
                    }
                }
            }
        }

        PlayerSaveResultImpl result = PlayerSaveResultImpl.determineBaseResult(username, oldUsername);

        Set<UUID> conflicting = new HashSet<>();
        try (Connection c = this.connectionFactory.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYER_SELECT_ALL_UUIDS_BY_USERNAME))) {
                ps.setString(1, username);
                ps.setString(2, uniqueId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        conflicting.add(UUID.fromString(rs.getString("uuid")));
                    }
                }
            }
        }

        if (!conflicting.isEmpty()) {
            // remove the mappings for conflicting uuids
            try (Connection c = this.connectionFactory.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYER_DELETE_ALL_UUIDS_BY_USERNAME))) {
                    ps.setString(1, username);
                    ps.setString(2, uniqueId.toString());
                    ps.execute();
                }
            }
            result = result.withOtherUuidsPresent(conflicting);
        }

        return result;
    }

    @Override
    public void deletePlayerData(UUID uniqueId) throws SQLException {
        try (Connection c = this.connectionFactory.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYER_DELETE))) {
                ps.setString(1, uniqueId.toString());
                ps.execute();
            }
        }
    }

    @Override
    public UUID getPlayerUniqueId(String username) throws SQLException {
        username = username.toLowerCase(Locale.ROOT);
        try (Connection c = this.connectionFactory.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYER_SELECT_UUID_BY_USERNAME))) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return UUID.fromString(rs.getString("uuid"));
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getPlayerName(UUID uniqueId) throws SQLException {
        try (Connection c = this.connectionFactory.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYER_SELECT_USERNAME_BY_UUID))) {
                ps.setString(1, uniqueId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String username = rs.getString("username");
                        if (username != null && !username.equals("null")) {
                            return username;
                        }
                    }
                }
            }
        }
        return null;
    }

    private SqlPlayerData selectPlayerData(Connection c, UUID user) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYER_SELECT_BY_UUID))) {
            ps.setString(1, user.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new SqlPlayerData(rs.getString("username"));
                } else {
                    return null;
                }
            }
        }
    }

    private Map<UUID, SqlPlayerData> selectPlayerData(Connection c, Set<UUID> users) throws SQLException {
        Map<UUID, SqlPlayerData> map = new HashMap<>();

        try (Statement s = c.createStatement()) {
            String sql = createUserSelectWhereClause(PLAYER_SELECT_BY_UUID_MULTIPLE, users);
            try (ResultSet rs = s.executeQuery(sql)) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    SqlPlayerData data = new SqlPlayerData(
                            rs.getString("username")
                    );
                    map.put(uuid, data);
                }
            }
        }

        return map;
    }

    private String createUserSelectWhereClause(String baseQuery, Set<UUID> users) {
        String param = users.stream()
                .map(uuid -> "'" + uuid + "'")
                .collect(Collectors.joining(",", "uuid IN (", ")"));

        // we don't want to use preparedstatements because the parameter length is variable
        // safe to do string concat/replacement because the UUID.toString value isn't injectable
        return this.statementProcessor.apply(baseQuery) + param;
    }

    private void insertPlayerData(Connection c, UUID user, SqlPlayerData data) throws SQLException {
        // TODO: implement this method in the future
//        boolean hasPrimaryGroupSaved;
//        try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYER_SELECT_PRIMARY_GROUP_BY_UUID))) {
//            ps.setString(1, user.toString());
//            try (ResultSet rs = ps.executeQuery()) {
//                hasPrimaryGroupSaved = rs.next();
//            }
//        }
//
//        if (hasPrimaryGroupSaved) {
//            // update
//            try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYER_UPDATE_PRIMARY_GROUP_BY_UUID))) {
//                ps.setString(1, data.primaryGroup);
//                ps.setString(2, user.toString());
//                ps.execute();
//            }
//        } else {
//            // insert
//            try (PreparedStatement ps = c.prepareStatement(this.statementProcessor.apply(PLAYER_INSERT))) {
//                ps.setString(1, user.toString());
//                ps.setString(2, data.username);
//                ps.setString(3, data.primaryGroup);
//                ps.execute();
//            }
//        }
    }

    private static boolean tableExists(Connection connection, String table) throws SQLException {
        try (ResultSet rs = connection.getMetaData().getTables(connection.getCatalog(), null, "%", null)) {
            while (rs.next()) {
                if (rs.getString(3).equalsIgnoreCase(table)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class SqlPlayerData {
        private final String username;

        SqlPlayerData(String username) {
            this.username = username;
        }
    }
}