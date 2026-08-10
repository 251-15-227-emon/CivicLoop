package civicloop.gui;

import civicloop.model.CommunityPost;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


/**
 * Community Feed where users can post messages and like posts.
 */
public class FeedPanel extends JPanel {
    private MainFrame parent;
    private DefaultListModel<String> postListModel;
    private JList<String> postList;
    private JTextArea postInput;

    

    public FeedPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());

        // List of posts (newest first)
        postListModel = new DefaultListModel<>();
        postList = new JList<>(postListModel);
        add(new JScrollPane(postList), BorderLayout.CENTER);

        // Bottom: input area and buttons
        JPanel bottom = new JPanel(new BorderLayout());
        postInput = new JTextArea(3, 30);
        postInput.setLineWrap(true);
        JScrollPane inputScroll = new JScrollPane(postInput);
        bottom.add(inputScroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton postBtn = new JButton("Post to Feed");
        JButton likeBtn = new JButton("Like Selected Post");
        btnPanel.add(postBtn);
        btnPanel.add(likeBtn);
        bottom.add(btnPanel, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        postBtn.addActionListener(e -> addPost());
        likeBtn.addActionListener(e -> likePost());
        refresh();
    }