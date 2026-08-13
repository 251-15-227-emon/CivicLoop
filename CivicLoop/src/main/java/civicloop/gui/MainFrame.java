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
        setSize(900, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        UITheme.stylePanel(header);
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        welcomeLabel = new JLabel();
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(UITheme.HEADER_FONT);
        header.add(welcomeLabel, BorderLayout.WEST);

        logoutBtn = new JButton("Logout");
        UITheme.styleButton(logoutBtn);
        logoutBtn.setBackground(UITheme.DANGER);
        header.add(logoutBtn, BorderLayout.EAST);
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?", "Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                // Save data before logout
                saveData();
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
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

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveData();
            }
        });
    }

    private void saveData() {
        try {
            dataStore.saveToFile("civicloop_data.dat");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save data: " + ex.getMessage());
        }
    }

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

    public DataStore getDataStore() { return dataStore; }
    public User getCurrentUser() { return currentUser; }
}