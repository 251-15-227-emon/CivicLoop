package civicloop.gui;

import civicloop.model.Item;
import civicloop.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemPanel extends JPanel {
    private MainFrame parent;
    private DefaultListModel<Item> listModel;
    private JList<Item> itemList;
    private JTextField searchField;
    private List<Item> allItems;

    public ItemPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        UITheme.stylePanel(this);

        listModel = new DefaultListModel<>();
        itemList = new JList<>(listModel);
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.setCellRenderer(new ItemCardRenderer());
        itemList.setFixedCellHeight(70);
        itemList.setBackground(UITheme.BACKGROUND);

        JScrollPane scroll = new JScrollPane(itemList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.BACKGROUND);
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

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        searchPanel.setOpaque(false);
        JLabel searchLbl = new JLabel("🔎 Search by Owner:");
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
        Item selected = itemList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return;
        }
        String hoursStr = JOptionPane.showInputDialog(this, "How many hours to borrow?");
        if (hoursStr == null) return;
        try {
            double hours = Double.parseDouble(hoursStr);
            if (hours <= 0) throw new IllegalArgumentException("Must be positive.");
            String result = parent.getDataStore().requestItem(selected.getItemId(), parent.getCurrentUser(), hours);
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
        String[] options = borrowed.stream()
                .map(i -> i.getItemName() + " (" + formatItemId(i.getItemId()) + ")")
                .toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(this,
                "Select an item to return:", "Return Item",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (selected == null) return;
        int idx = java.util.Arrays.asList(options).indexOf(selected);
        String itemId = borrowed.get(idx).getItemId();
        String result = parent.getDataStore().returnItem(itemId, parent.getCurrentUser());
        JOptionPane.showMessageDialog(this, result);
        saveData();
        parent.refreshAll();
    }

    private void filterItems() {
        String query = searchField.getText().trim().toLowerCase();
        listModel.clear();
        List<Item> filtered = allItems.stream()
                .filter(item -> {
                    if (query.isEmpty()) return true;
                    User owner = parent.getDataStore().findUser(item.getOwnerId());
                    String ownerName = owner != null ? owner.getName().toLowerCase() : "";
                    return ownerName.contains(query);
                })
                .collect(Collectors.toList());
        for (Item i : filtered) listModel.addElement(i);
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

    /** Formats a raw numeric ID like "3" into a professional tag "ITEM-003". */
    static String formatItemId(String rawId) {
        try {
            return "ITEM-" + String.format("%03d", Integer.parseInt(rawId));
        } catch (NumberFormatException e) {
            return "ITEM-" + rawId;
        }
    }

    // ================= CARD RENDERER =================
    private class ItemCardRenderer extends JPanel implements ListCellRenderer<Item> {
        private JLabel iconLabel, nameLabel, idTag, ownerLabel;
        private JPanel statusHolder;

        ItemCardRenderer() {
            setLayout(new BorderLayout(12, 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

            iconLabel = new JLabel("📦");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setPreferredSize(new Dimension(44, 44));
            add(iconLabel, BorderLayout.WEST);

            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            topRow.setOpaque(false);
            nameLabel = new JLabel();
            nameLabel.setFont(UITheme.TITLE_FONT.deriveFont(15f));
            nameLabel.setForeground(UITheme.TEXT_MAIN);
            idTag = new JLabel();
            idTag.setFont(UITheme.SMALL_FONT.deriveFont(Font.BOLD, 11f));
            idTag.setForeground(UITheme.PRIMARY_DARK);
            topRow.add(nameLabel);
            topRow.add(idTag);
            center.add(topRow);

            ownerLabel = new JLabel();
            ownerLabel.setFont(UITheme.SMALL_FONT);
            ownerLabel.setForeground(UITheme.TEXT_MUTED);
            center.add(ownerLabel);

            add(center, BorderLayout.CENTER);

            statusHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            statusHolder.setOpaque(false);
            add(statusHolder, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Item> list, Item item,
                int index, boolean isSelected, boolean cellHasFocus) {
            User owner = parent.getDataStore().findUser(item.getOwnerId());
            String ownerName = owner != null ? owner.getName() : "Unknown";

            nameLabel.setText(item.getItemName());
            idTag.setText("#" + formatItemId(item.getItemId()));
            ownerLabel.setText("👤 Owned by " + ownerName);

            statusHolder.removeAll();
            String status = item.isAvailable() ? "AVAILABLE" : "BORROWED";
            statusHolder.add(UITheme.statusBadge(status, UITheme.statusColor(status)));

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            wrapper.add(this, BorderLayout.CENTER);

            // Card background painted here via a wrapping panel
            CardBackground bg = new CardBackground(isSelected);
            bg.setLayout(new BorderLayout());
            bg.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            bg.add(this, BorderLayout.CENTER);
            return bg;
        }
    }

    /** Rounded card background with subtle border, highlights on selection. */
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
            g2.setColor(selected ? new Color(UITheme.PRIMARY.getRed(), UITheme.PRIMARY.getGreen(), UITheme.PRIMARY.getBlue(), 25) : Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
            g2.setColor(selected ? UITheme.PRIMARY : UITheme.BORDER_COLOR);
            g2.setStroke(new BasicStroke(selected ? 1.6f : 1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, 14, 14));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}