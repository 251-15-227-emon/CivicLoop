package civicloop.gui;

import civicloop.model.Service;
import civicloop.model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel for offering services and requesting them.
 */


public class ServicePanel extends JPanel {


private MainFrame parent;
    private JTable serviceTable;
    private DefaultTableModel tableModel;

    public ServicePanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID","Service","Provider","Available"}, 0);
        serviceTable = new JTable(tableModel);
        add(new JScrollPane(serviceTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout());
        JButton offerBtn = new JButton("Offer Service");
        JButton requestBtn = new JButton("Request Selected Service");
        bottom.add(offerBtn);
        bottom.add(requestBtn);
        add(bottom, BorderLayout.SOUTH);

        offerBtn.addActionListener(e -> offerService());
        requestBtn.addActionListener(e -> requestService());
        refreshTable();
    }

     private void offerService() {
        String type = JOptionPane.showInputDialog(this, "Enter service type (e.g., Guitar Teaching):");
        if (type == null || type.trim().isEmpty()) return;
        parent.getDataStore().addService(type.trim(), parent.getCurrentUser());
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
            if (hours <= 0) throw new IllegalArgumentException("Hours must be positive.");
            String result = parent.getDataStore().requestService(serviceId, parent.getCurrentUser(), hours);
            JOptionPane.showMessageDialog(this, result);
            parent.refreshAll();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }




















}


