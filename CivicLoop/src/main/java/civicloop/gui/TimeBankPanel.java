package civicloop.gui;

import civicloop.model.TimeCreditTransaction;
import civicloop.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class TimeBankPanel extends JPanel {
    private MainFrame parent;
    private JLabel balanceValue;
    private DefaultListModel<TimeCreditTransaction> listModel;
    private JList<TimeCreditTransaction> txList;

    public TimeBankPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(0, 14));
        UITheme.stylePanel(this);

        add(buildBalanceCard(), BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        txList = new JList<>(listModel);
        txList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        txList.setCellRenderer(new TransactionCardRenderer());
        txList.setFixedCellHeight(84);          // was 64 — was clipping the detail line
        txList.setBackground(UITheme.BACKGROUND);

        JScrollPane scroll = new JScrollPane(txList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    // ================= BALANCE CARD =================
    private JComponent buildBalanceCard() {
        UITheme.RoundedCardPanel card = new UITheme.RoundedCardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel caption = new JLabel("💰 YOUR TIMECREDIT BALANCE");
        caption.setFont(UITheme.SMALL_FONT.deriveFont(Font.BOLD, 11f));
        caption.setForeground(UITheme.TEXT_MUTED);

        balanceValue = new JLabel("0 TC");
        balanceValue.setFont(new Font("Segoe UI", Font.BOLD, 32));
        balanceValue.setForeground(UITheme.ACCENT.darker());

        left.add(caption);
        left.add(balanceValue);
        card.add(left, BorderLayout.WEST);

        JLabel bigIcon = new JLabel("⏳");
        bigIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        card.add(bigIcon, BorderLayout.EAST);

        return card;
    }

    // ================= REFRESH =================
    public void refresh() {
        User user = parent.getCurrentUser();
        String uid = user.getUserId();
        balanceValue.setText(user.getTimeCreditBalance() + " TC");

        listModel.clear();
        for (TimeCreditTransaction t : parent.getDataStore().getTransactions()) {
            if (t.getFromUserId().equals(uid) || t.getToUserId().equals(uid)) {
                listModel.addElement(t);
            }
        }
    }

    static String formatTransactionId(String rawId) {
        try {
            return "TXN-" + String.format("%03d", Integer.parseInt(rawId));
        } catch (NumberFormatException e) {
            return "TXN-" + rawId;
        }
    }

    // ================= CARD RENDERER =================
    private class TransactionCardRenderer extends JPanel implements ListCellRenderer<TimeCreditTransaction> {
        private JLabel directionIcon, idTag, partyLabel, detailLabel, amountLabel;

        TransactionCardRenderer() {
            setLayout(new BorderLayout(12, 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

            directionIcon = new JLabel();
            directionIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            directionIcon.setHorizontalAlignment(SwingConstants.CENTER);
            directionIcon.setVerticalAlignment(SwingConstants.CENTER);
            directionIcon.setPreferredSize(new Dimension(40, 40));
            add(directionIcon, BorderLayout.WEST);

            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            topRow.setOpaque(false);
            topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            partyLabel = new JLabel();
            partyLabel.setFont(UITheme.TITLE_FONT.deriveFont(14f));
            partyLabel.setForeground(UITheme.TEXT_MAIN);
            idTag = new JLabel();
            idTag.setFont(UITheme.SMALL_FONT.deriveFont(Font.BOLD, 11f));
            idTag.setForeground(UITheme.PRIMARY_DARK);
            topRow.add(partyLabel);
            topRow.add(idTag);
            center.add(topRow);

            center.add(Box.createVerticalStrut(4));

            detailLabel = new JLabel();
            detailLabel.setFont(UITheme.SMALL_FONT);
            detailLabel.setForeground(UITheme.TEXT_MUTED);
            detailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            center.add(detailLabel);

            add(center, BorderLayout.CENTER);

            amountLabel = new JLabel();
            amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            amountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            amountLabel.setVerticalAlignment(SwingConstants.CENTER);
            amountLabel.setPreferredSize(new Dimension(90, 40));
            add(amountLabel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends TimeCreditTransaction> list,
                TimeCreditTransaction t, int index, boolean isSelected, boolean cellHasFocus) {

            String myId = parent.getCurrentUser().getUserId();
            boolean sent = t.getFromUserId().equals(myId);

            User other = parent.getDataStore().findUser(sent ? t.getToUserId() : t.getFromUserId());
            String otherName = other != null ? other.getName() : "Unknown";

            idTag.setText("#" + formatTransactionId(t.getTransactionId()));

            String typeIcon = t.getType().equalsIgnoreCase("Item") ? "📦" : "🛠️";

            if (sent) {
                directionIcon.setText("↑");
                directionIcon.setForeground(UITheme.DANGER);
                partyLabel.setText("To " + otherName);
                detailLabel.setText(typeIcon + " " + t.getType() + "  •  ⏱ " + t.getHoursSpent() + " hrs");
                amountLabel.setText("-" + t.getCreditAmount() + " TC");
                amountLabel.setForeground(UITheme.DANGER);
            } else {
                directionIcon.setText("↓");
                directionIcon.setForeground(UITheme.SUCCESS);
                partyLabel.setText("From " + otherName);
                detailLabel.setText(typeIcon + " " + t.getType() + "  •  ⏱ " + t.getHoursSpent() + " hrs");
                amountLabel.setText("+" + t.getCreditAmount() + " TC");
                amountLabel.setForeground(UITheme.SUCCESS.darker());
            }

            CardBackground bg = new CardBackground(isSelected);
            bg.setLayout(new BorderLayout());
            bg.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            bg.add(this, BorderLayout.CENTER);
            return bg;
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