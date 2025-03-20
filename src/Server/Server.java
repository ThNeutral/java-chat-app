package Server;

import MessageService.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class Server {
    private final ThreadPoolExecutor pool;
    private final int port;
    public Server(int connectionSize, int port) {
        var queue = new SynchronousQueue<Runnable>();
        pool = new ThreadPoolExecutor(
                connectionSize,
                connectionSize,
                1, TimeUnit.MINUTES ,
                queue,
                new ThreadPoolExecutor.AbortPolicy()
        );
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
        public ClientHandler(Socket client) {
            this.client = client;
            try {
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("Client says: " + message);
                }
            } catch (IOException e) {
                System.out.println("Connection error.");
                System.out.println("\t" + e.getMessage());
            } finally {
                try {
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
