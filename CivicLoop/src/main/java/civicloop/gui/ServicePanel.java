package civicloop.gui;

import civicloop.model.Service;
import civicloop.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ServicePanel extends JPanel {
    private MainFrame parent;
    private DefaultListModel<Service> listModel;
    private JList<Service> serviceList;
    private JTextField searchField;
    private List<Service> allServices;

    public ServicePanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        UITheme.stylePanel(this);

        listModel = new DefaultListModel<>();
        serviceList = new JList<>(listModel);
        serviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serviceList.setCellRenderer(new ServiceCardRenderer());
        serviceList.setFixedCellHeight(70);
        serviceList.setBackground(UITheme.BACKGROUND);

        JScrollPane scroll = new JScrollPane(serviceList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.BACKGROUND);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setBackground(UITheme.BACKGROUND);
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        buttonPanel.setOpaque(false);
        JButton offerBtn = UITheme.createRoundedButton(UITheme.iconText("➕", "Offer Service"), UITheme.PRIMARY);
        buttonPanel.add(offerBtn);
        JButton requestBtn = UITheme.createRoundedButton(UITheme.iconText("🤝", "Request Selected"), UITheme.SECONDARY);
        buttonPanel.add(requestBtn);
        JButton completeBtn = UITheme.createRoundedButton(UITheme.iconText("✔", "Complete Selected"), UITheme.SUCCESS);
        buttonPanel.add(completeBtn);
        bottom.add(buttonPanel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        searchPanel.setOpaque(false);
        JLabel searchLbl = new JLabel(UITheme.iconText("🔎", "Search by Provider:"));
        searchLbl.setFont(UITheme.SMALL_FONT);
        searchLbl.setForeground(UITheme.TEXT_MUTED);
        searchPanel.add(searchLbl);
        searchField = new JTextField(12);
        searchField.setFont(UITheme.LABEL_FONT);
        searchField.setBorder(UITheme.TEXT_BORDER);
        searchPanel.add(searchField);
        JButton searchBtn = UITheme.createRoundedButton(UITheme.iconText("🔍", "Search"), UITheme.PRIMARY_DARK);
        searchPanel.add(searchBtn);
        JButton clearBtn = UITheme.createRoundedButton(UITheme.iconText("✕", "Clear"), UITheme.WARNING);
        searchPanel.add(clearBtn);
        bottom.add(searchPanel, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        offerBtn.addActionListener(e -> offerService());
        requestBtn.addActionListener(e -> requestService());
        completeBtn.addActionListener(e -> completeService());
        searchBtn.addActionListener(e -> filterServices());
        clearBtn.addActionListener(e -> { searchField.setText(""); filterServices(); });
        searchField.addActionListener(e -> filterServices());

        refreshTable();
    }

    private void offerService() {
        String type = JOptionPane.showInputDialog(this, "Enter service type (e.g., Guitar Teaching):");
        if (type == null || type.trim().isEmpty()) return;
        parent.getDataStore().addService(type.trim(), parent.getCurrentUser());
        saveData();
        parent.refreshAll();
    }

    private void requestService() {
        Service selected = serviceList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a service first.");
            return;
        }
        String hoursStr = JOptionPane.showInputDialog(this, "How many hours of service?");
        if (hoursStr == null) return;
        try {
            double hours = Double.parseDouble(hoursStr);
            if (hours <= 0) throw new IllegalArgumentException("Must be positive.");
            String result = parent.getDataStore().requestService(selected.getServiceId(), parent.getCurrentUser(), hours);
            JOptionPane.showMessageDialog(this, result);
            saveData();
            parent.refreshAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void completeService() {
        List<Service> busy = parent.getDataStore().getBusyServicesByProvider(
                parent.getCurrentUser().getUserId());
        if (busy.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You have no busy services to complete.");
            return;
        }
        String[] options = busy.stream()
                .map(s -> s.getServiceType() + " (" + formatServiceId(s.getServiceId()) + ")")
                .toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(this,
                "Select a service to complete:", "Complete Service",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (selected == null) return;
        int idx = java.util.Arrays.asList(options).indexOf(selected);
        String serviceId = busy.get(idx).getServiceId();
        String result = parent.getDataStore().completeService(serviceId, parent.getCurrentUser());
        JOptionPane.showMessageDialog(this, result);
        saveData();
        parent.refreshAll();
    }

    private void filterServices() {
        String query = searchField.getText().trim().toLowerCase();
        listModel.clear();
        List<Service> filtered = allServices.stream()
                .filter(s -> {
                    if (query.isEmpty()) return true;
                    User provider = parent.getDataStore().findUser(s.getProviderId());
                    String providerName = provider != null ? provider.getName().toLowerCase() : "";
                    return providerName.contains(query);
                })
                .collect(Collectors.toList());
        for (Service s : filtered) listModel.addElement(s);
    }

    private void saveData() {
        try {
            parent.getDataStore().saveToFile("civicloop_data.dat");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save data: " + ex.getMessage());
        }
    }

    public void refreshTable() {
        allServices = parent.getDataStore().getServices();
        filterServices();
    }

    /** Formats a raw numeric ID like "3" into a professional tag "SVC-003". */
    static String formatServiceId(String rawId) {
        try {
            return "SVC-" + String.format("%03d", Integer.parseInt(rawId));
        } catch (NumberFormatException e) {
            return "SVC-" + rawId;
        }
    }

    // ================= CARD RENDERER =================
    private class ServiceCardRenderer extends JPanel implements ListCellRenderer<Service> {
        private JLabel iconLabel, typeLabel, idTag, providerLabel;
        private JPanel statusHolder;

        ServiceCardRenderer() {
            setLayout(new BorderLayout(12, 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

            iconLabel = new JLabel("🛠️");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setPreferredSize(new Dimension(44, 44));
            add(iconLabel, BorderLayout.WEST);

            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            topRow.setOpaque(false);
            typeLabel = new JLabel();
            typeLabel.setFont(UITheme.TITLE_FONT.deriveFont(15f));
            typeLabel.setForeground(UITheme.TEXT_MAIN);
            idTag = new JLabel();
            idTag.setFont(UITheme.SMALL_FONT.deriveFont(Font.BOLD, 11f));
            idTag.setForeground(UITheme.PRIMARY_DARK);
            topRow.add(typeLabel);
            topRow.add(idTag);
            center.add(topRow);

            providerLabel = new JLabel();
            providerLabel.setFont(UITheme.SMALL_FONT);
            providerLabel.setForeground(UITheme.TEXT_MUTED);
            center.add(providerLabel);

            add(center, BorderLayout.CENTER);

            statusHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            statusHolder.setOpaque(false);
            add(statusHolder, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Service> list, Service service,
                int index, boolean isSelected, boolean cellHasFocus) {
            User provider = parent.getDataStore().findUser(service.getProviderId());
            String providerName = provider != null ? provider.getName() : "Unknown";

            typeLabel.setText(service.getServiceType());
            idTag.setText("#" + formatServiceId(service.getServiceId()));
            providerLabel.setText(UITheme.iconText("👤", "Provided by " + providerName));

            statusHolder.removeAll();
            String status = service.isAvailable() ? "AVAILABLE" : "BUSY";
            statusHolder.add(UITheme.statusBadge(status, UITheme.statusColor(status)));

            CardBackground bg = new CardBackground(isSelected);
            bg.setLayout(new BorderLayout());
            bg.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
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