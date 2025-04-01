package Server;

import Common.Message;
import Server.Handlers.ConnectionHandler;
import Server.Services.AuthService;
import Server.Services.SenderService;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
    private final ThreadPoolExecutor threadPool;
    private final SenderService senderService;
    private final AuthService authService;
    private final int port;
    private final int capacity;
    public Server(int port, int capacity) {
        this.port = port;
        this.capacity = capacity;
        senderService = new SenderService(capacity);
        authService = new AuthService(capacity);
        threadPool = new ThreadPoolExecutor(
                capacity,
                capacity,
                1, TimeUnit.MINUTES,
                new SynchronousQueue<>(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
    public void serve() {
        try (var server = new ServerSocket(port)) {
            while (true) {
                Socket socket;
                try {
                    socket = server.accept();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    break;
                }
                try {
                    var handler = new ConnectionHandler(socket, senderService, authService);
                    threadPool.execute(handler);
                } catch (RejectedExecutionException e) {
                    var writer = new PrintWriter(socket.getOutputStream(), true);
                    writer.println(Message.Error("Server is full"));
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
