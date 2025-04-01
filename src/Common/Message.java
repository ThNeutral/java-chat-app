package Common;

import java.util.Hashtable;

public class Message {
    public static final String DELIMETER = "///";
    public static final String HEADER_DELIMETER = ":";

    public static final String NAME_HEADER = "Name";
    public static final String IDENTIFIER_HEADER = "Identifier";
    public static final String TOKEN_HEADER = "Token";

    public static Message parse(String messageStr) throws IllegalArgumentException {
        String[] parts = messageStr.split(DELIMETER);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid message format");
        }

        Message message = new Message();
        message.type = MessageType.valueOf(parts[0]);

        int i = 1;
        while (i < parts.length - 1) {
            if (!parts[i].contains(HEADER_DELIMETER)) {
                break;
            }
            String[] headerParts = parts[i].split(HEADER_DELIMETER, 2);
            message.headers.put(headerParts[0], headerParts[1]);
            i++;
        }

        message.payload = parts[i];
        return message;
    }

    public static Message ServerMessage(String payload) {
        return ServerMessage(new Hashtable<>(), payload);
    }
    public static Message ServerMessage(Hashtable<String, String> headers, String payload) {
        return new Message(MessageType.SERVER_MESSAGE, headers, payload);
    }

    public static Message ClientMessage(String payload) {
        return ClientMessage(new Hashtable<>(), payload);
    }
    public static Message ClientMessage(Hashtable<String, String> headers, String payload) {
        return new Message(MessageType.CLIENT_MESSAGE, headers, payload);
    }

    public static Message RequestName() {
        return RequestName(new Hashtable<>());
    }
    public static Message RequestName(Hashtable<String, String> headers) {
        return new Message(MessageType.REQUEST_NAME, headers, "_");
    }

    public static Message ResponseName(String payload) {
        return ResponseName(new Hashtable<>(), payload);
    }
    public static Message ResponseName(Hashtable<String, String> headers, String payload) {
        return new Message(MessageType.RESPONSE_NAME, headers, payload);
    }

    public static Message GrantName(String payload) {
        return GrantName(new Hashtable<>(), payload);
    }
    public static Message GrantName(Hashtable<String, String> headers, String payload) {
        return new Message(MessageType.GRANT_NAME, headers, payload);
    }

    public static Message Error(String payload) {
        return Error(new Hashtable<>(), payload);
    }
    public static Message Error(Hashtable<String, String> headers, String payload) {
        return new Message(MessageType.ERROR, headers, payload);
    }

    public enum MessageType {
        SERVER_MESSAGE,
        CLIENT_MESSAGE,
        REQUEST_NAME,
        RESPONSE_NAME,
        GRANT_NAME,
        ERROR
    }

    public MessageType type;
    public Hashtable<String, String> headers;
    public String payload;

    public Message() {
        this.headers = new Hashtable<>();
    }

    public Message(MessageType type, Hashtable<String, String> headers, String payload) {
        this.type = type;
        this.headers = headers != null ? headers : new Hashtable<>();
        this.payload = payload;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(type).append(DELIMETER);

        for (var entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(HEADER_DELIMETER).append(entry.getValue()).append(DELIMETER);
        }

        sb.append(payload);

        return sb.toString();
    }
}
