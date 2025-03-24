package Server.AuthService;

import Common.Hash;

import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;

public class AuthService {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String NAME_HEADER = "NAME";
    private final HashMap<UUID, String> tokens;
    private final int capacity;
    public AuthService(int capacity) {
        this.tokens = new HashMap<>(capacity);
        this.capacity = capacity;
    }
    public UUID Register(String name) {
        if (tokens.size() >= capacity) throw new IllegalStateException("Exceeded capacity of AuthService");
        var uuid = UUID.randomUUID();
        name += "-" + Hash.GetSHA256String(uuid.toString(), 8);
        tokens.put(uuid, name);
        return uuid;
    }
    public String GetNameFromUUID(UUID uuid) {
        return tokens.get(uuid);
    }
    public void Unregister(UUID uuid) {
        if (tokens.isEmpty()) throw new IllegalStateException("Tried to remove token from empty AuthService");
        tokens.remove(uuid);
    }
}
