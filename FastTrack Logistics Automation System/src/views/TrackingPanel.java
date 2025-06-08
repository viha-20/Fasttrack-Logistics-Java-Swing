package views;

import models.Driver;
import models.Shipment;
import services.DataService;
import services.NotificationService;
import utils.DateUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TrackingPanel extends JPanel {
    private DataService dataService;
    private NotificationService notificationService;
    private JTable trackingTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilterCombo;

    public TrackingPanel() {
        dataService = new DataService();
        notificationService = new NotificationService();
        initUI();
        loadShipments();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by Status:"));
        statusFilterCombo = new JComboBox<>(new String[]{"ALL", "PENDING", "SCHEDULED", "IN_TRANSIT", "DELIVERED", "DELAYED"});
        statusFilterCombo.addActionListener(e -> filterShipments());
        filterPanel.add(statusFilterCombo);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadShipments());
        filterPanel.add(refreshButton);

        // Table setup
        String[] columnNames = {"ID", "Sender", "Receiver", "Status", "Location", "Driver", "Delivery Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        trackingTable = new JTable(tableModel);
        trackingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(trackingTable);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton updateButton = new JButton("Update Location/Status");
        JButton delayButton = new JButton("Mark as Delayed");

        updateButton.addActionListener(this::updateShipment);
        delayButton.addActionListener(this::markAsDelayed);

        buttonPanel.add(updateButton);
        buttonPanel.add(delayButton);

        add(filterPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadShipments() {
        tableModel.setRowCount(0);
        List<Shipment> shipments = dataService.getAllShipments();
        for (Shipment shipment : shipments) {
            Object[] row = {
                    shipment.getShipmentId(),
                    shipment.getSenderName(),
                    shipment.getReceiverName(),
                    shipment.getStatus(),
                    shipment.getCurrentLocation(),
                    shipment.getAssignedDriverId(),
                    shipment.getDeliveryDate()
            };
            tableModel.addRow(row);
        }
    }

    private void filterShipments() {
        String statusFilter = (String) statusFilterCombo.getSelectedItem();

        if ("ALL".equals(statusFilter)) {
            loadShipments();
            return;
        }

        tableModel.setRowCount(0);
        List<Shipment> shipments = dataService.getAllShipments().stream()
                .filter(s -> s.getStatus().equals(statusFilter))
                .collect(Collectors.toList());

        for (Shipment shipment : shipments) {
            Object[] row = {
                    shipment.getShipmentId(),
                    shipment.getSenderName(),
                    shipment.getReceiverName(),
                    shipment.getStatus(),
                    shipment.getCurrentLocation(),
                    shipment.getAssignedDriverId(),
                    shipment.getDeliveryDate()
            };
            tableModel.addRow(row);
        }
    }

    private void updateShipment(ActionEvent e) {
        int selectedRow = trackingTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a shipment to update",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String shipmentId = (String) tableModel.getValueAt(selectedRow, 0);
        Shipment shipment = dataService.getAllShipments().stream()
                .filter(s -> s.getShipmentId().equals(shipmentId))
                .findFirst()
                .orElse(null);

        if (shipment == null) {
            JOptionPane.showMessageDialog(this, "Shipment not found",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog();
        dialog.setTitle("Update Shipment Tracking");
        dialog.setSize(400, 300);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

        // Current info
        panel.add(new JLabel("Shipment ID:"));
        panel.add(new JLabel(shipment.getShipmentId()));
        panel.add(new JLabel("Current Status:"));
        panel.add(new JLabel(shipment.getStatus()));
        panel.add(new JLabel("Current Location:"));
        panel.add(new JLabel(shipment.getCurrentLocation()));

        // Update fields
        panel.add(new JLabel("New Status:"));
        JComboBox<String> statusCombo = new JComboBox<>(
                new String[]{"PENDING", "SCHEDULED", "IN_TRANSIT", "DELIVERED", "DELAYED"});
        statusCombo.setSelectedItem(shipment.getStatus());
        panel.add(statusCombo);

        panel.add(new JLabel("New Location:"));
        JTextField locationField = new JTextField(shipment.getCurrentLocation());
        panel.add(locationField);

        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(ev -> {
            String newStatus = (String) statusCombo.getSelectedItem();
            String newLocation = locationField.getText().trim();

            if (newLocation.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Location cannot be empty",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            shipment.setStatus(newStatus);
            shipment.setCurrentLocation(newLocation);

            // If delivered, update delivery date to today
            if (newStatus.equals("DELIVERED")) {
                shipment.setDeliveryDate(LocalDate.now());
            }

            dataService.updateShipment(shipment);

            // Notify customer
            notificationService.sendCustomerNotification(
                    shipment.getReceiverContact(),
                    "Your shipment " + shipmentId + " status updated to: " + newStatus +
                            ". Current location: " + newLocation
            );

            // Notify driver if assigned
            if (shipment.getAssignedDriverId() != null) {
                Driver driver = dataService.getAllDrivers().stream()
                        .filter(d -> d.getDriverId().equals(shipment.getAssignedDriverId()))
                        .findFirst()
                        .orElse(null);

                if (driver != null) {
                    notificationService.sendDriverNotification(
                            driver.getContact(),
                            "Shipment " + shipmentId + " status updated to: " + newStatus +
                                    ". Current location: " + newLocation
                    );
                }
            }

            dialog.dispose();
            loadShipments();
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void markAsDelayed(ActionEvent e) {
        int selectedRow = trackingTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a shipment to mark as delayed",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String shipmentId = (String) tableModel.getValueAt(selectedRow, 0);
        Shipment shipment = dataService.getAllShipments().stream()
                .filter(s -> s.getShipmentId().equals(shipmentId))
                .findFirst()
                .orElse(null);

        if (shipment == null) {
            JOptionPane.showMessageDialog(this, "Shipment not found",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (shipment.getStatus().equals("DELIVERED")) {
            JOptionPane.showMessageDialog(this, "Already delivered shipments cannot be marked as delayed",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog();
        dialog.setTitle("Mark Shipment as Delayed");
        dialog.setSize(400, 200);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

        panel.add(new JLabel("Shipment ID:"));
        panel.add(new JLabel(shipment.getShipmentId()));
        panel.add(new JLabel("Current Delivery Date:"));
        panel.add(new JLabel(shipment.getDeliveryDate().toString()));

        panel.add(new JLabel("New Delivery Date:"));
        JTextField newDateField = new JTextField(
                shipment.getDeliveryDate().plusDays(1).toString());
        panel.add(newDateField);

        panel.add(new JLabel("Delay Reason:"));
        JTextField reasonField = new JTextField();
        panel.add(reasonField);

        JButton saveButton = new JButton("Mark as Delayed");
        saveButton.addActionListener(ev -> {
            try {
                LocalDate newDate = LocalDate.parse(newDateField.getText());
                String reason = reasonField.getText().trim();

                if (reason.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please provide a delay reason",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (newDate.isBefore(shipment.getDeliveryDate())) {
                    JOptionPane.showMessageDialog(dialog, "New date must be after current delivery date",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                shipment.setStatus("DELAYED");
                shipment.setDeliveryDate(newDate);
                dataService.updateShipment(shipment);

                // Notify customer
                notificationService.sendCustomerNotification(
                        shipment.getReceiverContact(),
                        "Your shipment " + shipmentId + " has been delayed. New delivery date: " +
                                newDate + ". Reason: " + reason
                );

                // Notify driver if assigned
                if (shipment.getAssignedDriverId() != null) {
                    Driver driver = dataService.getAllDrivers().stream()
                            .filter(d -> d.getDriverId().equals(shipment.getAssignedDriverId()))
                            .findFirst()
                            .orElse(null);

                    if (driver != null) {
                        notificationService.sendDriverNotification(
                                driver.getContact(),
                                "Shipment " + shipmentId + " has been delayed. New delivery date: " +
                                        newDate + ". Reason: " + reason
                        );
                    }
                }

                dialog.dispose();
                loadShipments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format (YYYY-MM-DD)",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}