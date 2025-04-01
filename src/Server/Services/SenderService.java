package Server.Services;

import Common.Message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SenderService {
    public interface ISender {
        void send(String message);
    }

    private final ThreadPoolExecutor poolExecutor;
    private final HashSet<ISender> senders;
    private final int capacity;
    public SenderService(int capacity) {
        poolExecutor = new ThreadPoolExecutor(
                capacity,
                capacity * 2,
                10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(capacity * 2)
        );
        senders = new HashSet<>(capacity);
        this.capacity = capacity;
    }

    public void addSender(ISender sender) {
        if (senders.size() >= capacity) throw new IllegalStateException("SenderService is full");
        senders.add(sender);
    }

    public void broadcast(Message message) {
        var string = message.toString();
        for (var sender : senders) {
            poolExecutor.execute(() -> sender.send(string));
        }
    }

    public void removeSender(ISender sender) {
        if (senders.isEmpty()) throw new IllegalStateException("SenderService is empty");
        senders.remove(sender);
    }
}
