package Client;

import MessageService.Message;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientSocket {
    private final InetSocketAddress address;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private Consumer<Void> onConnect;
    private Consumer<Void> onDisconnect;
    private Consumer<Message> onMessage;
    private Consumer<String> onError;
    private Consumer<String> onInfo;

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
    private void HandleReadSocket() {
        while (true) {
            try {
                var string = reader.readLine();
                var message = Message.ParseMessage(string);
                onMessage.accept(message);
                if (message.type == Message.MessageType.SERVER_IS_FULL) break;
            } catch (IOException | Message.InvalidMessageException e) {
                onError.accept(e.getMessage());
            }
        }
    }
    public void WriteMessage(String message) {
        writer.println(Message.NormalMessage(message).toString());
    }
}
