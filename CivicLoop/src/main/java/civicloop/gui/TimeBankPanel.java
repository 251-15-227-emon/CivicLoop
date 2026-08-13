package civicloop.gui;

import civicloop.model.TimeCreditTransaction;
import civicloop.model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TimeBankPanel extends JPanel {
    private MainFrame parent;
    private JLabel balanceLabel;
    private JTable txTable;
    private DefaultTableModel tableModel;

    public TimeBankPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        UITheme.stylePanel(this);

        balanceLabel = new JLabel();
        balanceLabel.setFont(UITheme.HEADER_FONT);
        balanceLabel.setForeground(UITheme.ACCENT);
        balanceLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        add(balanceLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{"ID", "From", "To", "Hours", "Credits", "Type"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        txTable = new JTable(tableModel);
        txTable.setRowHeight(28);
        txTable.setFont(UITheme.LABEL_FONT);
        txTable.getTableHeader().setFont(UITheme.BUTTON_FONT);
        txTable.getTableHeader().setBackground(UITheme.SECONDARY);
        txTable.getTableHeader().setForeground(Color.WHITE);
        txTable.setShowGrid(false);
        txTable.setIntercellSpacing(new Dimension(0, 0));
        txTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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
        JScrollPane scroll = new JScrollPane(txTable);
        scroll.setBorder(UITheme.COMPOUND_BORDER);
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        String uid = parent.getCurrentUser().getUserId();
        balanceLabel.setText("💰 Your TimeCredit Balance: " +
                parent.getCurrentUser().getTimeCreditBalance() + " TC");

        tableModel.setRowCount(0);
        for (TimeCreditTransaction t : parent.getDataStore().getTransactions()) {
            if (t.getFromUserId().equals(uid) || t.getToUserId().equals(uid)) {
                User fromUser = parent.getDataStore().findUser(t.getFromUserId());
                User toUser = parent.getDataStore().findUser(t.getToUserId());
                String fromDisplay = (fromUser != null) ? fromUser.getName() + " (" + t.getFromUserId() + ")" : t.getFromUserId();
                String toDisplay = (toUser != null) ? toUser.getName() + " (" + t.getToUserId() + ")" : t.getToUserId();
                tableModel.addRow(new Object[]{
                        t.getTransactionId(),
                        fromDisplay,
                        toDisplay,
                        t.getHoursSpent(),
                        t.getCreditAmount(),
                        t.getType()
                });
            }
        }
    }
}