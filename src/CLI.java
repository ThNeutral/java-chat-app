import Client.Client;
import Server.Server;

public class CLI {
    public static void main(String[] args) {
        if (args.length < 1) {
            printUsageError("Must provide app type.");
            return;
        }

        switch (args[0]) {
            case "server":
                handleServer(args);
                break;
            case "client":
                handleClient(args);
                break;
            default:
                printUsageError("Unknown command.");
        }
    }
    private static void handleServer(String[] args) {
        if (args.length != 2 && args.length != 3) {
            printUsageError("Invalid number of arguments to run server");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            printUsageError(e.getMessage());
            return;
        }

        int capacity = 3;
        if (args.length == 3) {
            try {
                capacity = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                printUsageError(e.getMessage());
                return;
            }
        }

        System.out.println("Listening on :" + port);
        try {
            new Server(port, capacity).serve();
        } catch (IllegalArgumentException e) {
            printUsageError(e.getMessage());
        }
    }
    private static void handleClient(String[] args) {
        if (args.length != 2) {
            printUsageError("Invalid number of arguments to run client");
            return;
        }

        try {
            new Client(args[1]).start();
        } catch (IllegalArgumentException e) {
            printUsageError(e.getMessage());
        }
    }
    private static void printUsageError() {
        printUsageError("");
    }
    private static void printUsageError(String message) {
        if (!message.isEmpty()) {
            System.out.println(message);
        }
        System.out.println("Usage:");
        System.out.println("\tFor server: java CLI server <port (int)> <size (int) (optional) (default=3)>");
        System.out.println("\tFor client: java CLI client <address (host:port)>");
    }
}