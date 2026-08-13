package civicloop.gui;

import civicloop.data.DataStore;
import civicloop.model.User;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LoginFrame extends JFrame {
    private JTextField userIdField, nameField, areaField;
    private JPasswordField passwordField, regPasswordField;
    private DataStore dataStore;

    public LoginFrame() {
        setTitle("CivicLoop - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        try {
            dataStore = DataStore.loadFromFile("civicloop_data.dat");
        } catch (IOException | ClassNotFoundException e) {
            dataStore = new DataStore();
            JOptionPane.showMessageDialog(this,
                "No saved data found. Starting fresh.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Login", createLoginPanel());
        tabs.addTab("Register", createRegisterPanel());
        add(tabs);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        UITheme.stylePanel(panel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblId = new JLabel("User ID:");
        lblId.setFont(UITheme.LABEL_FONT);
        gbc.gridx = 0; gbc.gridy = 0; panel.add(lblId, gbc);
        userIdField = new JTextField(12);
        userIdField.setFont(UITheme.LABEL_FONT);
        gbc.gridx = 1; panel.add(userIdField, gbc);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(UITheme.LABEL_FONT);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(lblPass, gbc);
        passwordField = new JPasswordField(12);
        passwordField.setFont(UITheme.LABEL_FONT);
        gbc.gridx = 1; panel.add(passwordField, gbc);

        JButton loginBtn = new JButton("Log In");
        UITheme.styleButton(loginBtn);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> {
            String uid = userIdField.getText().trim();
            String pass = new String(passwordField.getPassword());
            if (uid.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill both fields.");
                return;
            }
            User user = dataStore.login(uid, pass);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid User ID or password.");
            } else {
                dispose();
                new MainFrame(dataStore, user).setVisible(true);
            }
        });

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        UITheme.stylePanel(panel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblName = new JLabel("Full Name:");
        lblName.setFont(UITheme.LABEL_FONT);
        gbc.gridx = 0; gbc.gridy = 0; panel.add(lblName, gbc);
        nameField = new JTextField(12);
        nameField.setFont(UITheme.LABEL_FONT);
        gbc.gridx = 1; panel.add(nameField, gbc);

        JLabel lblArea = new JLabel("Area:");
        lblArea.setFont(UITheme.LABEL_FONT);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(lblArea, gbc);
        areaField = new JTextField(12);
        areaField.setFont(UITheme.LABEL_FONT);
        gbc.gridx = 1; panel.add(areaField, gbc);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(UITheme.LABEL_FONT);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(lblPass, gbc);
        regPasswordField = new JPasswordField(12);
        regPasswordField.setFont(UITheme.LABEL_FONT);
        gbc.gridx = 1; panel.add(regPasswordField, gbc);

        JButton regBtn = new JButton("Register");
        UITheme.styleButton(regBtn);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(regBtn, gbc);

        regBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String area = areaField.getText().trim();
            String pass = new String(regPasswordField.getPassword());
            if (name.isEmpty() || area.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }
            String newId = dataStore.registerUser(name, area, pass);
            if (newId == null) {
                JOptionPane.showMessageDialog(this, "Registration failed: No available User ID (1000-5000).");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Registration successful!\nYour User ID is: " + newId);
                nameField.setText("");
                areaField.setText("");
                regPasswordField.setText("");
            }
        });

        return panel;
    }

    @Override
    public void dispose() {
        try {
            dataStore.saveToFile("civicloop_data.dat");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save data: " + ex.getMessage());
        }
        super.dispose();
    }
}