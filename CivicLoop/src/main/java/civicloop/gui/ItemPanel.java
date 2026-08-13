package civicloop.gui;

import civicloop.model.Item;
import civicloop.model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ItemPanel extends JPanel {
    private MainFrame parent;
    private JTable itemTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private List<Item> allItems;  // ফিল্টার করার জন্য মূল ডেটা

    public ItemPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        UITheme.stylePanel(this);

        // টেবিল সেটআপ (আগের মতো)
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Owner", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        itemTable = new JTable(tableModel);
        itemTable.setRowHeight(28);
        itemTable.setFont(UITheme.LABEL_FONT);
        itemTable.getTableHeader().setFont(UITheme.BUTTON_FONT);
        itemTable.getTableHeader().setBackground(UITheme.SECONDARY);
        itemTable.getTableHeader().setForeground(Color.WHITE);
        itemTable.setShowGrid(false);
        itemTable.setIntercellSpacing(new Dimension(0, 0));
        itemTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : UITheme.TABLE_ALT_ROW);
                }
                return c;
            }
        });
        JScrollPane scroll = new JScrollPane(itemTable);
        scroll.setBorder(UITheme.COMPOUND_BORDER);
        add(scroll, BorderLayout.CENTER);

        // নিচের প্যানেল (বাটন + সার্চ)
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setBackground(UITheme.PANEL_BG);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        buttonPanel.setOpaque(false);

        JButton addBtn = new JButton("Add Item");
        UITheme.styleButton(addBtn);
        buttonPanel.add(addBtn);

        JButton requestBtn = new JButton("Request Selected");
        UITheme.styleButton(requestBtn);
        buttonPanel.add(requestBtn);

        JButton returnBtn = new JButton("Return Selected");
        UITheme.styleButton(returnBtn);
        returnBtn.setBackground(UITheme.SUCCESS);
        buttonPanel.add(returnBtn);

        bottom.add(buttonPanel, BorderLayout.WEST);

        // সার্চ অংশ
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search by Owner:"));
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

        bottom.add(searchPanel, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        // অ্যাকশন লিসনার
        addBtn.addActionListener(e -> addItem());
        requestBtn.addActionListener(e -> requestItem());
        returnBtn.addActionListener(e -> returnItem());
        searchBtn.addActionListener(e -> filterItems());
        clearBtn.addActionListener(e -> { searchField.setText(""); filterItems(); });
        searchField.addActionListener(e -> filterItems()); // Enter চাপলেও সার্চ

        refreshTable();
    }

    private void addItem() {
        String name = JOptionPane.showInputDialog(this, "Enter item name:");
        if (name == null || name.trim().isEmpty()) return;
        parent.getDataStore().addItem(name.trim(), parent.getCurrentUser());
        saveData();
        parent.refreshAll();
    }

    private void requestItem() {
        int row = itemTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return;
        }
        String itemId = (String) tableModel.getValueAt(row, 0);
        String hoursStr = JOptionPane.showInputDialog(this, "How many hours to borrow?");
        if (hoursStr == null) return;
        try {
            double hours = Double.parseDouble(hoursStr);
            if (hours <= 0) throw new IllegalArgumentException("Must be positive.");
            String result = parent.getDataStore().requestItem(itemId, parent.getCurrentUser(), hours);
            JOptionPane.showMessageDialog(this, result);
            saveData();
            parent.refreshAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void returnItem() {
        List<Item> borrowed = parent.getDataStore().getItemsBorrowedByUser(
                parent.getCurrentUser().getUserId());
        if (borrowed.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You have no borrowed items.");
            return;
        }
        String[] options = borrowed.stream().map(i -> i.getItemName() + " (" + i.getItemId() + ")")
                .toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(this,
                "Select an item to return:", "Return Item",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (selected == null) return;
        String itemId = selected.substring(selected.lastIndexOf('(') + 1, selected.lastIndexOf(')'));
        String result = parent.getDataStore().returnItem(itemId, parent.getCurrentUser());
        JOptionPane.showMessageDialog(this, result);
        saveData();
        parent.refreshAll();
    }

    private void filterItems() {
        String query = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        List<Item> filtered = allItems.stream()
                .filter(item -> {
                    if (query.isEmpty()) return true;
                    User owner = parent.getDataStore().findUser(item.getOwnerId());
                    String ownerName = owner != null ? owner.getName().toLowerCase() : "";
                    return ownerName.contains(query);
                })
                .collect(Collectors.toList());
        for (Item i : filtered) {
            User owner = parent.getDataStore().findUser(i.getOwnerId());
            String ownerName = owner != null ? owner.getName() : "Unknown";
            String status = i.isAvailable() ? "Available" : "Borrowed";
            tableModel.addRow(new Object[]{i.getItemId(), i.getItemName(), ownerName, status});
        }
    }

    private void saveData() {
        try {
            parent.getDataStore().saveToFile("civicloop_data.dat");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save data: " + ex.getMessage());
        }
    }

    public void refreshTable() {
        allItems = parent.getDataStore().getItems();  // মূল ডেটা সেভ
        filterItems();  // বর্তমান সার্চ শর্তে আপডেট
    }
}