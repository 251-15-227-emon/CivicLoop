package civicloop.gui;

import civicloop.model.Service;
import civicloop.model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ServicePanel extends JPanel {
    private MainFrame parent;
    private JTable serviceTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private List<Service> allServices;

    public ServicePanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        UITheme.stylePanel(this);

        tableModel = new DefaultTableModel(new String[]{"ID", "Service", "Provider", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        serviceTable = new JTable(tableModel);
        serviceTable.setRowHeight(28);
        serviceTable.setFont(UITheme.LABEL_FONT);
        serviceTable.getTableHeader().setFont(UITheme.BUTTON_FONT);
        serviceTable.getTableHeader().setBackground(UITheme.SECONDARY);
        serviceTable.getTableHeader().setForeground(Color.WHITE);
        serviceTable.setShowGrid(false);
        serviceTable.setIntercellSpacing(new Dimension(0, 0));
        serviceTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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
        JScrollPane scroll = new JScrollPane(serviceTable);
        scroll.setBorder(UITheme.COMPOUND_BORDER);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setBackground(UITheme.PANEL_BG);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        buttonPanel.setOpaque(false);
        JButton offerBtn = new JButton("Offer Service");
        UITheme.styleButton(offerBtn);
        buttonPanel.add(offerBtn);
        JButton requestBtn = new JButton("Request Selected");
        UITheme.styleButton(requestBtn);
        buttonPanel.add(requestBtn);
        JButton completeBtn = new JButton("Complete Selected (Provider)");
        UITheme.styleButton(completeBtn);
        completeBtn.setBackground(UITheme.SUCCESS);
        buttonPanel.add(completeBtn);
        bottom.add(buttonPanel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search by Provider:"));
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
        int row = serviceTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a service first.");
            return;
        }
        String serviceId = (String) tableModel.getValueAt(row, 0);
        String hoursStr = JOptionPane.showInputDialog(this, "How many hours of service?");
        if (hoursStr == null) return;
        try {
            double hours = Double.parseDouble(hoursStr);
            if (hours <= 0) throw new IllegalArgumentException("Must be positive.");
            String result = parent.getDataStore().requestService(serviceId, parent.getCurrentUser(), hours);
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
        String[] options = busy.stream().map(s -> s.getServiceType() + " (" + s.getServiceId() + ")")
                .toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(this,
                "Select a service to complete:", "Complete Service",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (selected == null) return;
        String serviceId = selected.substring(selected.lastIndexOf('(') + 1, selected.lastIndexOf(')'));
        String result = parent.getDataStore().completeService(serviceId, parent.getCurrentUser());
        JOptionPane.showMessageDialog(this, result);
        saveData();
        parent.refreshAll();
    }

    private void filterServices() {
        String query = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        List<Service> filtered = allServices.stream()
                .filter(s -> {
                    if (query.isEmpty()) return true;
                    User provider = parent.getDataStore().findUser(s.getProviderId());
                    String providerName = provider != null ? provider.getName().toLowerCase() : "";
                    return providerName.contains(query);
                })
                .collect(Collectors.toList());
        for (Service s : filtered) {
            User provider = parent.getDataStore().findUser(s.getProviderId());
            String name = provider != null ? provider.getName() : "Unknown";
            tableModel.addRow(new Object[]{
                    s.getServiceId(), s.getServiceType(), name,
                    s.isAvailable() ? "Available" : "Busy"
            });
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
        allServices = parent.getDataStore().getServices();
        filterServices();
    }
}