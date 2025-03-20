package MessageService;

public class Message {
    private static final String DELIM = ": ";

    public static class InvalidMessageException extends Exception {
        public InvalidMessageException(String message) {
            super(message);
        }
    }

    public static Message ServerIsFull() {
        return new Message(MessageType.SERVER_IS_FULL, "Server is full");
    }

    public static Message NormalMessage(String payload) {
        return new Message(MessageType.NORMAL_MESSAGE, payload);
    }

    public static Message ParseMessage(String message) throws InvalidMessageException {
        var parts = message.split(DELIM);
        if (parts.length != 2) throw new InvalidMessageException("Tried to parse message with unknown format: " + message);
        MessageType type;
        try {
            type = MessageType.valueOf(parts[0]);
        } catch (IllegalArgumentException e) {
            throw new InvalidMessageException(parts[0] + "is not valid message type");
        }
        return new Message(type, parts[1]);
    }

    public enum MessageType {
        SERVER_IS_FULL,
        NORMAL_MESSAGE
    }
    public MessageType type;
    public String payload;
    public Message(MessageType type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    @Override
    public String toString() {
        return type.name() + DELIM + payload;
    }
}
