package me.kubbidev.multiversus.storage.implementation.mongodb;

import com.google.common.base.Strings;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import me.kubbidev.multiversus.model.User;
import me.kubbidev.multiversus.plugin.MultiPlugin;
import me.kubbidev.multiversus.storage.StorageMetadata;
import me.kubbidev.multiversus.storage.implementation.StorageImplementation;
import me.kubbidev.multiversus.storage.misc.PlayerSaveResultImpl;
import me.kubbidev.multiversus.storage.misc.StorageCredentials;
import net.multiversus.api.model.PlayerSaveResult;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class MongoStorage implements StorageImplementation {
    private final MultiPlugin plugin;

    private final StorageCredentials configuration;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private final String prefix;
    private final String connectionUri;

    public MongoStorage(MultiPlugin plugin, StorageCredentials configuration, String prefix, String connectionUri) {
        this.plugin = plugin;
        this.configuration = configuration;
        this.prefix = prefix;
        this.connectionUri = connectionUri;
    }

    @Override
    public MultiPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public String getImplementationName() {
        return "MongoDB";
    }

    @Override
    public void init() throws Exception {
        MongoClientOptions.Builder options = MongoClientOptions.builder()
                .uuidRepresentation(UuidRepresentation.JAVA_LEGACY);

        if (!Strings.isNullOrEmpty(this.connectionUri)) {
            this.mongoClient = new MongoClient(new MongoClientURI(this.connectionUri, options));
        } else {
            MongoCredential credential = null;
            if (!Strings.isNullOrEmpty(this.configuration.getUsername())) {
                credential = MongoCredential.createCredential(
                        this.configuration.getUsername(),
                        this.configuration.getDatabase(),
                        Strings.isNullOrEmpty(this.configuration.getPassword()) ? new char[0] : this.configuration.getPassword().toCharArray()
                );
            }

            String[] addressSplit = this.configuration.getAddress().split(":");
            String host = addressSplit[0];
            int port = addressSplit.length > 1 ? Integer.parseInt(addressSplit[1]) : 27017;
            ServerAddress address = new ServerAddress(host, port);

            if (credential == null) {
                this.mongoClient = new MongoClient(address, options.build());
            } else {
                this.mongoClient = new MongoClient(address, credential, options.build());
            }
        }

        this.database = this.mongoClient.getDatabase(this.configuration.getDatabase());
    }

    @Override
    public void shutdown() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    @Override
    public StorageMetadata getMeta() {
        StorageMetadata metadata = new StorageMetadata();

        boolean success = true;
        long start = System.currentTimeMillis();

        try {
            this.database.runCommand(new Document("ping", 1));
        } catch (Exception e) {
            success = false;
        }

        if (success) {
            int duration = (int) (System.currentTimeMillis() - start);
            metadata.ping(duration);
        }

        metadata.connected(success);
        return metadata;
    }

    @Override
    public User loadUser(UUID uniqueId, String username) throws Exception {
        User user = this.plugin.getUserManager().getOrMake(uniqueId, username);
        MongoCollection<Document> c = this.database.getCollection(this.prefix + "users");
        try (MongoCursor<Document> cursor = c.find(new Document("_id", user.getUniqueId())).iterator()) {
            if (cursor.hasNext()) {
                // User exists, let's load.
                Document d = cursor.next();
                String name = d.getString("name");

                user.setUsername(name, true);

                boolean updatedUsername = user.getUsername().isPresent() && (name == null || !user.getUsername().get().equalsIgnoreCase(name));
                if (updatedUsername) {
                    c.replaceOne(new Document("_id", user.getUniqueId()), userToDoc(user));
                }
            }
        }
        return user;
    }

    @Override
    public Map<UUID, User> loadUsers(Set<UUID> uniqueIds) throws Exception {
        // make this a bulk search?
        Map<UUID, User> map = new HashMap<>();
        for (UUID uniqueId : uniqueIds) {
            map.put(uniqueId, loadUser(uniqueId, null));
        }
        return map;
    }

    @Override
    public void saveUser(User user) throws Exception {
        MongoCollection<Document> c = this.database.getCollection(this.prefix + "users");
        c.replaceOne(new Document("_id", user.getUniqueId()), userToDoc(user), new ReplaceOptions().upsert(true));
    }

    @Override
    public Set<UUID> getUniqueUsers() throws Exception {
        Set<UUID> uuids = new HashSet<>();
        MongoCollection<Document> c = this.database.getCollection(this.prefix + "users");
        try (MongoCursor<Document> cursor = c.find().iterator()) {
            while (cursor.hasNext()) {
                try {
                    uuids.add(getDocumentId(cursor.next()));
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        }
        return uuids;
    }

    @Override
    public PlayerSaveResult savePlayerData(UUID uniqueId, String username) throws Exception {
        username = username.toLowerCase(Locale.ROOT);
        MongoCollection<Document> c = this.database.getCollection(this.prefix + "uuid");

        // find any existing mapping
        String oldUsername = getPlayerName(uniqueId);

        // do the insert
        if (!username.equalsIgnoreCase(oldUsername)) {
            c.replaceOne(new Document("_id", uniqueId), new Document("_id", uniqueId).append("name", username), new ReplaceOptions().upsert(true));
        }

        PlayerSaveResultImpl result = PlayerSaveResultImpl.determineBaseResult(username, oldUsername);

        Set<UUID> conflicting = new HashSet<>();
        try (MongoCursor<Document> cursor = c.find(new Document("name", username)).iterator()) {
            while (cursor.hasNext()) {
                conflicting.add(getDocumentId(cursor.next()));
            }
        }
        conflicting.remove(uniqueId);

        if (!conflicting.isEmpty()) {
            // remove the mappings for conflicting uuids
            c.deleteMany(Filters.or(conflicting.stream().map(u -> Filters.eq("_id", u)).collect(Collectors.toList())));
            result = result.withOtherUuidsPresent(conflicting);
        }

        return result;
    }

    @Override
    public void deletePlayerData(UUID uniqueId) throws Exception {
        MongoCollection<Document> c = this.database.getCollection(this.prefix + "uuid");
        c.deleteMany(Filters.eq("_id", uniqueId));
    }

    @Override
    public @Nullable UUID getPlayerUniqueId(String username) throws Exception {
        MongoCollection<Document> c = this.database.getCollection(this.prefix + "uuid");
        Document doc = c.find(new Document("name", username.toLowerCase(Locale.ROOT))).first();
        if (doc != null) {
            return getDocumentId(doc);
        }
        return null;
    }

    @Override
    public @Nullable String getPlayerName(UUID uniqueId) throws Exception {
        MongoCollection<Document> c = this.database.getCollection(this.prefix + "uuid");
        Document doc = c.find(new Document("_id", uniqueId)).first();
        if (doc != null) {
            String username = doc.get("name", String.class);
            if (username != null && !username.equals("null")) {
                return username;
            }
        }
        return null;
    }

    private static UUID getDocumentId(Document document) {
        Object id = document.get("_id");
        if (id instanceof UUID) {
            return (UUID) id;
        } else if (id instanceof String) {
            return UUID.fromString((String) id);
        } else {
            throw new IllegalArgumentException("Unknown id type: " + id.getClass().getName());
        }
    }

    private static Document userToDoc(User user) {
        return new Document("_id", user.getUniqueId())
                .append("name", user.getUsername().orElse("null"));
    }
}