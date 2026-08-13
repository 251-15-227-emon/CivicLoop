package civicloop.gui;

import civicloop.model.User;
import javax.swing.*;
import java.awt.*;

public class TrustPanel extends JPanel {
    private MainFrame parent;
    private JProgressBar progressBar;
    private JLabel scoreLabel, statusLabel;
    private JTextArea profileArea;

    public TrustPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);

        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2;
        profileArea = new JTextArea(5, 30);
        profileArea.setEditable(false);
        add(new JScrollPane(profileArea), gbc);

        gbc.gridy=1; gbc.gridwidth=1;
        add(new JLabel("Trust Score:"), gbc);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        gbc.gridx=1;
        add(progressBar, gbc);

        scoreLabel = new JLabel();
        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=2;
        add(scoreLabel, gbc);

        statusLabel = new JLabel();
        gbc.gridy=3;
        add(statusLabel, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton lateBtn = new JButton("Report Late Return");
        JButton fakeBtn = new JButton("Report Fake Request");
        btnPanel.add(lateBtn);
        btnPanel.add(fakeBtn);
        gbc.gridy=4;
        add(btnPanel, gbc);

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