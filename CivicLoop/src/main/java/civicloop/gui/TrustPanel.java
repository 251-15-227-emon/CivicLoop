package civicloop.gui;

import civicloop.model.User;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TrustPanel extends JPanel {
    private MainFrame parent;
    private JProgressBar progressBar;
    private JLabel scoreLabel, statusLabel;
    private JTextArea profileArea;
    private JTextField skillField;
    private JButton addSkillBtn;

    public TrustPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new GridBagLayout());
        UITheme.stylePanel(this);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Profile area
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        profileArea = new JTextArea(6, 30);
        profileArea.setEditable(false);
        profileArea.setFont(UITheme.LABEL_FONT);
        profileArea.setBackground(UITheme.PANEL_BG);
        profileArea.setBorder(UITheme.COMPOUND_BORDER);
        add(new JScrollPane(profileArea), gbc);

        // Trust score
        gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel lblScore = new JLabel("Trust Score:");
        lblScore.setFont(UITheme.LABEL_FONT);
        add(lblScore, gbc);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(UITheme.LABEL_FONT);
        progressBar.setForeground(UITheme.ACCENT);
        gbc.gridx = 1;
        add(progressBar, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        scoreLabel = new JLabel();
        scoreLabel.setFont(UITheme.LABEL_FONT);
        add(scoreLabel, gbc);

        gbc.gridy = 3;
        statusLabel = new JLabel();
        statusLabel.setFont(UITheme.LABEL_FONT);
        add(statusLabel, gbc);

        // Skill management
        gbc.gridy = 4; gbc.gridwidth = 1;
        JLabel skillLabel = new JLabel("Add Skill:");
        skillLabel.setFont(UITheme.LABEL_FONT);
        add(skillLabel, gbc);
        skillField = new JTextField(15);
        skillField.setFont(UITheme.LABEL_FONT);
        gbc.gridx = 1;
        add(skillField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        addSkillBtn = new JButton("Add Skill");
        UITheme.styleButton(addSkillBtn);
        addSkillBtn.setBackground(UITheme.SECONDARY);
        add(addSkillBtn, gbc);

        addSkillBtn.addActionListener(e -> addSkill());

        // Report buttons
        JPanel reportPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        reportPanel.setBackground(UITheme.PANEL_BG);
        JButton lateBtn = new JButton("Report Late Return");
        UITheme.styleButton(lateBtn);
        lateBtn.setBackground(UITheme.WARNING);
        reportPanel.add(lateBtn);

        JButton fakeBtn = new JButton("Report Fake Request");
        UITheme.styleButton(fakeBtn);
        fakeBtn.setBackground(UITheme.DANGER);
        reportPanel.add(fakeBtn);

        gbc.gridy = 6;
        add(reportPanel, gbc);

        lateBtn.addActionListener(e -> {
            parent.getDataStore().reportLateReturn(parent.getCurrentUser().getUserId());
            parent.refreshAll();
        });
        fakeBtn.addActionListener(e -> {
            parent.getDataStore().reportFakeRequest(parent.getCurrentUser().getUserId());
            parent.refreshAll();
        });

        refresh();
    }

    private void addSkill() {
        String skill = skillField.getText().trim();
        if (skill.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a skill.");
            return;
        }
        parent.getCurrentUser().addSkill(skill);
        skillField.setText("");
        parent.refreshAll();
        JOptionPane.showMessageDialog(this, "Skill added!");
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

        profileArea.setText("Name: " + user.getName() +
                "\nArea: " + user.getArea() +
                "\nSkills: " + String.join(", ", user.getSkills()));
    }
}