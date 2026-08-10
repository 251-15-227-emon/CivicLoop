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