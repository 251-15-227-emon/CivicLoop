package civicloop.gui;

import civicloop.data.DataStore;
import civicloop.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;

public class MainFrame extends JFrame {
    private DataStore dataStore;
    private User currentUser;
    private JLabel avatarLabel, nameLabel, areaLabel;
    private StatChip tcChip, trustChip;
    private JButton logoutBtn, refreshBtn;
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
        setSize(1050, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);

        refreshAll();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveData();
            }
        });
    }

    // ================= HEADER =================
    private JComponent buildHeader() {
        UITheme.GradientPanel header = new UITheme.GradientPanel(UITheme.PRIMARY_DARK, UITheme.SECONDARY);
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        // ---- Left: avatar + name/area ----
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);

        avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(52, 52));
        left.add(avatarLabel);

        JPanel nameBlock = new JPanel();
        nameBlock.setOpaque(false);
        nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS));
        nameLabel = new JLabel();
        nameLabel.setFont(UITheme.HEADER_FONT);
        nameLabel.setForeground(Color.WHITE);
        areaLabel = new JLabel();
        areaLabel.setFont(UITheme.SMALL_FONT);
        areaLabel.setForeground(new Color(255, 255, 255, 210));
        nameBlock.add(nameLabel);
        nameBlock.add(areaLabel);
        left.add(nameBlock);

        header.add(left, BorderLayout.WEST);

        // ---- Center: BIG, clear stat chips ----
        JPanel chipsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        chipsPanel.setOpaque(false);
        tcChip = new StatChip("💰", "TIMECREDITS", "0", UITheme.ACCENT);
        trustChip = new StatChip("⭐", "TRUST SCORE", "0", UITheme.SECONDARY);
        chipsPanel.add(tcChip);
        chipsPanel.add(trustChip);
        header.add(chipsPanel, BorderLayout.CENTER);

        // ---- Right: refresh + logout buttons ----
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setOpaque(false);

        refreshBtn = UITheme.createRoundedButton(UITheme.iconText("↻", "Refresh"), UITheme.SUCCESS);
        rightButtons.add(refreshBtn);

        logoutBtn = UITheme.createRoundedButton("Logout", UITheme.DANGER);
        rightButtons.add(logoutBtn);

        header.add(rightButtons, BorderLayout.EAST);

        refreshBtn.addActionListener(e -> {
            try {
                dataStore.reloadFromFile("civicloop_data.dat");
                User updatedUser = dataStore.findUser(currentUser.getUserId());
                if (updatedUser != null) {
                    currentUser = updatedUser;
                } else {
                    JOptionPane.showMessageDialog(this, "Your user account no longer exists. Logging out.");
                    dispose();
                    new LoginFrame().setVisible(true);
                    return;
                }
                refreshAll();
                UITheme.showAutoDismissPopup(this, "✔ Data reloaded from disk successfully.");
            } catch (IOException | ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Failed to reload data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

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

        return header;
    }

    // ================= TABS =================
    private JComponent buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.BUTTON_FONT);
        tabs.setBackground(UITheme.BACKGROUND);

        itemPanel = new ItemPanel(this);
        servicePanel = new ServicePanel(this);
        timeBankPanel = new TimeBankPanel(this);
        trustPanel = new TrustPanel(this);
        feedPanel = new FeedPanel(this);

        tabs.addTab(UITheme.iconText("📦", "Item Sharing"), itemPanel);
        tabs.addTab(UITheme.iconText("🛠️", "Service Exchange"), servicePanel);
        tabs.addTab(UITheme.iconText("💰", "TimeBank"), timeBankPanel);
        tabs.addTab(UITheme.iconText("⭐", "Trust & Profile"), trustPanel);
        tabs.addTab(UITheme.iconText("📢", "Community Feed"), feedPanel);

        return tabs;
    }

    // ================= SAVE / REFRESH =================
    private void saveData() {
        try {
            dataStore.saveToFile("civicloop_data.dat");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save data: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshAll() {
        avatarLabel.setIcon(UITheme.avatarCircle(currentUser.getName(), currentUser.getUserId(), 52));
        nameLabel.setText(currentUser.getName() + "  (" + currentUser.getUserId() + ")");
        areaLabel.setText(UITheme.iconText("📍", currentUser.getArea()));

        tcChip.setValue(String.valueOf(currentUser.getTimeCreditBalance()) + " TC");
        trustChip.setValue(currentUser.getTrustScore() + " / 100");
        trustChip.setAccentColor(trustColorFor(currentUser.getTrustScore()));

        itemPanel.refreshTable();
        servicePanel.refreshTable();
        timeBankPanel.refresh();
        trustPanel.refresh();
        feedPanel.refresh();
    }

    private Color trustColorFor(int score) {
        if (score >= 80) return UITheme.SUCCESS;
        if (score >= 50) return UITheme.SECONDARY;
        if (score >= 30) return UITheme.WARNING;
        return UITheme.DANGER;
    }

    public DataStore getDataStore() { return dataStore; }
    public User getCurrentUser() { return currentUser; }

    // ================= STAT CHIP COMPONENT =================
    /**
     * A large, solid-background "stat card" for the header — much more
     * noticeable than a thin outlined pill. Shows an icon, a small caption
     * label, and a big bold value. Color-codes via setAccentColor().
     */
    private static class StatChip extends JComponent {
        private final String icon;
        private final String caption;
        private String value;
        private Color accentColor;

        StatChip(String icon, String caption, String initialValue, Color accentColor) {
            this.icon = icon;
            this.caption = caption;
            this.value = initialValue;
            this.accentColor = accentColor;
            setPreferredSize(new Dimension(170, 56));
            setOpaque(false);
        }

        void setValue(String value) {
            this.value = value;
            repaint();
        }

        void setAccentColor(Color c) {
            this.accentColor = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // Solid white-ish card background so it pops against the gradient header
            g2.setColor(new Color(255, 255, 255, 230));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));

            // Left accent strip (color-coded)
            g2.setColor(accentColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, 6, h, 16, 16));
            g2.fillRect(0, 0, 10, h); // square off the strip's right edge

            // Icon — drawn with an emoji-capable font directly (not HTML, since this is custom painting)
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            g2.drawString(icon, 16, h / 2 + 8);

            // Caption (small, muted, uppercase)
            g2.setFont(UITheme.SMALL_FONT.deriveFont(Font.BOLD, 10f));
            g2.setColor(UITheme.TEXT_MUTED);
            g2.drawString(caption, 46, h / 2 - 4);

            // Value (big, bold, colored)
            g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
            g2.setColor(accentColor.darker());
            g2.drawString(value, 46, h / 2 + 16);

            g2.dispose();
        }
    }
}