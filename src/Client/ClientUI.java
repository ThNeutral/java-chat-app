package Client;

import MessageService.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ClientUI {
    private final ClientSocket socket;
    private final JFrame frame;
    private final JPanel panel;
    private final JTextArea textArea;
    private final JTextField textField;
    private final JButton sendButton;

    public ClientUI(ClientSocket socket) {
        this.socket = socket;
        socket.addOnConnect(this::onConnect);
        socket.addOnDisconnect(this::onDisconnect);
        socket.addOnMessage(this::onMessage);
        socket.addOnError(this::onError);
        socket.addOnInfo(this::onInfo);

        textArea = new JTextArea(20, 50);
        textArea.setEditable(false);

        textField = new JTextField(30);
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setEnabled(false);

        sendButton = new JButton("Send");
        sendButton.setEnabled(false);
        sendButton.addActionListener(this::sendHandler);

        panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.add(textField);
        panel.add(sendButton);

        frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);

        new Thread(socket::Connect).start();
    }
    private void onConnect(Void unused) {
        textField.setEnabled(true);
        sendButton.setEnabled(true);
        textArea.append("Connected!\n");
    }
    private void onDisconnect(Void unused) {
        textField.setEnabled(false);
        sendButton.setEnabled(false);
        textArea.append("Disconnected!\n");
    }
    private void onMessage(Message message) {
        textArea.append(message.toString() + "\n");
    }
    private void onError(String errorMessage) {
        textArea.append(errorMessage + "\n");
    }
    private void onInfo(String info) {
        textArea.append(info + "\n");
    }
    private void sendHandler(ActionEvent e) {
        var text = textField.getText();
        if (!text.isEmpty()) {
            socket.WriteMessage(text);
            textField.setText("");
        }
    }
}