package Server.Services;

import Common.Hash;

import java.util.Hashtable;
import java.util.UUID;

public class AuthService {
    public static class UserEntry {
        public String identifier;
        public String name;
        public UserEntry(String identifier, String name) {
            this.identifier = identifier;
            this.name = name;
        }
    }

    private final Hashtable<UUID, UserEntry> tokens;
    private final int capacity;

    public AuthService(int capacity) {
        tokens = new Hashtable<>(capacity);
        this.capacity = capacity;
    }

    public UUID add(String name) {
        if (tokens.size() >= capacity) throw new IllegalStateException("AuthService is full");
        var token = UUID.randomUUID();
        var identifier = Hash.GetSHA256String(token.toString(), 8);
        var entry = new UserEntry(identifier, name);
        tokens.put(token, entry);
        return token;
    }

    public UserEntry getUserEntry(UUID token) {
        var entry = tokens.get(token);
        if (entry == null) throw new IllegalStateException("Token is not recognised");
        return entry;
    }

    public void remove(UUID token) {
        if (tokens.isEmpty()) throw new IllegalStateException("AuthService is empty");
        tokens.remove(token);
    }
}
