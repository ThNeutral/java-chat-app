package Server.AuthService;

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
        var uuid = UUID.randomUUID();
        tokens.put(uuid, name);
        return uuid;
    }
    public String GetNameFromUUID(UUID uuid) {
        return tokens.get(uuid);
    }
    public void Unregister(UUID uuid) {
        tokens.remove(uuid);
    }
}
