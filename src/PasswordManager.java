import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.security.Key;
import java.util.List;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class PasswordManager extends JFrame {
    private static final String FILE_NAME = "credentials.txt";
    private static final String SECRET_KEY = "1234567890123456"; // AES-128 key

    private JTextField addWebsiteField, addUsernameField;
    private JPasswordField addPasswordField;

    private JCheckBox darkModeToggle;
    private boolean isDarkMode = false;
    private Color lightBG = Color.WHITE;
    private Color darkBG = new Color(45, 45, 45);
    private Color lightFG = Color.BLACK;
    private Color darkFG = Color.WHITE;

    private JTextField searchField;
    private JTextArea viewArea, searchArea, deleteArea;
    private JTextField deleteIndexField;

    public PasswordManager() {
        setTitle("Password Manager");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("➕ Add", createAddTab());
        tabs.addTab("👁️ View All", createViewTab());
        tabs.addTab("🔍 Search", createSearchTab());
        tabs.addTab("🗑️ Delete", createDeleteTab());

        darkModeToggle = new JCheckBox("🌙 Dark Mode");
        darkModeToggle.addActionListener(e -> toggleDarkMode());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(darkModeToggle, BorderLayout.EAST);
        topPanel.add(tabs, BorderLayout.CENTER);
        add(topPanel);
    }

    private void toggleDarkMode() {
        isDarkMode = darkModeToggle.isSelected();
        SwingUtilities.invokeLater(() -> {
            applyThemeToComponents(this.getContentPane());
            repaint();
        });
    }

    private void applyThemeToComponents(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel || comp instanceof JScrollPane || comp instanceof JTabbedPane) {
                comp.setBackground(isDarkMode ? darkBG : lightBG);
                comp.setForeground(isDarkMode ? darkFG : lightFG);
                if (comp instanceof Container) applyThemeToComponents((Container) comp);
            } else if (comp instanceof JTextArea || comp instanceof JTextField || comp instanceof JPasswordField) {
                comp.setBackground(isDarkMode ? new Color(65, 65, 65) : Color.WHITE);
                comp.setForeground(isDarkMode ? darkFG : lightFG);
                comp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            } else if (comp instanceof JLabel || comp instanceof JButton || comp instanceof JCheckBox) {
                comp.setForeground(isDarkMode ? darkFG : lightFG);
                comp.setBackground(isDarkMode ? darkBG : lightBG);
            }
        }
    }

    private JPanel createAddTab() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Website:"));
        addWebsiteField = new JTextField();
        panel.add(addWebsiteField);

        panel.add(new JLabel("Username:"));
        addUsernameField = new JTextField();
        panel.add(addUsernameField);

        panel.add(new JLabel("Password:"));
        addPasswordField = new JPasswordField();
        panel.add(addPasswordField);

        JLabel strengthLabel = new JLabel("Password Strength: ");
        panel.add(strengthLabel);
        panel.add(new JLabel());

        addPasswordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }

            void updateStrength() {
                String password = new String(addPasswordField.getPassword());
                strengthLabel.setText("Strength: " + evaluateStrength(password));
            }
        });

        JButton addButton = new JButton("Add Credential");
        addButton.addActionListener(e -> addCredential());
        panel.add(addButton);

        return panel;
    }

    private JPanel createViewTab() {
        JPanel panel = new JPanel(new BorderLayout());
        viewArea = new JTextArea();
        viewArea.setEditable(false);
        JButton refreshButton = new JButton("🔁 Refresh");
        refreshButton.addActionListener(e -> viewCredentials());

        panel.add(new JScrollPane(viewArea), BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createSearchTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout());
        searchField = new JTextField();
        JButton searchButton = new JButton("Search");

        top.add(new JLabel("🔍 Search: "), BorderLayout.WEST);
        top.add(searchField, BorderLayout.CENTER);
        top.add(searchButton, BorderLayout.EAST);

        searchArea = new JTextArea();
        searchArea.setEditable(false);
        searchButton.addActionListener(e -> searchCredentials());

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(searchArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDeleteTab() {
        JPanel panel = new JPanel(new BorderLayout());
        deleteArea = new JTextArea();
        deleteArea.setEditable(false);
        deleteIndexField = new JTextField();

        JButton loadButton = new JButton("Load Credentials");
        JButton deleteButton = new JButton("Delete by Index");

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("Index to delete: "), BorderLayout.WEST);
        inputPanel.add(deleteIndexField, BorderLayout.CENTER);
        inputPanel.add(deleteButton, BorderLayout.EAST);

        loadButton.addActionListener(e -> viewCredentials(deleteArea));
        deleteButton.addActionListener(e -> deleteCredential());

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.add(loadButton);
        top.add(inputPanel);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(deleteArea), BorderLayout.CENTER);
        return panel;
    }

    private void addCredential() {
        String website = addWebsiteField.getText().trim();
        String username = addUsernameField.getText().trim();
        String password = new String(addPasswordField.getPassword());

        if (website.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        try (FileWriter fw = new FileWriter(FILE_NAME, true)) {
            String encrypted = encrypt(password);
            fw.write(website + "," + username + "," + encrypted + "\n");
            JOptionPane.showMessageDialog(this, "✅ Credential saved.");
            addWebsiteField.setText("");
            addUsernameField.setText("");
            addPasswordField.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving credential.");
        }
    }

    private void viewCredentials() {
        viewCredentials(viewArea);
    }

    private void viewCredentials(JTextArea area) {
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) {
                area.setText("⚠️ No credentials found.");
                return;
            }

            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty()) {
                area.setText("⚠️ No credentials found.");
                return;
            }

            StringBuilder output = new StringBuilder();
            int index = 1;
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    output.append(index++).append(". Website: ").append(parts[0])
                            .append("\n   Username: ").append(parts[1])
                            .append("\n   Password: ").append(decrypt(parts[2]))
                            .append("\n\n");
                }
            }
            area.setText(output.toString());
        } catch (Exception e) {
            area.setText("Error reading credentials.");
        }
    }

    private void searchCredentials() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            searchArea.setText("Enter a keyword to search.");
            return;
        }

        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) {
                searchArea.setText("⚠️ No credentials found.");
                return;
            }

            List<String> lines = Files.readAllLines(file.toPath());
            StringBuilder results = new StringBuilder();
            boolean found = false;

            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 3 &&
                        (parts[0].toLowerCase().contains(keyword) || parts[1].toLowerCase().contains(keyword))) {
                    results.append("Website: ").append(parts[0])
                            .append("\nUsername: ").append(parts[1])
                            .append("\nPassword: ").append(decrypt(parts[2]))
                            .append("\n\n");
                    found = true;
                }
            }

            searchArea.setText(found ? results.toString() : "❌ No match found.");
        } catch (Exception e) {
            searchArea.setText("Error reading credentials.");
        }
    }

    private void deleteCredential() {
        String input = deleteIndexField.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter an index.");
            return;
        }

        try {
            int index = Integer.parseInt(input);
            File file = new File(FILE_NAME);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(this, "⚠️ No credentials to delete.");
                return;
            }

            List<String> lines = Files.readAllLines(file.toPath());
            if (index < 1 || index > lines.size()) {
                JOptionPane.showMessageDialog(this, "Invalid index.");
                return;
            }

            lines.remove(index - 1);
            Files.write(file.toPath(), lines);
            JOptionPane.showMessageDialog(this, "✅ Credential deleted.");
            deleteIndexField.setText("");
            viewCredentials(deleteArea);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting credential.");
        }
    }

    private String encrypt(String strToEncrypt) throws Exception {
        Key aesKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes()));
    }

    private String decrypt(String strToDecrypt) throws Exception {
        Key aesKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
    }

    private String evaluateStrength(String password) {
        if (password.length() < 6) return "Very Weak";

        int score = 0;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[!@#$%^&*()].*")) score++;
        if (password.length() >= 12) score++;

        switch (score) {
            case 0: case 1: return "Weak";
            case 2: return "Moderate";
            case 3: return "Good";
            case 4: return "Strong";
            case 5: return "Very Strong";
            default: return "Unknown";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PasswordManager().setVisible(true));
    }
}
