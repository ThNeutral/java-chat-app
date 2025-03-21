package Server.MessageService;

import Common.Message;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MessageService {
    private final ThreadPoolExecutor pool;
    private final List<Socket> connections;
    public MessageService(int connectionsSize) {
        pool = new ThreadPoolExecutor(
                connectionsSize,
                connectionsSize * 2,
                1, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(connectionsSize * 10)
        );
        this.connections = new ArrayList<>(connectionsSize);
    }
    public void Add(Socket connection) {
        connections.add(connection);
    }
    public void Remove(Socket connection) {
        connections.remove(connection);
    }
    public void Broadcast(Message message) {
        for (var conn : connections) {
            pool.execute(() -> HandleBroadcast(conn, message));
        }
    }
    private void HandleBroadcast(Socket conn, Message message) {
        try {
            var writer = new PrintWriter(conn.getOutputStream(), true);
            writer.println(message.toString());
        } catch (IOException e) {
            System.out.println("Failed to get writer");
            System.out.println("\t" + e.getMessage());
        }
    }
}
