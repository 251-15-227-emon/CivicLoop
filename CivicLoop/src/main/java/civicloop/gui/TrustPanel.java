package civicloop.gui;

import civicloop.model.TimeCreditTransaction;
import civicloop.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrustPanel extends JPanel {
    private MainFrame parent;
    private JProgressBar progressBar;
    private JLabel statusBadgeLabel;
    private JLabel avatarLabel, nameValue, areaValue, idValue, skillsValue;
    private JTextArea bioArea;
    private JLabel givenValue, receivedValue;
    private JButton editProfileBtn;
    private JComboBox<String> targetUserCombo;
    private JButton reportBtn;

    // NEW: backs the combo box with the actual transaction objects so a
    // report can be tied to ONE specific transaction instead of a user.
    // Index in this list always matches the selected index in targetUserCombo.
    private List<TimeCreditTransaction> reportableTransactions = new ArrayList<>();

    public TrustPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(0, 16));
        UITheme.stylePanel(this);

        add(buildProfileCard(), BorderLayout.NORTH);

        JPanel middle = new JPanel();
        middle.setOpaque(false);
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        middle.add(buildTrustCard());
        middle.add(Box.createVerticalStrut(16));
        middle.add(buildReportCard());
        add(middle, BorderLayout.CENTER);

        refresh();
    }

    // ================= PROFILE CARD =================
    private JComponent buildProfileCard() {
        UITheme.RoundedCardPanel card = new UITheme.RoundedCardPanel();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // ---- Top: avatar + name/id/area + edit button ----
        JPanel topRow = new JPanel(new BorderLayout(16, 0));
        topRow.setOpaque(false);

        avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(64, 64));
        avatarLabel.setVerticalAlignment(SwingConstants.TOP);
        topRow.add(avatarLabel, BorderLayout.WEST);

        JPanel infoGrid = new JPanel(new GridBagLayout());
        infoGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 0, 3, 14);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameValue = valueLabel("");
        nameValue.setFont(UITheme.TITLE_FONT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        infoGrid.add(nameValue, gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 1;
        gbc.gridx = 0; infoGrid.add(captionLabel("🆔 ID"), gbc);
        gbc.gridx = 1; idValue = valueLabel(""); infoGrid.add(idValue, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0; infoGrid.add(captionLabel("📍 Area"), gbc);
        gbc.gridx = 1; areaValue = valueLabel(""); infoGrid.add(areaValue, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0; infoGrid.add(captionLabel("🛠 Skills"), gbc);
        gbc.gridx = 1; skillsValue = valueLabel(""); infoGrid.add(skillsValue, gbc);

        topRow.add(infoGrid, BorderLayout.CENTER);

        editProfileBtn = UITheme.createRoundedButton("✎ Edit Profile", UITheme.SECONDARY);
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrap.setOpaque(false);
        btnWrap.add(editProfileBtn);
        topRow.add(btnWrap, BorderLayout.EAST);
        editProfileBtn.addActionListener(e -> editProfile());

        card.add(topRow, BorderLayout.NORTH);

        // ---- Bio: styled quote box, not raw text ----
        card.add(buildBioBox(), BorderLayout.SOUTH);

        return card;
    }

    private JComponent buildBioBox() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JLabel caption = new JLabel("📝 BIO");
        caption.setFont(UITheme.SMALL_FONT.deriveFont(Font.BOLD, 11f));
        caption.setForeground(UITheme.TEXT_MUTED);
        wrapper.add(caption, BorderLayout.NORTH);

        bioArea = new JTextArea();
        bioArea.setEditable(false);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        bioArea.setFont(UITheme.LABEL_FONT);
        bioArea.setOpaque(false);
        bioArea.setForeground(UITheme.TEXT_MAIN);
        bioArea.setRows(2);
        bioArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        // Rounded quote-style background behind the text area
        JPanel quoteBox = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.TABLE_ALT_ROW);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(UITheme.ACCENT);
                g2.fillRoundRect(0, 0, 4, getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        quoteBox.setOpaque(false);
        quoteBox.add(bioArea, BorderLayout.CENTER);
        wrapper.add(quoteBox, BorderLayout.CENTER);

        return wrapper;
    }

    private JLabel captionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.SMALL_FONT);
        l.setForeground(UITheme.TEXT_MUTED);
        return l;
    }

    private JLabel valueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.LABEL_FONT);
        l.setForeground(UITheme.TEXT_MAIN);
        return l;
    }

    // ================= TRUST SCORE CARD =================
    private JComponent buildTrustCard() {
        UITheme.RoundedCardPanel card = new UITheme.RoundedCardPanel();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("⭐ Trust Score");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_MAIN);
        top.add(title, BorderLayout.WEST);

        statusBadgeLabel = new JLabel();
        top.add(statusBadgeLabel, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(UITheme.BUTTON_FONT);
        progressBar.setPreferredSize(new Dimension(0, 26));
        progressBar.setBorder(BorderFactory.createEmptyBorder());
        card.add(progressBar, BorderLayout.CENTER);

        JPanel statsRow = new JPanel(new GridLayout(1, 2, 20, 0));
        statsRow.setOpaque(false);
        statsRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel givenBox = statBox("🎁 Given", UITheme.SUCCESS);
        givenValue = (JLabel) givenBox.getComponent(1);
        JPanel receivedBox = statBox("📥 Received", UITheme.SECONDARY);
        receivedValue = (JLabel) receivedBox.getComponent(1);

        statsRow.add(givenBox);
        statsRow.add(receivedBox);
        card.add(statsRow, BorderLayout.SOUTH);

        return card;
    }

    private JPanel statBox(String caption, Color color) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JLabel cap = new JLabel(caption);
        cap.setFont(UITheme.SMALL_FONT);
        cap.setForeground(UITheme.TEXT_MUTED);
        JLabel val = new JLabel("0");
        val.setFont(new Font("Segoe UI", Font.BOLD, 20));
        val.setForeground(color.darker());
        box.add(cap);
        box.add(val);
        return box;
    }

    // ================= REPORT CARD =================
    private JComponent buildReportCard() {
        UITheme.RoundedCardPanel card = new UITheme.RoundedCardPanel();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("⚠ Report a Community Member");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_MAIN);
        card.add(title, BorderLayout.NORTH);

        JPanel comboRow = new JPanel(new BorderLayout(10, 0));
        comboRow.setOpaque(false);
        JLabel comboLbl = new JLabel("🧾 Select transaction:");
        comboLbl.setFont(UITheme.LABEL_FONT);
        comboRow.add(comboLbl, BorderLayout.WEST);
        targetUserCombo = new JComboBox<>();
        targetUserCombo.setFont(UITheme.LABEL_FONT);
        comboRow.add(targetUserCombo, BorderLayout.CENTER);
        card.add(comboRow, BorderLayout.CENTER);

        JPanel reportBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        reportBtnPanel.setOpaque(false);
        reportBtn = UITheme.createRoundedButton("🚫 Report", UITheme.DANGER);
        reportBtnPanel.add(reportBtn);
        card.add(reportBtnPanel, BorderLayout.SOUTH);

        reportBtn.addActionListener(e -> reportUser());

        return card;
    }

    // ================= EDIT PROFILE DIALOG =================
    private void editProfile() {
        User user = parent.getCurrentUser();
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Profile", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(user.getName(), 15);
        nameField.setBorder(UITheme.TEXT_BORDER);
        JTextField areaField = new JTextField(user.getArea(), 15);
        areaField.setBorder(UITheme.TEXT_BORDER);
        JTextArea bioEditArea = new JTextArea(user.getBio(), 3, 20);
        bioEditArea.setLineWrap(true);
        bioEditArea.setWrapStyleWord(true);
        bioEditArea.setBorder(UITheme.TEXT_BORDER);

        DefaultListModel<String> skillModel = new DefaultListModel<>();
        for (String s : user.getSkills()) skillModel.addElement(s);
        JList<String> skillList = new JList<>(skillModel);
        JTextField skillAddField = new JTextField(10);
        JButton addSkillBtn = UITheme.createRoundedButton("➕ Add", UITheme.SUCCESS);
        JButton removeSkillBtn = UITheme.createRoundedButton("🗑 Remove", UITheme.DANGER);

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("🙍 Name:"), gbc);
        gbc.gridx = 1; dialog.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("📍 Area:"), gbc);
        gbc.gridx = 1; dialog.add(areaField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; dialog.add(new JLabel("📝 Bio:"), gbc);
        gbc.gridx = 1; dialog.add(new JScrollPane(bioEditArea), gbc);

        gbc.gridx = 0; gbc.gridy = 3; dialog.add(new JLabel("🛠 Skills:"), gbc);
        gbc.gridx = 1; dialog.add(new JScrollPane(skillList), gbc);

        JPanel skillPanel = new JPanel(new FlowLayout());
        skillPanel.add(skillAddField);
        skillPanel.add(addSkillBtn);
        skillPanel.add(removeSkillBtn);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        dialog.add(skillPanel, gbc);

        addSkillBtn.addActionListener(e -> {
            String skill = skillAddField.getText().trim();
            if (!skill.isEmpty()) {
                skillModel.addElement(skill);
                skillAddField.setText("");
            }
        });
        removeSkillBtn.addActionListener(e -> {
            int idx = skillList.getSelectedIndex();
            if (idx != -1) skillModel.remove(idx);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveBtn = UITheme.createRoundedButton("✔ Save", UITheme.SUCCESS);
        JButton cancelBtn = UITheme.createRoundedButton("✕ Cancel", UITheme.DANGER);
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);

        saveBtn.addActionListener(e -> {
            user.setName(nameField.getText().trim());
            user.setArea(areaField.getText().trim());
            user.setBio(bioEditArea.getText().trim());
            user.getSkills().clear();
            for (int i = 0; i < skillModel.size(); i++) {
                user.getSkills().add(skillModel.get(i));
            }
            try {
                parent.getDataStore().saveToFile("civicloop_data.dat");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, "Could not save: " + ex.getMessage());
            }
            parent.refreshAll();
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    // ================= REPORT ACTION (transaction-based, one report per transaction) =================
    /**
     * Only the person who offered the item/service (the transaction's
     * toUserId — owner or provider) may report the borrower/seeker
     * (fromUserId). refresh() already filters the combo to only such
     * transactions, so any selected entry here is always valid.
     */
    private void reportUser() {
        int idx = targetUserCombo.getSelectedIndex();
        if (idx < 0 || idx >= reportableTransactions.size()) {
            JOptionPane.showMessageDialog(this,
                    "No reportable transaction selected. You can only report a transaction where you offered the item/service, and only once.");
            return;
        }

        TimeCreditTransaction t = reportableTransactions.get(idx);
        String myId = parent.getCurrentUser().getUserId();
        String otherId = t.getFromUserId(); // the borrower/seeker being reported
        User other = parent.getDataStore().findUser(otherId);
        String otherName = other != null ? other.getName() : otherId;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Report " + otherName + " (" + otherId + ") on transaction #"
                        + t.getTransactionId() + "?",
                "Confirm Report", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String result = parent.getDataStore().reportTransaction(t.getTransactionId(), myId);

        saveData();
        parent.refreshAll();
        JOptionPane.showMessageDialog(this, result);
    }

    private void saveData() {
        try {
            parent.getDataStore().saveToFile("civicloop_data.dat");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save data: " + ex.getMessage());
        }
    }

    // ================= REFRESH =================
    public void refresh() {
        User user = parent.getCurrentUser();
        int score = parent.getDataStore().getTrustScore(user.getUserId());

        avatarLabel.setIcon(UITheme.avatarCircle(user.getName(), user.getUserId(), 64));
        nameValue.setText(user.getName());
        idValue.setText("#" + user.getUserId());
        areaValue.setText(user.getArea());
        skillsValue.setText(user.getSkills().isEmpty() ? "(None)" : String.join(", ", user.getSkills()));

        boolean hasBio = user.getBio() != null && !user.getBio().trim().isEmpty();
        bioArea.setText(hasBio ? user.getBio() : "This user hasn't written a bio yet.");
        bioArea.setForeground(hasBio ? UITheme.TEXT_MAIN : UITheme.TEXT_MUTED);
        bioArea.setFont(hasBio ? UITheme.LABEL_FONT : UITheme.LABEL_FONT.deriveFont(Font.ITALIC));

        Color trustColor = trustColorFor(score);
        progressBar.setValue(score);
        progressBar.setString(score + " / 100");
        progressBar.setForeground(trustColor);

        String desc;
        if (score >= 80) desc = "Excellent";
        else if (score >= 50) desc = "Good Standing";
        else if (score >= 30) desc = "Low Trust";
        else desc = "Unreliable";
        statusBadgeLabel = swapBadge(statusBadgeLabel, desc, trustColor);

        int given = 0, received = 0;
        for (var tx : parent.getDataStore().getTransactions()) {
            if (tx.getFromUserId().equals(user.getUserId())) given++;
            if (tx.getToUserId().equals(user.getUserId())) received++;
        }
        givenValue.setText(String.valueOf(given));
        receivedValue.setText(String.valueOf(received));

        // NEW: populate combo with individual, not-yet-reported transactions
        // where THIS user was the offerer (item owner / service provider) —
        // only the offerer may report the borrower/seeker on that transaction.
        targetUserCombo.removeAllItems();
        reportableTransactions = new ArrayList<>();
        String myId = user.getUserId();
        for (TimeCreditTransaction t : parent.getDataStore().getTransactions()) {
            if (t.isReported()) continue;
            if (!t.getToUserId().equals(myId)) continue; // must be the offerer

            String otherId = t.getFromUserId(); // borrower/seeker
            User other = parent.getDataStore().findUser(otherId);
            String otherName = other != null ? other.getName() : otherId;

            reportableTransactions.add(t);
            targetUserCombo.addItem("#" + TimeBankPanel.formatTransactionId(t.getTransactionId())
                    + " - " + otherName + " (" + otherId + ") - "
                    + t.getType() + " - " + t.getHoursSpent() + " hrs");
        }
        if (targetUserCombo.getItemCount() == 0) {
            targetUserCombo.addItem("(No reportable transactions)");
        }
    }

    private Color trustColorFor(int score) {
        if (score >= 80) return UITheme.SUCCESS;
        if (score >= 50) return UITheme.SECONDARY;
        if (score >= 30) return UITheme.WARNING;
        return UITheme.DANGER;
    }

    private JLabel swapBadge(JLabel oldLabel, String text, Color color) {
        JLabel newBadge = UITheme.statusBadge(text.toUpperCase(), color);
        Container parentContainer = oldLabel.getParent();
        if (parentContainer != null) {
            parentContainer.remove(oldLabel);
            parentContainer.add(newBadge, BorderLayout.EAST);
            parentContainer.revalidate();
            parentContainer.repaint();
        }
        return newBadge;
    }
}