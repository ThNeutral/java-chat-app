package Client;

import Common.Message;
import Server.AuthService.AuthService;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class ClientUI {
    private final ClientSocket socket;
    private final JFrame frame;
    private final JPanel panel;
    private final JTextPane textPane;
    private final JTextField textField;
    private final JButton sendButton;
    private boolean hasRequestedName;
    private StyledDocument doc;

    public ClientUI(ClientSocket socket) {
        this.socket = socket;
        socket.addOnConnect(this::onConnect);
        socket.addOnDisconnect(this::onDisconnect);
        socket.addOnMessage(this::onMessage);
        socket.addOnError(this::onError);
        socket.addOnInfo(this::onInfo);
        socket.addOnResponseName(this::onResponseName);
        socket.addOnRequestName(this::onRequestName);

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setPreferredSize(new Dimension(50 * 7, 20 * 16));
        doc = textPane.getStyledDocument();

        textField = new JTextField(30);
        textField.setHorizontalAlignment(JTextField.CENTER);

        sendButton = new JButton("Send");
        sendButton.addActionListener(this::sendHandler);

        panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.add(textField);
        panel.add(sendButton);

        frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(textPane), BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);

        new Thread(socket::Connect).start();
    }

    private void onResponseName(String name) {
        appendText("Your display name is '" + name + "'", Color.BLACK);
        hasRequestedName = true;
    }

    private void onConnect(Void unused) {
        textField.setEnabled(true);
        sendButton.setEnabled(true);
        appendText("Connected!", Color.BLACK);
    }

    private void onRequestName(Void unused) {
        appendText("Write display name.", Color.BLACK);
    }

    private void onDisconnect(Void unused) {
        textField.setEnabled(false);
        sendButton.setEnabled(false);
        appendText("Disconnected!", Color.BLACK);
    }

    private void onMessage(Message message) {
        appendText(message.headers.get(AuthService.NAME_HEADER) + ": " + message.payload, Color.BLACK);
    }

    private void onError(String errorMessage) {
        appendText(errorMessage, Color.RED);
    }

    private void onInfo(String info) {
        appendText(info, Color.BLACK);
    }

    private void appendText(String text, Color color) {
        var style = textPane.addStyle("ColorStyle", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), text + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void sendHandler(ActionEvent e) {
        var text = textField.getText();
        if (!text.isEmpty()) {
            var headers = new HashMap<String, String>();
            Message message;
            if (!hasRequestedName) {
                message = Message.NameResponse(text);
            } else {
                message = Message.NormalMessage(text, headers);
            }
            socket.WriteMessage(message);
            textField.setText("");
        }
    }
}