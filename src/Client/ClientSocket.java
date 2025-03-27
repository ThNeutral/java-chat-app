package Client;

import Common.Message;
import Server.AuthService.AuthService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class ClientSocket {
    private final InetSocketAddress address;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private UUID token;

    private Consumer<Void> onConnect;
    private Consumer<Void> onDisconnect;
    private Consumer<Message> onMessage;
    private Consumer<String> onError;
    private Consumer<String> onInfo;
    private Consumer<String> onResponseName;
    private Consumer<Void> onRequestName;

    public ClientSocket(String address) {
        try {
            var parts = address.split(":");
            this.address = new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
        } catch (Exception e) {
            throw new IllegalArgumentException(address + " is not an internet address");
        }
    }
    public void Connect() {
        try {
            socket = new Socket();
            socket.connect(address);
            onConnect.accept(null);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            HandleReadSocket();
        } catch (IOException e) {
            onError.accept(e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                onError.accept(e.getMessage());
            }
            onDisconnect.accept(null);
        }
    }
    public void addOnConnect(Consumer<Void> onConnect) {
        this.onConnect = onConnect;
    }
    public void addOnDisconnect(Consumer<Void> onDisconnect) {
        this.onDisconnect = onDisconnect;
    }
    public void addOnMessage(Consumer<Message> onMessage) {
        this.onMessage = onMessage;
    }
    public void addOnError(Consumer<String> onError) {
        this.onError = onError;
    }
    public void addOnInfo(Consumer<String> onInfo) {
        this.onInfo = onInfo;
    }
    public void addOnResponseName(Consumer<String> onResponseName) {
        this.onResponseName = onResponseName;
    }
    public void addOnRequestName(Consumer<Void> onRequestName) {
        this.onRequestName = onRequestName;
    }
    private void HandleReadSocket() {
        while (true) {
            try {
                var string = reader.readLine();
                if (string == null) {
                    onInfo.accept("Server disconnected");
                    break;
                }
                var message = Message.ParseMessage(string);
                if (message.type == Message.MessageType.SERVER_IS_FULL) {
                    onInfo.accept("Server is full.");
                    break;
                }
                if (message.type == Message.MessageType.NAME_REQUEST) {
                    onRequestName.accept(null);
                    continue;
                }
                if (message.type == Message.MessageType.TOKEN) {
                    token = UUID.fromString(message.headers.get(AuthService.AUTHORIZATION_HEADER));
                    onResponseName.accept(message.headers.get(AuthService.NAME_HEADER));
                    continue;
                }
                onMessage.accept(message);
            } catch (IOException | Message.InvalidMessageException e) {
                onError.accept(e.getMessage());
            }
        }
    }
    public void WriteMessage(Message message) {
        if (message.headers == null) {
            message.headers = new HashMap<>();
        }
        if (token != null) {
            message.headers.put(AuthService.AUTHORIZATION_HEADER, token.toString());
        }
        writer.println(message);
    }
}
