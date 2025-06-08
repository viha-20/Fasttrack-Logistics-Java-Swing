package views;

import models.Driver;
import services.DataService;
import services.NotificationService;
import utils.ValidationUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class DriverPanel extends JPanel {
    private DataService dataService;
    private NotificationService notificationService;
    private JTable driverTable;
    private DefaultTableModel tableModel;

    public DriverPanel() {
        dataService = new DataService();
        notificationService = new NotificationService();
        initUI();
        loadDrivers();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Table setup
        String[] columnNames = {"ID", "Name", "Contact", "License", "Available", "Vehicle Type"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        driverTable = new JTable(tableModel);
        driverTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(driverTable);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Driver");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton toggleButton = new JButton("Toggle Availability");

        addButton.addActionListener(this::addDriver);
        updateButton.addActionListener(this::updateDriver);
        deleteButton.addActionListener(this::deleteDriver);
        toggleButton.addActionListener(this::toggleAvailability);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(toggleButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadDrivers() {
        tableModel.setRowCount(0);
        List<Driver> drivers = dataService.getAllDrivers();
        for (Driver driver : drivers) {
            Object[] row = {
                    driver.getDriverId(),
                    driver.getName(),
                    driver.getContact(),
                    driver.getLicenseNumber(),
                    driver.isAvailable() ? "Yes" : "No",
                    driver.getVehicleType()
            };
            tableModel.addRow(row);
        }
    }

    private void addDriver(ActionEvent e) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Driver");
        dialog.setSize(400, 300);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        // Form fields
        JTextField idField = new JTextField("DR" + System.currentTimeMillis());
        idField.setEditable(false);
        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField licenseField = new JTextField();
        JComboBox<String> vehicleCombo = new JComboBox<>(new String[]{"BIKE", "CAR", "VAN", "TRUCK"});
        JCheckBox availableCheck = new JCheckBox("Available", true);

        formPanel.add(new JLabel("Driver ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Contact:"));
        formPanel.add(contactField);
        formPanel.add(new JLabel("License:"));
        formPanel.add(licenseField);
        formPanel.add(new JLabel("Vehicle Type:"));
        formPanel.add(vehicleCombo);
        formPanel.add(new JLabel("Available:"));
        formPanel.add(availableCheck);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(ev -> {
            if (!ValidationUtils.validateRequiredFields(nameField, contactField, licenseField)) {
                JOptionPane.showMessageDialog(dialog, "Please fill all required fields",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Driver driver = new Driver(
                    idField.getText(),
                    nameField.getText(),
                    contactField.getText(),
                    licenseField.getText(),
                    availableCheck.isSelected(),
                    (String) vehicleCombo.getSelectedItem()
            );

            dataService.saveDriver(driver);
            dialog.dispose();
            loadDrivers();
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void updateDriver(ActionEvent e) {
        int selectedRow = driverTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a driver to update",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String driverId = (String) tableModel.getValueAt(selectedRow, 0);
        Driver driver = dataService.getAllDrivers().stream()
                .filter(d -> d.getDriverId().equals(driverId))
                .findFirst()
                .orElse(null);

        if (driver == null) {
            JOptionPane.showMessageDialog(this, "Driver not found",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog();
        dialog.setTitle("Update Driver");
        dialog.setSize(400, 300);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        // Form fields with existing data
        JTextField idField = new JTextField(driver.getDriverId());
        idField.setEditable(false);
        JTextField nameField = new JTextField(driver.getName());
        JTextField contactField = new JTextField(driver.getContact());
        JTextField licenseField = new JTextField(driver.getLicenseNumber());
        JComboBox<String> vehicleCombo = new JComboBox<>(
                new String[]{"BIKE", "CAR", "VAN", "TRUCK"});
        vehicleCombo.setSelectedItem(driver.getVehicleType());
        JCheckBox availableCheck = new JCheckBox("Available", driver.isAvailable());

        formPanel.add(new JLabel("Driver ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Contact:"));
        formPanel.add(contactField);
        formPanel.add(new JLabel("License:"));
        formPanel.add(licenseField);
        formPanel.add(new JLabel("Vehicle Type:"));
        formPanel.add(vehicleCombo);
        formPanel.add(new JLabel("Available:"));
        formPanel.add(availableCheck);

        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(ev -> {
            if (!ValidationUtils.validateRequiredFields(nameField, contactField, licenseField)) {
                JOptionPane.showMessageDialog(dialog, "Please fill all required fields",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            driver.setName(nameField.getText());
            driver.setContact(contactField.getText());
            driver.setLicenseNumber(licenseField.getText());
            driver.setVehicleType((String) vehicleCombo.getSelectedItem());
            driver.setAvailable(availableCheck.isSelected());

            dataService.updateDriver(driver);
            dialog.dispose();
            loadDrivers();
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteDriver(ActionEvent e) {
        int selectedRow = driverTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a driver to delete",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String driverId = (String) tableModel.getValueAt(selectedRow, 0);

        // Check if driver has assigned shipments
        boolean hasAssignments = dataService.getAllShipments().stream()
                .anyMatch(s -> s.getAssignedDriverId() != null &&
                        s.getAssignedDriverId().equals(driverId));

        if (hasAssignments) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete driver with assigned shipments. Reassign shipments first.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete driver " + driverId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dataService.deleteDriver(driverId);
            loadDrivers();
        }
    }

    private void toggleAvailability(ActionEvent e) {
        int selectedRow = driverTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a driver",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String driverId = (String) tableModel.getValueAt(selectedRow, 0);
        Driver driver = dataService.getAllDrivers().stream()
                .filter(d -> d.getDriverId().equals(driverId))
                .findFirst()
                .orElse(null);

        if (driver == null) {
            JOptionPane.showMessageDialog(this, "Driver not found",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if driver has pending shipments when trying to make unavailable
        if (driver.isAvailable()) {
            boolean hasPending = dataService.getAllShipments().stream()
                    .anyMatch(s -> s.getAssignedDriverId() != null &&
                            s.getAssignedDriverId().equals(driverId) &&
                            !s.getStatus().equals("DELIVERED"));

            if (hasPending) {
                JOptionPane.showMessageDialog(this,
                        "Driver has pending shipments. Cannot mark as unavailable.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        driver.setAvailable(!driver.isAvailable());
        dataService.updateDriver(driver);

        // Notify driver
        notificationService.sendDriverNotification(
                driver.getContact(),
                "Your availability has been updated to: " + (driver.isAvailable() ? "AVAILABLE" : "UNAVAILABLE")
        );

        loadDrivers();
    }
}