package Server;

import Common.Message;
import Server.AuthService.AuthService;
import Server.MessageService.MessageService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.*;

public class Server {
    private final ThreadPoolExecutor pool;
    private final MessageService messageService;
    private final AuthService authService;
    private final int port;
    public Server(int connectionsSize, int port) {
        pool = new ThreadPoolExecutor(
                connectionsSize,
                connectionsSize,
                1, TimeUnit.MINUTES ,
                new SynchronousQueue<>(),
                new ThreadPoolExecutor.AbortPolicy()
        );
        messageService = new MessageService(connectionsSize);
        authService = new AuthService(connectionsSize);
        this.port = port;
    }
    public void Serve() {
        try (var socket = new ServerSocket(port)) {
            System.out.println("Server is listening on " + socket.getLocalSocketAddress());
            while (true) {
                Socket client = null;
                try {
                    client = socket.accept();
                    pool.execute(new ClientHandler(client));
                } catch (RejectedExecutionException e) {
                    var writer = new PrintWriter(client.getOutputStream(), true);
                    writer.println(Message.ServerIsFull());
                    client.close();
                } catch (Exception e) {
                    System.out.println("Failed to connect to client");
                    System.out.println("\t" + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to create socket");
        }
    }
    public class ClientHandler implements Runnable {
        private final Socket client;
        private final BufferedReader reader;
        private final PrintWriter writer;
        private UUID clientToken;
        public ClientHandler(Socket client) {
            messageService.Add(client);
            this.client = client;
            try {
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                writer = new PrintWriter(client.getOutputStream(), true);
            } catch (IOException e) {
                System.out.println("Failed to get writer and reader");
                System.out.println("\t" + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        @Override
        public void run() {
            try {
                writer.println(Message.NameRequest());
                var incoming = reader.readLine();
                var message = Message.ParseMessage(incoming);
                if (message.type != Message.MessageType.NAME_RESPONSE) throw new Exception();
                clientToken = authService.Register(message.payload);
                writer.println(Message.Token(clientToken, message.payload));
                while ((incoming = reader.readLine()) != null) {
                    try {
                        message = Message.ParseMessage(incoming);
                        if (message.type == Message.MessageType.NORMAL_MESSAGE) {
                            var token = message.headers.get(AuthService.AUTHORIZATION_HEADER);
                            String name;
                            try {
                                var uuid = UUID.fromString(token);
                                name = authService.GetNameFromUUID(uuid);
                                if (name == null) continue;
                            } catch (IllegalArgumentException e) {
                                System.out.println("Received invalid token: " + token);
                                continue;
                            }
                            message.headers.remove(AuthService.AUTHORIZATION_HEADER);
                            message.headers.put(AuthService.NAME_HEADER, name);
                            messageService.Broadcast(message);
                        }
                    } catch (Message.InvalidMessageException e) {
                        System.out.println("Recieved unknown message " + incoming + " from " + client.getInetAddress());
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection error.");
                System.out.println("\t" + e.getMessage());
            } catch (Message.InvalidMessageException e) {
                System.out.println("Bad initial message.");
                System.out.println("\t" + e.getMessage());
            } catch (Exception e) {
            } finally {
                try {
                    messageService.Remove(client);
                    authService.Unregister(clientToken);
                    System.out.println("Client disconnected.");
                    client.close();
                } catch (IOException e) {
                    System.out.println("Failed to close connection.");
                    System.out.println("\t" + e.getMessage());
                }
            }
        }
    }
}
