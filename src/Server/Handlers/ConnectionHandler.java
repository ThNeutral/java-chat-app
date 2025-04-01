package Server.Handlers;

import Common.Message;
import Server.Services.AuthService;
import Server.Services.SenderService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Hashtable;
import java.util.UUID;

public class ConnectionHandler implements Runnable, SenderService.ISender {
    private final Socket socket;
    private final SenderService senderService;
    private final AuthService authService;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private UUID token;
    public ConnectionHandler(Socket socket, SenderService senderService, AuthService authService) {
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Cannot get streams of client. "  + e.getMessage());
            try {
                socket.close();
            } catch (IOException ex) {
                System.out.println("Failed to close client socket. " + ex.getMessage());
            }
            throw new RuntimeException(e);
        }

        this.socket = socket;
        this.senderService = senderService;
        this.authService = authService;
    }
    @Override
    public void run() {
        loop();
        exit();
    }

    private void loop() {
        String incoming;
        this.senderService.addSender(this);

        try {
            writer.println(Message.RequestName());
            while (true) {
                incoming = reader.readLine();
                if (incoming == null) return;
                var message = Message.parse(incoming);

                if (message.type != Message.MessageType.RESPONSE_NAME) {
                    writer.println(Message.Error("Expected first message to be of type RESPONSE_NAME"));
                    writer.println(Message.Error(incoming));
                    continue;
                }

                token = authService.add(message.payload);
                var entry = authService.getUserEntry(token);
                var headers = new Hashtable<String, String>();

                headers.put(Message.NAME_HEADER, entry.name);
                headers.put(Message.IDENTIFIER_HEADER, entry.identifier);

                var outgoing = Message.GrantName(headers, token.toString());
                writer.println(outgoing);

                break;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        while (true) {
            try {
                incoming = reader.readLine();
                if (incoming == null) {
                    break;
                }

                Message message;
                try {
                    message = Message.parse(incoming);
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                    continue;
                }

                switch (message.type) {
                    case SERVER_MESSAGE:
                        System.out.println("Invalid message type SERVER_MESSAGE");
                        break;

                    case CLIENT_MESSAGE:
                        var headers = new Hashtable<String, String>();
                        var userEntry = authService.getUserEntry(UUID.fromString(message.headers.get(Message.TOKEN_HEADER)));
                        headers.put(Message.NAME_HEADER, userEntry.name);
                        headers.put(Message.IDENTIFIER_HEADER, userEntry.identifier);
                        var outgoing = Message.ServerMessage(headers, message.payload);
                        senderService.broadcast(outgoing);
                        break;
                }

            } catch (IOException e) {
                System.out.println("Failed to read line. " + e.getMessage());
                break;
            }
        }
    }

    private void exit() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Failed to close socket. " + e.getMessage());
        }

        authService.remove(token);
        senderService.removeSender(this);
    }

    @Override
    public void send(String message) {
        writer.println(message);
    }
}
