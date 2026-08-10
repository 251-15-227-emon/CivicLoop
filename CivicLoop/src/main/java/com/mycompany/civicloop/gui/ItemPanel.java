package civicloop.gui;

import civicloop.model.Item;
import civicloop.model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel for adding items and requesting to borrow them.
 */

public class ItemPanel extends JPanel {
    private MainFrame parent;
    private JTable itemTable;
    private DefaultTableModel tableModel;

    public ItemPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());

        // Table columns: ID, Name, Owner, Available
        tableModel = new DefaultTableModel(new String[]{"ID","Name","Owner","Available"}, 0);
        itemTable = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(itemTable);
        add(scroll, BorderLayout.CENTER);

        // Bottom panel with Add and Request buttons
        JPanel bottom = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add Item");
        JButton requestBtn = new JButton("Request Selected Item");
        bottom.add(addBtn);
        bottom.add(requestBtn);
        add(bottom, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addItem());
        requestBtn.addActionListener(e -> requestItem());
        refreshTable();
    }