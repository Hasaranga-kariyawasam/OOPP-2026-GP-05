package bmicalculator;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);
    private final Map<String, String> env = readEnvFile(".env");

    public LoginFrame() {
        super("Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Username"));
        form.add(usernameField);
        form.add(new JLabel("Password"));
        form.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(loginButton);

        messageLabel.setForeground(Color.RED);

        JPanel root = new JPanel(new BorderLayout(8, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.add(form, BorderLayout.NORTH);
        root.add(messageLabel, BorderLayout.CENTER);
        root.add(loginButton, BorderLayout.SOUTH);
        setContentPane(root);

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

    // Reads KEY=VALUE lines from the .env file
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
            // file not found: map stays empty, login will show a message
        }
        return map;
    }
}
