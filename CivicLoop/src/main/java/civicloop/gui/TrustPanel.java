package civicloop.gui;

import civicloop.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrustPanel extends JPanel {
    private MainFrame parent;
    private JProgressBar progressBar;
    private JLabel scoreLabel, statusLabel;
    private JTextArea profileArea;
    private JButton editProfileBtn;
    private JComboBox<String> targetUserCombo;
    private JButton reportLateBtn, reportFakeBtn;

    public TrustPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new GridBagLayout());
        UITheme.stylePanel(this);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ---- Profile display ----
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        profileArea = new JTextArea(8, 30);
        profileArea.setEditable(false);
        profileArea.setFont(UITheme.LABEL_FONT);
        profileArea.setBackground(UITheme.PANEL_BG);
        profileArea.setBorder(UITheme.COMPOUND_BORDER);
        add(new JScrollPane(profileArea), gbc);

        // ---- Edit profile button ----
        gbc.gridy = 1; gbc.gridwidth = 2;
        editProfileBtn = new JButton("✎ Edit Profile");
        UITheme.styleButton(editProfileBtn);
        editProfileBtn.setBackground(UITheme.SECONDARY);
        add(editProfileBtn, gbc);
        editProfileBtn.addActionListener(e -> editProfile());

        // ---- Trust score ----
        gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel lblScore = new JLabel("Trust Score:");
        lblScore.setFont(UITheme.LABEL_FONT);
        add(lblScore, gbc);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(UITheme.LABEL_FONT);
        progressBar.setForeground(UITheme.ACCENT);
        gbc.gridx = 1;
        add(progressBar, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        scoreLabel = new JLabel();
        scoreLabel.setFont(UITheme.LABEL_FONT);
        add(scoreLabel, gbc);

        gbc.gridy = 4;
        statusLabel = new JLabel();
        statusLabel.setFont(UITheme.LABEL_FONT);
        add(statusLabel, gbc);

        // ---- Report on other users ----
        gbc.gridy = 5; gbc.gridwidth = 1;
        JLabel reportLabel = new JLabel("Report user:");
        reportLabel.setFont(UITheme.LABEL_FONT);
        add(reportLabel, gbc);
        gbc.gridx = 1;
        targetUserCombo = new JComboBox<>();
        targetUserCombo.setFont(UITheme.LABEL_FONT);
        add(targetUserCombo, gbc);

        JPanel reportBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        reportBtnPanel.setBackground(UITheme.PANEL_BG);
        reportLateBtn = new JButton("Report Late Return");
        UITheme.styleButton(reportLateBtn);
        reportLateBtn.setBackground(UITheme.WARNING);
        reportBtnPanel.add(reportLateBtn);

        reportFakeBtn = new JButton("Report Fake Request");
        UITheme.styleButton(reportFakeBtn);
        reportFakeBtn.setBackground(UITheme.DANGER);
        reportBtnPanel.add(reportFakeBtn);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        add(reportBtnPanel, gbc);

        reportLateBtn.addActionListener(e -> reportUser(false));
        reportFakeBtn.addActionListener(e -> reportUser(true));

        refresh();
    }

    private void editProfile() {
        User user = parent.getCurrentUser();
        // Create edit dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Profile", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(user.getName(), 15);
        JTextField areaField = new JTextField(user.getArea(), 15);
        JTextArea bioArea = new JTextArea(user.getBio(), 3, 20);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        // Skills list
        DefaultListModel<String> skillModel = new DefaultListModel<>();
        for (String s : user.getSkills()) skillModel.addElement(s);
        JList<String> skillList = new JList<>(skillModel);
        JTextField skillAddField = new JTextField(10);
        JButton addSkillBtn = new JButton("Add");
        JButton removeSkillBtn = new JButton("Remove Selected");

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; dialog.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("Area:"), gbc);
        gbc.gridx = 1; dialog.add(areaField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(new JLabel("Bio:"), gbc);
        gbc.gridx = 1; dialog.add(new JScrollPane(bioArea), gbc);
        gbc.gridx = 0; gbc.gridy = 3; dialog.add(new JLabel("Skills:"), gbc);
        gbc.gridx = 1; dialog.add(new JScrollPane(skillList), gbc);
        // Add/remove skills
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

        // Save / Cancel buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveBtn = new JButton("Save");
        UITheme.styleButton(saveBtn);
        saveBtn.setBackground(UITheme.SUCCESS);
        JButton cancelBtn = new JButton("Cancel");
        UITheme.styleButton(cancelBtn);
        cancelBtn.setBackground(UITheme.DANGER);
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);

        saveBtn.addActionListener(e -> {
            user.setName(nameField.getText().trim());
            user.setArea(areaField.getText().trim());
            user.setBio(bioArea.getText().trim());
            // Update skills
            user.getSkills().clear();
            for (int i = 0; i < skillModel.size(); i++) {
                user.getSkills().add(skillModel.get(i));
            }
            // Save data
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

    private void reportUser(boolean fake) {
        String selected = (String) targetUserCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "No user selected to report.");
            return;
        }
        // Extract user ID from the combo item (format: "Name (ID)")
        String userId = selected.substring(selected.lastIndexOf('(') + 1, selected.lastIndexOf(')'));
        if (userId.equals(parent.getCurrentUser().getUserId())) {
            JOptionPane.showMessageDialog(this, "You cannot report yourself.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Report " + selected + " for " + (fake ? "fake request" : "late return") + "?",
                "Confirm Report", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (fake) {
                parent.getDataStore().reportFakeRequest(userId);
            } else {
                parent.getDataStore().reportLateReturn(userId);
            }
            parent.refreshAll();
            JOptionPane.showMessageDialog(this, "Report submitted.");
        }
    }

    public void refresh() {
        User user = parent.getCurrentUser();
        int score = parent.getDataStore().getTrustScore(user.getUserId());
        progressBar.setValue(score);
        scoreLabel.setText("Score: " + score + "/100");

        String desc;
        if (score >= 80) desc = "Excellent community member";
        else if (score >= 50) desc = "Good standing";
        else if (score >= 30) desc = "Low trust – be cautious";
        else desc = "Very low trust – unreliable";
        statusLabel.setText(desc);

        // Profile display
        StringBuilder sb = new StringBuilder();
        sb.append("👤 Name: ").append(user.getName()).append("\n");
        sb.append("📍 Area: ").append(user.getArea()).append("\n");
        sb.append("🆔 User ID: ").append(user.getUserId()).append("\n");
        sb.append("📝 Bio: ").append(user.getBio() != null ? user.getBio() : "(Not set)").append("\n");
        sb.append("🛠 Skills: ");
        if (user.getSkills().isEmpty()) {
            sb.append("(None)");
        } else {
            sb.append(String.join(", ", user.getSkills()));
        }
        sb.append("\n\n📊 Transaction Stats:\n");
        int given = 0, received = 0;
        for (var tx : parent.getDataStore().getTransactions()) {
            if (tx.getFromUserId().equals(user.getUserId())) given++;
            if (tx.getToUserId().equals(user.getUserId())) received++;
        }
        sb.append("  • Items/Services given: ").append(given).append("\n");
        sb.append("  • Items/Services received: ").append(received);
        profileArea.setText(sb.toString());

        // Update combo with all users except self
        targetUserCombo.removeAllItems();
        for (User u : parent.getDataStore().getAllUsers().values()) {
            if (!u.getUserId().equals(user.getUserId())) {
                targetUserCombo.addItem(u.getName() + " (" + u.getUserId() + ")");
            }
        }
        if (targetUserCombo.getItemCount() == 0) {
            targetUserCombo.addItem("(No other users)");
        }
    }
}