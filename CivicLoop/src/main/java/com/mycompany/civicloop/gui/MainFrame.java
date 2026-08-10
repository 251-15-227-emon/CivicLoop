package civicloop.gui;

import civicloop.data.DataStore;
import civicloop.model.User;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
/**
 * Main dashboard after login.
 * Contains a JTabbedPane hosting all 5 module panels plus a profile header.
 */

public class MainFrame extends JFrame {
    private DataStore dataStore;
    private User currentUser;
    private JLabel welcomeLabel;
    private ItemPanel itemPanel;
    private ServicePanel servicePanel;
    private TimeBankPanel timeBankPanel;
    private TrustPanel trustPanel;
    private FeedPanel feedPanel;