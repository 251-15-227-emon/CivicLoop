package civicloop.gui;

import civicloop.model.Item;
import civicloop.model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ItemPanel extends JPanel {
    private MainFrame parent;
    private JTable itemTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private List<Item> allItems;

    public ItemPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        UITheme.stylePanel(this);

        // ---- Table setup ----
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Owner", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        itemTable = new JTable(tableModel);
        itemTable.setRowHeight(38);
        itemTable.setFont(UITheme.LABEL_FONT);
        itemTable.setSelectionBackground(new Color(UITheme.PRIMARY.getRed(), UITheme.PRIMARY.getGreen(), UITheme.PRIMARY.getBlue(), 40));
        itemTable.setSelectionForeground(UITheme.TEXT_MAIN);
        itemTable.getTableHeader().setFont(UITheme.BUTTON_FONT);
        itemTable.getTableHeader().setBackground(UITheme.TABLE_HEADER_BG);
        itemTable.getTableHeader().setForeground(Color.WHITE);
        itemTable.getTableHeader().setPreferredSize(new Dimension(0, 38));
        itemTable.setShowGrid(false);
        itemTable.setIntercellSpacing(new Dimension(0, 0));

        // Plain columns (ID, Name, Owner) with alternating row color
        DefaultTableCellRenderer plainRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : UITheme.TABLE_ALT_ROW);
                }
                return c;
            }
        };
        itemTable.setDefaultRenderer(Object.class, plainRenderer);

        // Status column gets the colored badge renderer
        itemTable.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scroll = new JScrollPane(itemTable);
        scroll.setBorder(UITheme.COMPOUND_BORDER);
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ---- Bottom panel (buttons + search) ----
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setBackground(UITheme.BACKGROUND);
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        buttonPanel.setOpaque(false);

        JButton addBtn = UITheme.createRoundedButton("➕ Add Item", UITheme.PRIMARY);
        buttonPanel.add(addBtn);

        JButton requestBtn = UITheme.createRoundedButton("🤝 Request Selected", UITheme.SECONDARY);
        buttonPanel.add(requestBtn);

        JButton returnBtn = UITheme.createRoundedButton("↩ Return Selected", UITheme.SUCCESS);
        buttonPanel.add(returnBtn);

        bottom.add(buttonPanel, BorderLayout.WEST);

        // ---- Search section ----
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        searchPanel.setOpaque(false);
        JLabel searchLbl = new JLabel("Search by Owner:");
        searchLbl.setFont(UITheme.SMALL_FONT);
        searchLbl.setForeground(UITheme.TEXT_MUTED);
        searchPanel.add(searchLbl);
        searchField = new JTextField(12);
        searchField.setFont(UITheme.LABEL_FONT);
        searchField.setBorder(UITheme.TEXT_BORDER);
        searchPanel.add(searchField);
        JButton searchBtn = UITheme.createRoundedButton("🔍 Search", UITheme.PRIMARY_DARK);
        searchPanel.add(searchBtn);
        JButton clearBtn = UITheme.createRoundedButton("✕ Clear", UITheme.WARNING);
        searchPanel.add(clearBtn);

        bottom.add(searchPanel, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        // ---- Action listeners ----
        addBtn.addActionListener(e -> addItem());
        requestBtn.addActionListener(e -> requestItem());
        returnBtn.addActionListener(e -> returnItem());
        searchBtn.addActionListener(e -> filterItems());
        clearBtn.addActionListener(e -> { searchField.setText(""); filterItems(); });
        searchField.addActionListener(e -> filterItems());

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
        allItems = parent.getDataStore().getItems();
        filterItems();
    }

    // ================= STATUS BADGE RENDERER =================
    /**
     * Draws the status column value as a colored rounded pill instead of
     * plain text — green for "Available", orange for "Borrowed"/"Busy".
     */
    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(false);
            setBackground(row % 2 == 0 ? Color.WHITE : UITheme.TABLE_ALT_ROW);
            setForeground(Color.WHITE);
            setFont(UITheme.BADGE_FONT);
            this.status = (value == null) ? "" : value.toString();
            this.rowBg = row % 2 == 0 ? Color.WHITE : UITheme.TABLE_ALT_ROW;
            return this;
        }

        private String status = "";
        private Color rowBg = Color.WHITE;

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // paint the row background first (so cell isn't transparent/black)
            g2.setColor(rowBg);
            g2.fillRect(0, 0, getWidth(), getHeight());

            Color badgeColor = UITheme.statusColor(status);
            int pillW = Math.min(getWidth() - 16, 110);
            int pillH = 24;
            int px = (getWidth() - pillW) / 2;
            int py = (getHeight() - pillH) / 2;

            g2.setColor(badgeColor);
            g2.fill(new RoundRectangle2D.Float(px, py, pillW, pillH, pillH, pillH));

            g2.setColor(Color.WHITE);
            g2.setFont(UITheme.BADGE_FONT);
            FontMetrics fm = g2.getFontMetrics();
            int tx = px + (pillW - fm.stringWidth(status)) / 2;
            int ty = py + (pillH - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(status, tx, ty);

            g2.dispose();
        }
    }
}