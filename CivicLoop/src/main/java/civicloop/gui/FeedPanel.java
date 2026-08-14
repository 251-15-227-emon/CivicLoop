package civicloop.gui;

import civicloop.model.CommunityPost;
import civicloop.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.ArrayList;

public class FeedPanel extends JPanel {
    private MainFrame parent;
    private DefaultListModel<CommunityPost> postListModel;
    private JList<CommunityPost> postList;
    private JTextArea postInput;
    private JTextArea commentInput;
    private JTextField searchField;

    public FeedPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(0, 8));
        UITheme.stylePanel(this);

        postListModel = new DefaultListModel<>();
        postList = new JList<>(postListModel);
        postList.setCellRenderer(new PostCardRenderer());
        postList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        postList.setBackground(UITheme.BACKGROUND);
        postList.setFixedCellHeight(-1); // variable height, computed per-cell

        JScrollPane scroll = new JScrollPane(postList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        add(buildBottomPanel(), BorderLayout.SOUTH);

        refresh();
    }

    // ================= BOTTOM: compose + search =================
    private JComponent buildBottomPanel() {
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        bottom.add(buildComposeCard());
        bottom.add(Box.createVerticalStrut(6));
        bottom.add(buildSearchRow());

        return bottom;
    }

    private JComponent buildComposeCard() {
        UITheme.RoundedCardPanel card = new UITheme.RoundedCardPanel();
        card.setLayout(new GridLayout(1, 2, 12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        // ---- New post block ----
        JPanel postBlock = new JPanel(new BorderLayout(0, 4));
        postBlock.setOpaque(false);
        JLabel postCaption = new JLabel(UITheme.iconText("✍", "New Post"));
        postCaption.setFont(UITheme.SMALL_FONT.deriveFont(Font.BOLD, 11f));
        postCaption.setForeground(UITheme.TEXT_MUTED);
        postBlock.add(postCaption, BorderLayout.NORTH);

        postInput = new JTextArea(2, 20);
        postInput.setLineWrap(true);
        postInput.setWrapStyleWord(true);
        postInput.setFont(UITheme.LABEL_FONT);
        postInput.setBorder(UITheme.TEXT_BORDER);
        JScrollPane postScroll = new JScrollPane(postInput);
        postScroll.setBorder(BorderFactory.createEmptyBorder());
        postBlock.add(postScroll, BorderLayout.CENTER);

        JButton postBtn = UITheme.createRoundedButton(UITheme.iconText("📤", "Post"), UITheme.PRIMARY);
        JPanel postBtnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        postBtnWrap.setOpaque(false);
        postBtnWrap.add(postBtn);
        postBlock.add(postBtnWrap, BorderLayout.SOUTH);
        postBtn.addActionListener(e -> addPost());

        // ---- Comment block ----
        JPanel commentBlock = new JPanel(new BorderLayout(0, 4));
        commentBlock.setOpaque(false);
        JLabel commentCaption = new JLabel(UITheme.iconText("💬", "Add Comment (select a post)"));
        commentCaption.setFont(UITheme.SMALL_FONT.deriveFont(Font.BOLD, 11f));
        commentCaption.setForeground(UITheme.TEXT_MUTED);
        commentBlock.add(commentCaption, BorderLayout.NORTH);

        commentInput = new JTextArea(2, 20);
        commentInput.setLineWrap(true);
        commentInput.setWrapStyleWord(true);
        commentInput.setFont(UITheme.LABEL_FONT);
        commentInput.setBorder(UITheme.TEXT_BORDER);
        JScrollPane commentScroll = new JScrollPane(commentInput);
        commentScroll.setBorder(BorderFactory.createEmptyBorder());
        commentBlock.add(commentScroll, BorderLayout.CENTER);

        JButton commentBtn = UITheme.createRoundedButton(UITheme.iconText("💬", "Comment"), UITheme.SECONDARY);
        JPanel commentBtnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        commentBtnWrap.setOpaque(false);
        commentBtnWrap.add(commentBtn);
        commentBlock.add(commentBtnWrap, BorderLayout.SOUTH);
        commentBtn.addActionListener(e -> addComment());

        card.add(postBlock);
        card.add(commentBlock);
        return card;
    }

    private JComponent buildSearchRow() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);
        JLabel searchLbl = new JLabel(UITheme.iconText("🔎", "Search by Author:"));
        searchLbl.setFont(UITheme.SMALL_FONT);
        searchLbl.setForeground(UITheme.TEXT_MUTED);
        searchPanel.add(searchLbl);
        searchField = new JTextField(12);
        searchField.setFont(UITheme.LABEL_FONT);
        searchField.setBorder(UITheme.TEXT_BORDER);
        searchPanel.add(searchField);
        JButton searchBtn = UITheme.createRoundedButton(UITheme.iconText("🔍", "Search"), UITheme.PRIMARY_DARK);
        searchPanel.add(searchBtn);
        JButton clearBtn = UITheme.createRoundedButton(UITheme.iconText("✕", "Clear"), UITheme.WARNING);
        searchPanel.add(clearBtn);

        searchBtn.addActionListener(e -> refresh());
        clearBtn.addActionListener(e -> { searchField.setText(""); refresh(); });
        searchField.addActionListener(e -> refresh());

        return searchPanel;
    }

    // ================= ACTIONS =================
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
        for (int i = allPosts.size() - 1; i >= 0; i--) {
            CommunityPost p = allPosts.get(i);
            if (!query.isEmpty()) {
                User author = parent.getDataStore().findUser(p.getAuthorId());
                String authorName = (author != null) ? author.getName().toLowerCase() : "";
                String authorId = p.getAuthorId().toLowerCase();
                if (!authorName.contains(query) && !authorId.contains(query)) {
                    continue;
                }
            }
            postListModel.addElement(p);
        }
    }

    // ================= CARD RENDERER =================
    private class PostCardRenderer extends JPanel implements ListCellRenderer<CommunityPost> {
        private final JLabel avatarLabel, authorLabel, metaLabel;
        private final JTextArea contentArea;
        private final JPanel statsRow;         // stable field — no fragile getParent() lookups
        private final JPanel commentsWrapper;

        PostCardRenderer() {
            setLayout(new BorderLayout(8, 4));
            setOpaque(false);

            // ---- Header row: avatar + name + timestamp ----
            JPanel headerRow = new JPanel(new BorderLayout(8, 0));
            headerRow.setOpaque(false);

            avatarLabel = new JLabel();
            avatarLabel.setPreferredSize(new Dimension(34, 34));
            headerRow.add(avatarLabel, BorderLayout.WEST);

            JPanel nameCol = new JPanel();
            nameCol.setOpaque(false);
            nameCol.setLayout(new BoxLayout(nameCol, BoxLayout.Y_AXIS));
            authorLabel = new JLabel();
            authorLabel.setFont(UITheme.TITLE_FONT.deriveFont(13f));
            authorLabel.setForeground(UITheme.TEXT_MAIN);
            metaLabel = new JLabel();
            metaLabel.setFont(UITheme.SMALL_FONT.deriveFont(10.5f));
            metaLabel.setForeground(UITheme.TEXT_MUTED);
            nameCol.add(authorLabel);
            nameCol.add(metaLabel);
            headerRow.add(nameCol, BorderLayout.CENTER);

            add(headerRow, BorderLayout.NORTH);

            // ---- Content ----
            contentArea = new JTextArea();
            contentArea.setEditable(false);
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setFont(UITheme.LABEL_FONT.deriveFont(13f));
            contentArea.setBackground(null);
            contentArea.setBorder(BorderFactory.createEmptyBorder(2, 0, 3, 0));
            add(contentArea, BorderLayout.CENTER);

            // ---- Footer: comment count badge + comments ----
            JPanel footer = new JPanel();
            footer.setOpaque(false);
            footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));

            statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            statsRow.setOpaque(false);
            footer.add(statsRow);

            commentsWrapper = new JPanel();
            commentsWrapper.setOpaque(false);
            commentsWrapper.setLayout(new BoxLayout(commentsWrapper, BoxLayout.Y_AXIS));
            commentsWrapper.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            footer.add(commentsWrapper);

            add(footer, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends CommunityPost> list,
                CommunityPost post, int index, boolean isSelected, boolean cellHasFocus) {
            User author = parent.getDataStore().findUser(post.getAuthorId());
            String authorName = (author != null) ? author.getName() : post.getAuthorId();

            avatarLabel.setIcon(UITheme.avatarCircle(authorName, post.getAuthorId(), 34));
            authorLabel.setText(authorName + "  #" + post.getAuthorId());
            metaLabel.setText(UITheme.iconText("🕒", post.getTimestamp()));
            contentArea.setText(post.getContent());

            // Comment count badge only — like button removed (it had no real
            // click action wired up; likePost() in DataStore is now unused
            // from the UI intentionally).
            statsRow.removeAll();
            statsRow.add(UITheme.statusBadge(UITheme.iconText("💬", String.valueOf(post.getComments().size())), UITheme.SECONDARY));

            // Comment bubbles (max 3 shown, rest summarized)
            commentsWrapper.removeAll();
            java.util.List<String> comments = post.getComments();
            int shown = Math.min(comments.size(), 3);
            for (int i = 0; i < shown; i++) {
                commentsWrapper.add(buildCommentBubble(comments.get(i)));
                commentsWrapper.add(Box.createVerticalStrut(3));
            }
            if (comments.size() > shown) {
                JLabel more = new JLabel((comments.size() - shown) + " more comment(s)...");
                more.setFont(UITheme.SMALL_FONT.deriveFont(10.5f));
                more.setForeground(UITheme.TEXT_MUTED);
                more.setBorder(BorderFactory.createEmptyBorder(1, 6, 0, 0));
                commentsWrapper.add(more);
            }
            if (comments.isEmpty()) {
                JLabel none = new JLabel("No comments yet — be the first to reply!");
                none.setFont(UITheme.SMALL_FONT.deriveFont(10.5f));
                none.setForeground(UITheme.TEXT_MUTED);
                commentsWrapper.add(none);
            }

            CardBackground bg = new CardBackground(isSelected);
            bg.setLayout(new BorderLayout());
            bg.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            bg.add(this, BorderLayout.CENTER);
            return bg;
        }

        /**
         * Builds a compact comment bubble: a single header line with
         * 👤 name #id  🕒 time (post-card style), then the comment text
         * below. Parses the "id|timestamp|text" format written by
         * DataStore.addCommentToPost(); falls back gracefully for older
         * comments saved before the timestamp was added.
         */
        private JComponent buildCommentBubble(String rawComment) {
            String commenterId, timestamp, text;
            String[] parts = rawComment.split("\\|", 3);
            if (parts.length == 3) {
                commenterId = parts[0];
                timestamp = parts[1];
                text = parts[2];
            } else {
                // Legacy format: "id: text" (no timestamp available)
                int idx = rawComment.indexOf(": ");
                if (idx > 0) {
                    commenterId = rawComment.substring(0, idx);
                    text = rawComment.substring(idx + 2);
                } else {
                    commenterId = "?";
                    text = rawComment;
                }
                timestamp = null;
            }

            User commenter = parent.getDataStore().findUser(commenterId);
            String commenterName = (commenter != null) ? commenter.getName() : commenterId;

            String headerHtml;
            if (timestamp != null) {
                headerHtml = "<html><span style='font-family:Segoe UI Emoji;'>👤</span>&nbsp;<b>"
                        + escapeHtml(commenterName) + "</b>&nbsp;#" + escapeHtml(commenterId)
                        + "&nbsp;&nbsp;<span style='font-family:Segoe UI Emoji;'>🕒</span>&nbsp;"
                        + escapeHtml(timestamp) + "</html>";
            } else {
                headerHtml = "<html><span style='font-family:Segoe UI Emoji;'>👤</span>&nbsp;<b>"
                        + escapeHtml(commenterName) + "</b>&nbsp;#" + escapeHtml(commenterId) + "</html>";
            }

            JPanel bubble = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0xF2F4F8));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            bubble.setOpaque(false);
            bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
            bubble.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            bubble.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel headerLabel = new JLabel(headerHtml);
            headerLabel.setFont(UITheme.SMALL_FONT.deriveFont(10.5f));
            headerLabel.setForeground(UITheme.PRIMARY_DARK);
            headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel textLabel = new JLabel("<html><div style='width:100%'>" + escapeHtml(text) + "</div></html>");
            textLabel.setFont(UITheme.SMALL_FONT);
            textLabel.setForeground(UITheme.TEXT_MAIN);
            textLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            textLabel.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));

            bubble.add(headerLabel);
            bubble.add(textLabel);
            return bubble;
        }

        private String escapeHtml(String s) {
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }

    private static class CardBackground extends JPanel {
        private final boolean selected;
        CardBackground(boolean selected) {
            this.selected = selected;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
            g2.setColor(selected ? UITheme.PRIMARY : UITheme.BORDER_COLOR);
            g2.setStroke(new BasicStroke(selected ? 1.6f : 1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, 14, 14));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}