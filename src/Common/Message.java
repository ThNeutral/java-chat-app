package Common;

import Server.AuthService.AuthService;

import java.util.*;

public class Message {
    private static final String DELIM = "///";

    public static class InvalidMessageException extends Exception {
        public InvalidMessageException(String message) {
            super(message);
        }
    }

    public static Message ServerIsFull() {
        return new Message(MessageType.SERVER_IS_FULL, new HashMap<>(), "Server is full");
    }

    public static Message Token(UUID token, String name) {
        var headers = new HashMap<String, String>();
        headers.put(AuthService.NAME_HEADER, name);
        headers.put(AuthService.AUTHORIZATION_HEADER, token.toString());
        return new Message(MessageType.TOKEN, headers, "");
    }

    public static Message NameResponse(String name) {
        return new Message(MessageType.NAME_RESPONSE, new HashMap<>(), name);
    }

    public static Message NameRequest() {
        return new Message(MessageType.NAME_REQUEST, new HashMap<>(), "");
    }

    public static Message NormalMessage(String payload) {
        return NormalMessage(payload, new HashMap<>());
    }

    public static Message NormalMessage(String payload, HashMap<String, String> headers) {
        return new Message(MessageType.NORMAL_MESSAGE, headers, payload);
    }

    public static Message ParseMessage(String str) throws InvalidMessageException {
        if (str == null || str.isEmpty()) {
            throw new InvalidMessageException("Input string cannot be null or empty");
        }

        String[] parts = str.split(DELIM, -1);
        if (parts.length < 2) {
            throw new InvalidMessageException("Invalid message format");
        }

        MessageType type = MessageType.valueOf(parts[0]);
        String payload = parts[parts.length - 1];

        var headers = new HashMap<String, String>();

        for (int i = 1; i < parts.length - 1; i++) {
            String[] headerParts = parts[i].split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            }
        }

        return new Message(type, headers, payload);
    }

    public enum MessageType {
        SERVER_IS_FULL,
        NORMAL_MESSAGE,
        TOKEN,
        NAME_RESPONSE,
        NAME_REQUEST
    }
    public MessageType type;
    public HashMap<String, String> headers;
    public String payload;
    public Message(MessageType type, HashMap<String, String> headers, String payload) {
        this.type = type;
        this.headers = headers;
        this.payload = payload;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.name());

        if (headers != null && !headers.isEmpty()) {
            for (var key : headers.keySet()) {
                var value = headers.get(key);
                sb.append(DELIM).append(key).append(": ").append(value);
            }
        }

        sb.append(DELIM).append(payload);
        return sb.toString();
    }
}
