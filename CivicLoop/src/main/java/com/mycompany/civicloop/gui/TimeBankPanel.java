package civicloop.gui;

import civicloop.model.TimeCreditTransaction;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Shows the current user's transaction history and TimeCredit balance.
 */

public class TimeBankPanel extends JPanel {
    private MainFrame parent;
    private JLabel balanceLabel;
    private JTable txTable;
    private DefaultTableModel tableModel;

    public TimeBankPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());

        // Balance display at the top
        balanceLabel = new JLabel();
        balanceLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(balanceLabel, BorderLayout.NORTH);

        // Transaction table
        tableModel = new DefaultTableModel(
                new String[]{"ID","From","To","Hours","Credits","Type"}, 0);
        txTable = new JTable(tableModel);
        add(new JScrollPane(txTable), BorderLayout.CENTER);

        refresh();
    }