package Client;

public class Client {
    private final ClientUI clientUI;
    private final ClientSocket clientSocket;
    public Client(String serverIP) {
        clientUI = new ClientUI();
        clientSocket = new ClientSocket(serverIP);

        clientUI.addSender(clientSocket::send);

        clientSocket.addOnInfo(clientUI::onInfo);
        clientSocket.addOnMessage(clientUI::onMessage);
        clientSocket.addOnError(clientUI::onError);
        clientSocket.addOnRequestName(clientUI::onRequestName);
        clientSocket.addOnGrantName(clientUI::onGrantName);
    }
    public void start() {
        clientUI.render();
        clientSocket.connect();
    }
}
