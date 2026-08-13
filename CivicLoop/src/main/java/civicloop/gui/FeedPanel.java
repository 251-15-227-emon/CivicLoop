package civicloop.gui;

import civicloop.model.CommunityPost;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class FeedPanel extends JPanel {
    private MainFrame parent;
    private DefaultListModel<CommunityPost> postListModel;
    private JList<CommunityPost> postList;
    private JTextArea postInput;
    private JTextArea commentInput;
    private JTextField searchField;

    public FeedPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        UITheme.stylePanel(this);

        postListModel = new DefaultListModel<>();
        postList = new JList<>(postListModel);
        postList.setCellRenderer(new PostCardRenderer());
        postList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        postList.setBorder(UITheme.COMPOUND_BORDER);
        JScrollPane scroll = new JScrollPane(postList);
        scroll.setBorder(UITheme.COMPOUND_BORDER);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(6, 6));
        bottom.setBackground(UITheme.PANEL_BG);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // New post
        JPanel postPanel = new JPanel(new BorderLayout());
        postPanel.setBackground(UITheme.PANEL_BG);
        postPanel.setBorder(BorderFactory.createTitledBorder("New Post"));
        postInput = new JTextArea(2, 30);
        postInput.setLineWrap(true);
        postInput.setWrapStyleWord(true);
        postInput.setFont(UITheme.LABEL_FONT);
        postPanel.add(new JScrollPane(postInput), BorderLayout.CENTER);
        JButton postBtn = new JButton("Post");
        UITheme.styleButton(postBtn);
        postPanel.add(postBtn, BorderLayout.EAST);
        postBtn.addActionListener(e -> addPost());

        // Comment
        JPanel commentPanel = new JPanel(new BorderLayout());
        commentPanel.setBackground(UITheme.PANEL_BG);
        commentPanel.setBorder(BorderFactory.createTitledBorder("Add Comment"));
        commentInput = new JTextArea(2, 30);
        commentInput.setLineWrap(true);
        commentInput.setWrapStyleWord(true);
        commentInput.setFont(UITheme.LABEL_FONT);
        commentPanel.add(new JScrollPane(commentInput), BorderLayout.CENTER);
        JButton commentBtn = new JButton("Add Comment");
        UITheme.styleButton(commentBtn);
        commentBtn.setBackground(UITheme.ACCENT);
        commentPanel.add(commentBtn, BorderLayout.EAST);
        commentBtn.addActionListener(e -> addComment());

        // Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search by Author:"));
        searchField = new JTextField(12);
        searchField.setFont(UITheme.LABEL_FONT);
        searchPanel.add(searchField);
        JButton searchBtn = new JButton("Search");
        UITheme.styleButton(searchBtn);
        searchBtn.setBackground(UITheme.SECONDARY);
        searchPanel.add(searchBtn);
        JButton clearBtn = new JButton("Clear");
        UITheme.styleButton(clearBtn);
        clearBtn.setBackground(UITheme.WARNING);
        searchPanel.add(clearBtn);

        bottom.add(postPanel, BorderLayout.NORTH);
        bottom.add(commentPanel, BorderLayout.CENTER);
        bottom.add(searchPanel, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> refresh());
        clearBtn.addActionListener(e -> { searchField.setText(""); refresh(); });
        searchField.addActionListener(e -> refresh());

        refresh();
    }

    private void addPost() {
        String content = postInput.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cannot post empty message.");
            return;
        }
        parent.getDataStore().addPost(parent.getCurrentUser().getUserId(), content);
        saveData();
        postInput.setText("");
        parent.refreshAll();
    }

    private void addComment() {
        CommunityPost selected = postList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a post first.");
            return;
        }
        String comment = commentInput.getText().trim();
        if (comment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Comment cannot be empty.");
            return;
        }
        parent.getDataStore().addCommentToPost(selected.getPostId(),
                parent.getCurrentUser().getUserId(), comment);
        saveData();
        commentInput.setText("");
        parent.refreshAll();
    }

    private void saveData() {
        try {
            parent.getDataStore().saveToFile("civicloop_data.dat");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save data: " + ex.getMessage());
        }
    }

    public void refresh() {
        String query = searchField.getText().trim().toLowerCase();
        postListModel.clear();
        ArrayList<CommunityPost> allPosts = parent.getDataStore().getPosts();
        // newest first
        for (int i = allPosts.size() - 1; i >= 0; i--) {
            CommunityPost p = allPosts.get(i);
            if (!query.isEmpty()) {
                String author = p.getAuthorId().toLowerCase();
                if (!author.contains(query)) continue;
            }
            postListModel.addElement(p);
        }
    }

    // Inner class: custom renderer for post cards (shows comments)
    private class PostCardRenderer extends JPanel implements ListCellRenderer<CommunityPost> {
        private JLabel authorLabel, metaLabel, likeLabel, commentCountLabel;
        private JTextArea contentArea, commentArea;
        private JPanel commentPanel;

        public PostCardRenderer() {
            setLayout(new BorderLayout(6, 4));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.SECONDARY, 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            setBackground(Color.WHITE);

            // Header: author + timestamp
            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            authorLabel = new JLabel();
            authorLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            authorLabel.setForeground(UITheme.PRIMARY);
            top.add(authorLabel, BorderLayout.WEST);
            metaLabel = new JLabel();
            metaLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            metaLabel.setForeground(Color.GRAY);
            top.add(metaLabel, BorderLayout.EAST);
            add(top, BorderLayout.NORTH);

            // Content (multiline)
            contentArea = new JTextArea();
            contentArea.setEditable(false);
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
            contentArea.setBackground(null);
            contentArea.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            add(contentArea, BorderLayout.CENTER);

            // Comments section
            commentPanel = new JPanel(new BorderLayout());
            commentPanel.setOpaque(false);
            commentArea = new JTextArea();
            commentArea.setEditable(false);
            commentArea.setLineWrap(true);
            commentArea.setWrapStyleWord(true);
            commentArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
            commentArea.setForeground(Color.DARK_GRAY);
            commentArea.setBackground(new Color(240, 244, 248));
            commentArea.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            commentPanel.add(new JScrollPane(commentArea) {
                @Override
                public Dimension getPreferredSize() {
                    // Limit height to show up to 3 lines
                    return new Dimension(0, 60);
                }
            }, BorderLayout.CENTER);
            add(commentPanel, BorderLayout.SOUTH);

            // Like & comment count (will be added below comment area? or we can keep them separate)
            // Actually we can add them in a small panel at the bottom.
            JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
            statsPanel.setOpaque(false);
            likeLabel = new JLabel("♥ 0");
            likeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            statsPanel.add(likeLabel);
            commentCountLabel = new JLabel("💬 0");
            commentCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            statsPanel.add(commentCountLabel);
            // Add stats below the comment panel, so we need a container
            // For simplicity, we add it to SOUTH after commentPanel?
            // I'll create a new panel for bottom and add both comment and stats.
            // Since we already used SOUTH for commentPanel, we can replace with a wrapper.
            // Let's rework: use a vertical box for the bottom part.
            JPanel bottomWrapper = new JPanel(new BorderLayout());
            bottomWrapper.setOpaque(false);
            bottomWrapper.add(commentPanel, BorderLayout.CENTER);
            bottomWrapper.add(statsPanel, BorderLayout.SOUTH);
            add(bottomWrapper, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends CommunityPost> list,
                CommunityPost post, int index, boolean isSelected, boolean cellHasFocus) {
            authorLabel.setText("👤 " + post.getAuthorId());
            metaLabel.setText(post.getTimestamp());
            contentArea.setText(post.getContent());
            likeLabel.setText("♥ " + post.getLikes());
            commentCountLabel.setText("💬 " + post.getComments().size());

            // Show comments (if any)
            StringBuilder sb = new StringBuilder();
            if (post.getComments().isEmpty()) {
                sb.append("(No comments)");
            } else {
                for (String c : post.getComments()) {
                    sb.append("• ").append(c).append("\n");
                }
            }
            commentArea.setText(sb.toString());

            if (isSelected) {
                setBackground(UITheme.SECONDARY.brighter());
                setBorder(BorderFactory.createLineBorder(UITheme.ACCENT, 2));
            } else {
                setBackground(Color.WHITE);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.SECONDARY, 1),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
            return this;
        }
    }
}