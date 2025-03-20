import Client.ClientSocket;
import Client.ClientUI;
import Server.Server;

public class CLI {
    public static void main(String[] args) {
        if (args.length != 2) {
            printUsageError("Expected two arguments");
            return;
        }

        switch (args[0]) {
            case "server":
                new Server(3, Integer.parseInt(args[1])).Serve();
                break;
            case "client":
                var socket = new ClientSocket(args[1]);
                var ui = new ClientUI(socket);
                break;
            default:
                printUsageError("Unknown command.");
        }
    }
    private static void printUsageError(String message) {
        System.out.println(message);
        System.out.println("Usage:");
        System.out.println("\tFor server: java CLI server <port>");
        System.out.println("\tFor client: java CLI client <address>");
    }
}