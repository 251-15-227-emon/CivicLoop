package civicloop.gui;

import civicloop.data.DataStore;
import civicloop.model.User;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MainFrame extends JFrame {
    private DataStore dataStore;
    private User currentUser;
    private JLabel welcomeLabel;
    private JButton logoutBtn;
    private ItemPanel itemPanel;
    private ServicePanel servicePanel;
    private TimeBankPanel timeBankPanel;
    private TrustPanel trustPanel;
    private FeedPanel feedPanel;

    public MainFrame(DataStore dataStore, User user) {
        this.dataStore = dataStore;
        this.currentUser = user;
        setTitle("CivicLoop - " + user.getName() + " (" + user.getUserId() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ---- Gradient Header Panel ----
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY, getWidth(), 0, UITheme.SECONDARY);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        // Welcome label
        welcomeLabel = new JLabel();
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(UITheme.HEADER_FONT);
        header.add(welcomeLabel, BorderLayout.WEST);

        // Logout button
        logoutBtn = new JButton("Logout");
        UITheme.styleButton(logoutBtn);
        logoutBtn.setBackground(UITheme.DANGER);
        header.add(logoutBtn, BorderLayout.EAST);
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?", "Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                saveData();
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        add(header, BorderLayout.NORTH);

        // ---- Tabbed Panels ----
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.BUTTON_FONT);
        tabs.setBackground(UITheme.PANEL_BG);

        itemPanel = new ItemPanel(this);
        servicePanel = new ServicePanel(this);
        timeBankPanel = new TimeBankPanel(this);
        trustPanel = new TrustPanel(this);
        feedPanel = new FeedPanel(this);

        tabs.addTab("Item Sharing", itemPanel);
        tabs.addTab("Service Exchange", servicePanel);
        tabs.addTab("TimeBank", timeBankPanel);
        tabs.addTab("Trust & Profile", trustPanel);
        tabs.addTab("Community Feed", feedPanel);
        add(tabs, BorderLayout.CENTER);

        refreshAll();

        // Save data when window closes
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveData();
            }
        });
    }

    /**
     * Save data to the file.
     */
    private void saveData() {
        try {
            dataStore.saveToFile("civicloop_data.dat");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save data: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Refresh all panels to reflect the latest data.
     */
    public void refreshAll() {
        welcomeLabel.setText("👋 " + currentUser.getName() +
                "  |  Area: " + currentUser.getArea() +
                "  |  TC Balance: " + currentUser.getTimeCreditBalance() +
                "  |  Trust: " + currentUser.getTrustScore());
        itemPanel.refreshTable();
        servicePanel.refreshTable();
        timeBankPanel.refresh();
        trustPanel.refresh();
        feedPanel.refresh();
    }

    // ---- Getters for child panels ----
    public DataStore getDataStore() { return dataStore; }
    public User getCurrentUser() { return currentUser; }
}