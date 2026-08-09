package civicloop.gui;

import civicloop.data.DataStore;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
/**
 * Login and registration window.
 * Uses DataStore for persistence – loads saved data on startup and saves on exit.
 */
public class LoginFrame extends JFrame {
    private JTextField userIdField, nameField, areaField, regUserIdField;
    private JPasswordField passwordField, regPasswordField;
    private DataStore dataStore;

    public LoginFrame() {
        setTitle("CivicLoop - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);

        // Load existing data (if any)
        try {
            dataStore = DataStore.loadFromFile("civicloop_data.dat");
        } catch (IOException | ClassNotFoundException e) {
            dataStore = new DataStore();  // start fresh if file missing/corrupt
        }

        // Use a tabbed pane to separate Login and Registration
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Login", createLoginPanel());
        tabs.add("Register", createRegisterPanel());
        add(tabs);
    }
   
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;