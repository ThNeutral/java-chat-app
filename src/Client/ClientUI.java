package Client;

import Common.Message;
import Server.Services.AuthService;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.UUID;
import java.util.function.Consumer;

class ClientUI {
    private JFrame frame;
    private JPanel panel;
    private JTextPane textPane;
    private JTextField textField;
    private JButton sendButton;
    private StyledDocument doc;

    private int ethemeralLines = 0;
    private boolean isAuthenticated = false;

    private Consumer<Message> sender;
    public void addSender(Consumer<Message> sender) {
        this.sender = sender;
    }

    public void render() {
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setPreferredSize(new Dimension(50 * 7, 20 * 16));
        doc = textPane.getStyledDocument();

        textField = new JTextField(30);
        textField.setHorizontalAlignment(JTextField.CENTER);

        sendButton = new JButton("Send");
        sendButton.addActionListener(this::onSend);

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
    }

    public void onInfo(String info) {
        removeEthemeralText();
        appendText("INFO: " + info, Color.black, true);
    }

    public void onMessage(Message message) {
        removeEthemeralText();
        var name = message.headers.get(Message.NAME_HEADER);
        var identifier = message.headers.get(Message.IDENTIFIER_HEADER);
        appendText((name != null ? name + "-" + identifier : "???") + ": ", Color.green, true);
        appendText(message.payload, Color.black, true);
    }

    public void onError(String errorMessage) {
        removeEthemeralText();
        appendText(errorMessage, Color.red, true);
    }

    public void onRequestName(Void unused) {
        addEthemeralText("Enter your display name", Color.blue);
    }

    public void onGrantName(AuthService.UserEntry userEntry) {
        removeEthemeralText();
        appendText("Your name: " + userEntry.name, Color.black, true);
        appendText("Your identifier: " + userEntry.identifier, Color.black, true);
        isAuthenticated = true;
    }

    private void onSend(ActionEvent unused) {
        var string = textField.getText();
        if (string.isEmpty()) {
            addEthemeralText("Cannot send empty message", Color.red);
            return;
        }

        removeEthemeralText();
        Message message;
        if (isAuthenticated) {
            message = Message.ClientMessage(string);
        } else {
            message = Message.ResponseName(string);
        }
        sender.accept(message);

        textField.setText("");
    }

    private void addEthemeralText(String text, Color color) {
        this.ethemeralLines += text.split("\n").length + 1;
        appendText(text, color, true);
    }

    private void removeEthemeralText() {
        try {
            int endOffset = doc.getLength();
            int startOffset = endOffset;
            int linesToRemove = this.ethemeralLines;

            while (linesToRemove > 0 && startOffset > 0) {
                startOffset--;
                if (doc.getText(startOffset, 1).equals("\n")) {
                    linesToRemove--;
                }
            }

            doc.remove(startOffset, endOffset - startOffset);

            this.ethemeralLines = 0;
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void appendText(String text, Color color, boolean newLine) {
        var style = textPane.addStyle("ColorStyle", null);
        StyleConstants.setForeground(style, color);
        try {
            var lineBreak = newLine ? "\n" : "";
            doc.insertString(doc.getLength(), text + lineBreak, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
