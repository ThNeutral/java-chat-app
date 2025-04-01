package Client;

import Common.Message;
import Server.Services.AuthService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.function.Consumer;

class ClientSocket {
    private final InetSocketAddress serverInetAddress;
    private BufferedReader reader;
    private PrintWriter writer;

    private UUID token;
    private AuthService.UserEntry userEntry;

    private Consumer<String> onInfo;
    public void addOnInfo(Consumer<String> onInfo) {
        this.onInfo = onInfo;
    }

    private Consumer<Message> onMessage;
    public void addOnMessage(Consumer<Message> onMessage) {
        this.onMessage = onMessage;
    }

    private Consumer<String> onError;
    public void addOnError(Consumer<String> onError) {
        this.onError = onError;
    }

    private Consumer<Void> onRequestName;
    public void addOnRequestName(Consumer<Void> onRequestName) {
        this.onRequestName = onRequestName;
    }

    private Consumer<AuthService.UserEntry> onGrantName;
    public void addOnGrantName(Consumer<AuthService.UserEntry> onGrantName) {
        this.onGrantName = onGrantName;
    }

    public ClientSocket(String serverIP) {
        var parts = serverIP.split(":");
        if (parts.length != 2) throw new IllegalArgumentException(serverIP + " is not a valid socket address");
        int port;
        try {
            port = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(parts[1] + " is not a valid socket port");
        }
        serverInetAddress = new InetSocketAddress(parts[0], port);
    }

    public void send(Message message) {
        if (token != null) {
            message.headers.put(Message.TOKEN_HEADER, token.toString());
        }
        writer.println(message.toString());
    }

    public void connect() {
        try (var socket = new Socket()) {
            socket.connect(serverInetAddress);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            String incoming;
            while (true) {
                incoming = reader.readLine();
                if (incoming == null) {
                    break;
                }

                Message message;
                try {
                    message = Message.parse(incoming);
                } catch (IllegalArgumentException e) {
                    onError.accept(e.getMessage());
                    onError.accept(incoming);
                    continue;
                }

                switch (message.type) {
                    case SERVER_MESSAGE:
                        onMessage.accept(message);
                        break;

                    case CLIENT_MESSAGE:
                        onError.accept("Invalid message type CLIENT_MESSAGE");
                        break;

                    case REQUEST_NAME:
                        onRequestName.accept(null);
                        break;

                    case GRANT_NAME:
                        try {
                            token = UUID.fromString(message.payload);
                            var identifier = message.headers.get(Message.IDENTIFIER_HEADER);
                            var name = message.headers.get(Message.NAME_HEADER);
                            userEntry = new AuthService.UserEntry(identifier, name);
                            onGrantName.accept(userEntry);
                        } catch (Exception e) {
                            onError.accept(e.getMessage());
                        }
                        break;

                    case RESPONSE_NAME:
                        onError.accept("Invalid message type RESPONSE_NAME");
                        break;

                    case ERROR:
                        onError.accept(message.payload);
                        break;
                }
            }
        } catch (IOException e) {

        }
    }
}
