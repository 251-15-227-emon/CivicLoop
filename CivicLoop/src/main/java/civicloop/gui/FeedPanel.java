package civicloop.gui;

import civicloop.model.CommunityPost;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class FeedPanel extends JPanel {
    private MainFrame parent;
    private DefaultListModel<CommunityPost> postListModel;
    private JList<CommunityPost> postList;
    private JTextArea postInput;
    private JTextArea commentInput;
    private JButton commentBtn;
    private JButton refreshBtn;

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

        // New post panel
        JPanel postPanel = new JPanel(new BorderLayout());
        postPanel.setBackground(UITheme.PANEL_BG);
        postPanel.setBorder(BorderFactory.createTitledBorder("New Post"));
        postInput = new JTextArea(2, 30);
        postInput.setLineWrap(true);
        postInput.setWrapStyleWord(true);
        postInput.setFont(UITheme.LABEL_FONT);
        postPanel.add(new JScrollPane(postInput), BorderLayout.CENTER);

        JPanel postBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton postBtn = new JButton("Post");
        UITheme.styleButton(postBtn);
        postBtnPanel.add(postBtn);
        postPanel.add(postBtnPanel, BorderLayout.EAST);
        postBtn.addActionListener(e -> addPost());

        // Comment panel
        JPanel commentPanel = new JPanel(new BorderLayout());
        commentPanel.setBackground(UITheme.PANEL_BG);
        commentPanel.setBorder(BorderFactory.createTitledBorder("Add Comment to Selected Post"));
        commentInput = new JTextArea(2, 30);
        commentInput.setLineWrap(true);
        commentInput.setWrapStyleWord(true);
        commentInput.setFont(UITheme.LABEL_FONT);
        commentPanel.add(new JScrollPane(commentInput), BorderLayout.CENTER);

        JPanel commentBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        commentBtn = new JButton("Add Comment");
        UITheme.styleButton(commentBtn);
        commentBtn.setBackground(UITheme.ACCENT);
        commentBtnPanel.add(commentBtn);
        commentPanel.add(commentBtnPanel, BorderLayout.EAST);
        commentBtn.addActionListener(e -> addComment());

        // Refresh button at bottom
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshBtn = new JButton("Refresh Feed");
        UITheme.styleButton(refreshBtn);
        refreshBtn.setBackground(UITheme.SECONDARY);
        refreshPanel.add(refreshBtn);
        refreshBtn.addActionListener(e -> refresh());

        bottom.add(postPanel, BorderLayout.NORTH);
        bottom.add(commentPanel, BorderLayout.CENTER);
        bottom.add(refreshPanel, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

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
        postListModel.clear();
        ArrayList<CommunityPost> posts = parent.getDataStore().getPosts();
        for (int i = posts.size() - 1; i >= 0; i--) {
            postListModel.addElement(posts.get(i));
        }
    }

    // Custom card renderer (unchanged, but included for completeness)
    private class PostCardRenderer extends JPanel implements ListCellRenderer<CommunityPost> {
        private JLabel authorLabel, contentLabel, metaLabel, likeLabel, commentCountLabel;

        public PostCardRenderer() {
            setLayout(new BorderLayout(6, 4));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.SECONDARY, 1),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
            setBackground(Color.WHITE);

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

            contentLabel = new JLabel();
            contentLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            contentLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            add(contentLabel, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
            bottom.setOpaque(false);
            likeLabel = new JLabel("♥ 0");
            likeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            bottom.add(likeLabel);
            commentCountLabel = new JLabel("💬 0");
            commentCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            bottom.add(commentCountLabel);
            add(bottom, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends CommunityPost> list,
                CommunityPost post, int index, boolean isSelected, boolean cellHasFocus) {
            authorLabel.setText("👤 " + post.getAuthorId());
            metaLabel.setText(post.getTimestamp());
            contentLabel.setText(post.getContent());
            likeLabel.setText("♥ " + post.getLikes());
            commentCountLabel.setText("💬 " + post.getComments().size());

            if (isSelected) {
                setBackground(UITheme.SECONDARY.brighter());
                setBorder(BorderFactory.createLineBorder(UITheme.ACCENT, 2));
            } else {
                setBackground(Color.WHITE);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.SECONDARY, 1),
                        BorderFactory.createEmptyBorder(6, 8, 6, 8)
                ));
            }
            return this;
        }
    }
}