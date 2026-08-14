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

    private CardLayout cardLayout;
    private JPanel cardHolder;
    private UITheme.RoundedButton loginToggleBtn, registerToggleBtn;

    public LoginFrame() {
        setTitle("CivicLoop - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        try {
            dataStore = DataStore.loadFromFile("civicloop_data.dat");
        } catch (IOException | ClassNotFoundException e) {
            dataStore = new DataStore();
            JOptionPane.showMessageDialog(this,
                "No saved data found. Starting fresh.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }

        // ---- Full-window gradient background ----
        UITheme.GradientPanel background = new UITheme.GradientPanel(UITheme.PRIMARY_DARK, UITheme.SECONDARY);
        background.setLayout(new GridBagLayout());
        setContentPane(background);

        // ---- Outer wrapper: logo + card ----
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        wrapper.add(buildLogo());
        wrapper.add(Box.createVerticalStrut(22));
        wrapper.add(buildCard());

        background.add(wrapper);
    }

    // ---- Logo / wordmark ----
    private JComponent buildLogo() {
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));

        JLabel iconLabel = new JLabel("🔄", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("CivicLoop", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Share • Trust • Thrive Together", SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(new Color(255, 255, 255, 200));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoPanel.add(iconLabel);
        logoPanel.add(titleLabel);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(subLabel);
        return logoPanel;
    }

    // ---- White rounded card containing the toggle + form ----
    private JComponent buildCard() {
        UITheme.RoundedCardPanel card = new UITheme.RoundedCardPanel();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(400, 430));
        card.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));

        // Toggle buttons (Login / Register)
        JPanel togglePanel = new JPanel(new GridLayout(1, 2, 8, 0));
        togglePanel.setOpaque(false);
        togglePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        loginToggleBtn = UITheme.createRoundedButton("Log In", UITheme.PRIMARY);
        registerToggleBtn = UITheme.createRoundedButton("Register", UITheme.BORDER_COLOR);
        registerToggleBtn.setForeground(UITheme.TEXT_MUTED);

        togglePanel.add(loginToggleBtn);
        togglePanel.add(registerToggleBtn);
        card.add(togglePanel, BorderLayout.NORTH);

        // Card layout for switching forms
        cardLayout = new CardLayout();
        cardHolder = new JPanel(cardLayout);
        cardHolder.setOpaque(false);
        cardHolder.add(createLoginPanel(), "LOGIN");
        cardHolder.add(createRegisterPanel(), "REGISTER");
        card.add(cardHolder, BorderLayout.CENTER);

        loginToggleBtn.addActionListener(e -> switchTo("LOGIN"));
        registerToggleBtn.addActionListener(e -> switchTo("REGISTER"));

        return card;
    }

    private void switchTo(String name) {
        cardLayout.show(cardHolder, name);
        boolean isLogin = name.equals("LOGIN");
        loginToggleBtn.setBaseColor(isLogin ? UITheme.PRIMARY : UITheme.BORDER_COLOR);
        loginToggleBtn.setForeground(isLogin ? Color.WHITE : UITheme.TEXT_MUTED);
        registerToggleBtn.setBaseColor(!isLogin ? UITheme.PRIMARY : UITheme.BORDER_COLOR);
        registerToggleBtn.setForeground(!isLogin ? Color.WHITE : UITheme.TEXT_MUTED);
    }

    // ---- Login form ----
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(fieldLabel("🆔", "User ID"));
        userIdField = styledTextField();
        panel.add(userIdField);
        panel.add(Box.createVerticalStrut(14));

        panel.add(fieldLabel("🔒", "Password"));
        passwordField = styledPasswordField();
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(26));

        UITheme.RoundedButton loginBtn = UITheme.createRoundedButton("Log In →", UITheme.ACCENT);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        panel.add(loginBtn);

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

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(panel, BorderLayout.NORTH);
        return wrap;
    }

    // ---- Register form ----
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(fieldLabel("🙍", "Full Name"));
        nameField = styledTextField();
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(12));

        panel.add(fieldLabel("📍", "Area"));
        areaField = styledTextField();
        panel.add(areaField);
        panel.add(Box.createVerticalStrut(12));

        panel.add(fieldLabel("🔒", "Password"));
        regPasswordField = styledPasswordField();
        panel.add(regPasswordField);
        panel.add(Box.createVerticalStrut(22));

        UITheme.RoundedButton regBtn = UITheme.createRoundedButton("Create Account", UITheme.SECONDARY);
        regBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        regBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        panel.add(regBtn);

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
                switchTo("LOGIN");
            }
        });

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(panel, BorderLayout.NORTH);
        return wrap;
    }

    // ---- Small styled helpers ----
    /**
     * Builds a field caption label with an emoji icon + text, keeping the
     * emoji in an emoji-capable font ("Segoe UI Emoji") while the text stays
     * in the normal theme font. Fixes emoji showing as blank boxes (□) on
     * some Windows/JDK setups.
     */
    private JLabel fieldLabel(String emoji, String text) {
        JLabel l = new JLabel(UITheme.iconText(emoji, text));
        l.setFont(UITheme.SMALL_FONT);
        l.setForeground(UITheme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 2, 4, 0));
        return l;
    }

    private JTextField styledTextField() {
        JTextField field = new JTextField();
        field.setFont(UITheme.LABEL_FONT);
        field.setBorder(UITheme.TEXT_BORDER);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(UITheme.LABEL_FONT);
        field.setBorder(UITheme.TEXT_BORDER);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
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