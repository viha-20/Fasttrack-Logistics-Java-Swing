package views;

import models.Driver;
import models.Shipment;
import services.DataService;
import services.NotificationService;
import utils.ValidationUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.List;

public class ShipmentPanel extends JPanel {
    private DataService dataService;
    private NotificationService notificationService;
    private JTable shipmentTable;
    private DefaultTableModel tableModel;
    private JButton addButton, updateButton, deleteButton, assignDriverButton;

    public ShipmentPanel() {
        dataService = new DataService();
        notificationService = new NotificationService();
        initUI();
        loadShipments();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Table setup
        String[] columnNames = {"ID", "Sender", "Receiver", "Status", "Delivery Date", "Priority", "Driver"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        shipmentTable = new JTable(tableModel);
        shipmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        shipmentTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(shipmentTable);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("Add Shipment");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        assignDriverButton = new JButton("Assign Driver");

        addButton.addActionListener(this::addShipment);
        updateButton.addActionListener(this::updateShipment);
        deleteButton.addActionListener(this::deleteShipment);
        assignDriverButton.addActionListener(this::assignDriver);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(assignDriverButton);

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
                    shipment.getDeliveryDate(),
                    shipment.getPriority(),
                    shipment.getAssignedDriverId()
            };
            tableModel.addRow(row);
        }
    }

    private void addShipment(ActionEvent e) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Shipment");
        dialog.setSize(500, 600);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        // Form fields
        JTextField idField = new JTextField("SH" + System.currentTimeMillis());
        idField.setEditable(false);
        JTextField senderNameField = new JTextField();
        JTextField senderAddressField = new JTextField();
        JTextField senderContactField = new JTextField();
        JTextField receiverNameField = new JTextField();
        JTextField receiverAddressField = new JTextField();
        JTextField receiverContactField = new JTextField();
        JTextField contentsField = new JTextField();
        JTextField weightField = new JTextField();
        JTextField dimensionsField = new JTextField();
        JTextField deliveryDateField = new JTextField(LocalDate.now().plusDays(1).toString());
        JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"NORMAL", "EXPRESS", "URGENT"});
        JTextField locationField = new JTextField("WAREHOUSE");

        // Add fields to form
        formPanel.add(new JLabel("Shipment ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Sender Name:"));
        formPanel.add(senderNameField);
        formPanel.add(new JLabel("Sender Address:"));
        formPanel.add(senderAddressField);
        formPanel.add(new JLabel("Sender Contact:"));
        formPanel.add(senderContactField);
        formPanel.add(new JLabel("Receiver Name:"));
        formPanel.add(receiverNameField);
        formPanel.add(new JLabel("Receiver Address:"));
        formPanel.add(receiverAddressField);
        formPanel.add(new JLabel("Receiver Contact:"));
        formPanel.add(receiverContactField);
        formPanel.add(new JLabel("Package Contents:"));
        formPanel.add(contentsField);
        formPanel.add(new JLabel("Weight (kg):"));
        formPanel.add(weightField);
        formPanel.add(new JLabel("Dimensions (LxWxH):"));
        formPanel.add(dimensionsField);
        formPanel.add(new JLabel("Delivery Date (YYYY-MM-DD):"));
        formPanel.add(deliveryDateField);
        formPanel.add(new JLabel("Priority:"));
        formPanel.add(priorityCombo);
        formPanel.add(new JLabel("Current Location:"));
        formPanel.add(locationField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(ev -> {
            try {
                // Validate inputs
                if (!ValidationUtils.validateRequiredFields(
                        senderNameField, senderAddressField, senderContactField,
                        receiverNameField, receiverAddressField, receiverContactField,
                        contentsField, weightField, dimensionsField, deliveryDateField)) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all required fields",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double weight = Double.parseDouble(weightField.getText());
                LocalDate deliveryDate = LocalDate.parse(deliveryDateField.getText());

                Shipment shipment = new Shipment(
                        idField.getText(),
                        senderNameField.getText(),
                        senderAddressField.getText(),
                        senderContactField.getText(),
                        receiverNameField.getText(),
                        receiverAddressField.getText(),
                        receiverContactField.getText(),
                        contentsField.getText(),
                        weight,
                        dimensionsField.getText(),
                        LocalDate.now(),
                        deliveryDate,
                        "PENDING",
                        null,
                        locationField.getText(),
                        (String) priorityCombo.getSelectedItem()
                );

                dataService.saveShipment(shipment);
                notificationService.sendCustomerNotification(
                        receiverContactField.getText(),
                        "Your shipment " + idField.getText() + " has been registered. Expected delivery: " + deliveryDate
                );

                dialog.dispose();
                loadShipments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void updateShipment(ActionEvent e) {
        int selectedRow = shipmentTable.getSelectedRow();
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
        dialog.setTitle("Update Shipment");
        dialog.setSize(500, 600);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        // Form fields with existing data
        JTextField idField = new JTextField(shipment.getShipmentId());
        idField.setEditable(false);
        JTextField senderNameField = new JTextField(shipment.getSenderName());
        JTextField senderAddressField = new JTextField(shipment.getSenderAddress());
        JTextField senderContactField = new JTextField(shipment.getSenderContact());
        JTextField receiverNameField = new JTextField(shipment.getReceiverName());
        JTextField receiverAddressField = new JTextField(shipment.getReceiverAddress());
        JTextField receiverContactField = new JTextField(shipment.getReceiverContact());
        JTextField contentsField = new JTextField(shipment.getPackageContents());
        JTextField weightField = new JTextField(String.valueOf(shipment.getWeight()));
        JTextField dimensionsField = new JTextField(shipment.getDimensions());
        JTextField deliveryDateField = new JTextField(shipment.getDeliveryDate().toString());
        JComboBox<String> statusCombo = new JComboBox<>(
                new String[]{"PENDING", "IN_TRANSIT", "DELIVERED", "DELAYED"});
        statusCombo.setSelectedItem(shipment.getStatus());
        JComboBox<String> priorityCombo = new JComboBox<>(
                new String[]{"NORMAL", "EXPRESS", "URGENT"});
        priorityCombo.setSelectedItem(shipment.getPriority());
        JTextField locationField = new JTextField(shipment.getCurrentLocation());

        // Add fields to form
        formPanel.add(new JLabel("Shipment ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Sender Name:"));
        formPanel.add(senderNameField);
        formPanel.add(new JLabel("Sender Address:"));
        formPanel.add(senderAddressField);
        formPanel.add(new JLabel("Sender Contact:"));
        formPanel.add(senderContactField);
        formPanel.add(new JLabel("Receiver Name:"));
        formPanel.add(receiverNameField);
        formPanel.add(new JLabel("Receiver Address:"));
        formPanel.add(receiverAddressField);
        formPanel.add(new JLabel("Receiver Contact:"));
        formPanel.add(receiverContactField);
        formPanel.add(new JLabel("Package Contents:"));
        formPanel.add(contentsField);
        formPanel.add(new JLabel("Weight (kg):"));
        formPanel.add(weightField);
        formPanel.add(new JLabel("Dimensions (LxWxH):"));
        formPanel.add(dimensionsField);
        formPanel.add(new JLabel("Delivery Date (YYYY-MM-DD):"));
        formPanel.add(deliveryDateField);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusCombo);
        formPanel.add(new JLabel("Priority:"));
        formPanel.add(priorityCombo);
        formPanel.add(new JLabel("Current Location:"));
        formPanel.add(locationField);

        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(ev -> {
            try {
                // Validate inputs
                if (!ValidationUtils.validateRequiredFields(
                        senderNameField, senderAddressField, senderContactField,
                        receiverNameField, receiverAddressField, receiverContactField,
                        contentsField, weightField, dimensionsField, deliveryDateField)) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all required fields",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double weight = Double.parseDouble(weightField.getText());
                LocalDate deliveryDate = LocalDate.parse(deliveryDateField.getText());

                shipment.setSenderName(senderNameField.getText());
                shipment.setSenderAddress(senderAddressField.getText());
                shipment.setSenderContact(senderContactField.getText());
                shipment.setReceiverName(receiverNameField.getText());
                shipment.setReceiverAddress(receiverAddressField.getText());
                shipment.setReceiverContact(receiverContactField.getText());
                shipment.setPackageContents(contentsField.getText());
                shipment.setWeight(weight);
                shipment.setDimensions(dimensionsField.getText());
                shipment.setDeliveryDate(deliveryDate);
                shipment.setStatus((String) statusCombo.getSelectedItem());
                shipment.setPriority((String) priorityCombo.getSelectedItem());
                shipment.setCurrentLocation(locationField.getText());

                dataService.updateShipment(shipment);

                // Notify customer if status changed
                if (!shipment.getStatus().equals("PENDING")) {
                    notificationService.sendCustomerNotification(
                            shipment.getReceiverContact(),
                            "Your shipment " + shipment.getShipmentId() + " status updated to: " +
                                    shipment.getStatus() + ". Current location: " + shipment.getCurrentLocation()
                    );
                }

                dialog.dispose();
                loadShipments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteShipment(ActionEvent e) {
        int selectedRow = shipmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a shipment to delete",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String shipmentId = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete shipment " + shipmentId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dataService.deleteShipment(shipmentId);
            loadShipments();
        }
    }

    private void assignDriver(ActionEvent e) {
        int selectedRow = shipmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a shipment first",
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

        List<Driver> availableDrivers = dataService.getAvailableDrivers();
        if (availableDrivers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No available drivers",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog();
        dialog.setTitle("Assign Driver to Shipment");
        dialog.setSize(400, 300);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Select Driver for Shipment: " + shipmentId));

        DefaultListModel<String> driverListModel = new DefaultListModel<>();
        for (Driver driver : availableDrivers) {
            driverListModel.addElement(driver.getDriverId() + " - " + driver.getName() +
                    " (" + driver.getVehicleType() + ")");
        }

        JList<String> driverList = new JList<>(driverListModel);
        driverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(driverList);
        panel.add(scrollPane);

        JButton assignButton = new JButton("Assign");
        assignButton.addActionListener(ev -> {
            int selectedDriverIndex = driverList.getSelectedIndex();
            if (selectedDriverIndex < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select a driver",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Driver selectedDriver = availableDrivers.get(selectedDriverIndex);
            shipment.setAssignedDriverId(selectedDriver.getDriverId());
            shipment.setStatus("IN_TRANSIT");

            // Update driver's availability and assigned shipments
            selectedDriver.setAvailable(false);
            selectedDriver.getAssignedShipments().add(shipment.getShipmentId());

            dataService.updateShipment(shipment);
            dataService.updateDriver(selectedDriver);

            // Notify both customer and driver
            notificationService.sendCustomerNotification(
                    shipment.getReceiverContact(),
                    "Your shipment " + shipment.getShipmentId() + " has been assigned to driver " +
                            selectedDriver.getName() + " (" + selectedDriver.getContact() + ")"
            );

            notificationService.sendDriverNotification(
                    selectedDriver.getContact(),
                    "You have been assigned to deliver shipment " + shipment.getShipmentId() +
                            " from " + shipment.getSenderName() + " to " + shipment.getReceiverName() +
                            ". Pickup at warehouse."
            );

            dialog.dispose();
            loadShipments();
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(assignButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}