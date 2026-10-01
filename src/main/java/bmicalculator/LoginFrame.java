package bmicalculator;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LoginFrame extends JFrame {
    // These fields are created by the GUI Designer from LoginFrame.form
    private JPanel rootPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;

    private final Map<String, String> env = readEnvFile(".env");

    public LoginFrame() {
        super("Login");
        if (rootPanel == null) {
            throw new IllegalStateException(
                    "GUI form not initialised. Rebuild the project (Build > Rebuild Project).");
        }
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(rootPanel);

        messageLabel.setForeground(Color.RED);
        loginButton.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(loginButton);

        pack();
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        String user = env.get("APP_USERNAME");
        String pass = env.get("APP_PASSWORD");
        if (user == null || pass == null) {
            messageLabel.setText(".env file missing or incomplete");
            return;
        }
        String enteredPass = new String(passwordField.getPassword());
        if (user.equals(usernameField.getText().trim()) && pass.equals(enteredPass)) {
            new BMIFrame().setVisible(true);
            dispose();
        } else {
            messageLabel.setText("Wrong username or password");
            passwordField.setText("");
        }
    }

    private static Map<String, String> readEnvFile(String fileName) {
        Map<String, String> map = new HashMap<>();
        try {
            for (String line : Files.readAllLines(Paths.get(fileName))) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) continue;
                int i = line.indexOf('=');
                map.put(line.substring(0, i).trim(), line.substring(i + 1).trim());
            }
        } catch (IOException e) {
            // file not found: map stays empty, login shows a message
        }
        return map;
    }
}
