package views;

import models.DeliverySchedule;
import models.Driver;
import models.Shipment;
import services.DataService;
import services.DeliveryService;
import services.NotificationService;
import utils.DateUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class SchedulePanel extends JPanel {
    private DataService dataService;
    private DeliveryService deliveryService;
    private NotificationService notificationService;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> dateFilterCombo;
    private JComboBox<String> driverFilterCombo;

    public SchedulePanel() {
        dataService = new DataService();
        deliveryService = new DeliveryService();
        notificationService = new NotificationService();
        initUI();
        loadSchedules();
        populateFilters();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by Date:"));
        dateFilterCombo = new JComboBox<>();
        dateFilterCombo.addItem("All Dates");
        dateFilterCombo.addActionListener(e -> filterSchedules());
        filterPanel.add(dateFilterCombo);

        filterPanel.add(new JLabel("Filter by Driver:"));
        driverFilterCombo = new JComboBox<>();
        driverFilterCombo.addItem("All Drivers");
        driverFilterCombo.addActionListener(e -> filterSchedules());
        filterPanel.add(driverFilterCombo);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            populateFilters();
            loadSchedules();
        });
        filterPanel.add(refreshButton);

        // Table setup
        String[] columnNames = {"Schedule ID", "Shipment ID", "Driver ID", "Date", "Time Slot", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scheduleTable = new JTable(tableModel);
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(scheduleTable);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Schedule");
        JButton updateButton = new JButton("Update Status");
        JButton deleteButton = new JButton("Delete");

        addButton.addActionListener(this::addSchedule);
        updateButton.addActionListener(this::updateStatus);
        deleteButton.addActionListener(this::deleteSchedule);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        add(filterPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadSchedules() {
        tableModel.setRowCount(0);
        List<DeliverySchedule> schedules = dataService.getAllSchedules();
        for (DeliverySchedule schedule : schedules) {
            Object[] row = {
                    schedule.getScheduleId(),
                    schedule.getShipmentId(),
                    schedule.getDriverId(),
                    schedule.getScheduleDate(),
                    schedule.getTimeSlot(),
                    schedule.getStatus()
            };
            tableModel.addRow(row);
        }
    }

    private void populateFilters() {
        dateFilterCombo.removeAllItems();
        dateFilterCombo.addItem("All Dates");

        List<DeliverySchedule> schedules = dataService.getAllSchedules();
        schedules.stream()
                .map(DeliverySchedule::getScheduleDate)
                .distinct()
                .sorted()
                .forEach(date -> dateFilterCombo.addItem(date.toString()));

        driverFilterCombo.removeAllItems();
        driverFilterCombo.addItem("All Drivers");

        List<Driver> drivers = dataService.getAllDrivers();
        drivers.forEach(driver -> driverFilterCombo.addItem(driver.getDriverId() + " - " + driver.getName()));
    }

    private void filterSchedules() {
        String selectedDate = (String) dateFilterCombo.getSelectedItem();
        String selectedDriver = (String) driverFilterCombo.getSelectedItem();

        List<DeliverySchedule> schedules = dataService.getAllSchedules();

        if (selectedDate != null && !"All Dates".equals(selectedDate)) {
            try {
                LocalDate filterDate = LocalDate.parse(selectedDate);
                schedules = schedules.stream()
                        .filter(s -> s.getScheduleDate().equals(filterDate))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid date format in filter",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (selectedDriver != null && !"All Drivers".equals(selectedDriver)) {
            try {
                String driverId = selectedDriver.split(" - ")[0];
                schedules = schedules.stream()
                        .filter(s -> s.getDriverId().equals(driverId))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid driver selection",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        tableModel.setRowCount(0);
        for (DeliverySchedule schedule : schedules) {
            Object[] row = {
                    schedule.getScheduleId(),
                    schedule.getShipmentId(),
                    schedule.getDriverId(),
                    schedule.getScheduleDate(),
                    schedule.getTimeSlot(),
                    schedule.getStatus()
            };
            tableModel.addRow(row);
        }
    }

    private void addSchedule(ActionEvent e) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Schedule New Delivery");
        dialog.setSize(500, 400);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        // Form fields
        List<Shipment> pendingShipments = dataService.getAllShipments().stream()
                .filter(s -> s.getStatus().equals("PENDING"))
                .collect(Collectors.toList());

        if (pendingShipments.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "No pending shipments available to schedule",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JComboBox<String> shipmentCombo = new JComboBox<>();
        pendingShipments.forEach(s -> shipmentCombo.addItem(s.getShipmentId() + " - " + s.getReceiverName()));

        JComboBox<String> dateCombo = new JComboBox<>();
        for (int i = 1; i <= 7; i++) {
            dateCombo.addItem(LocalDate.now().plusDays(i).toString());
        }

        JComboBox<String> timeCombo = new JComboBox<>(new String[]{"MORNING", "AFTERNOON", "EVENING"});

        formPanel.add(new JLabel("Shipment:"));
        formPanel.add(shipmentCombo);
        formPanel.add(new JLabel("Delivery Date:"));
        formPanel.add(dateCombo);
        formPanel.add(new JLabel("Time Slot:"));
        formPanel.add(timeCombo);

        // Driver selection
        LocalDate selectedDate = LocalDate.parse((String) dateCombo.getSelectedItem());
        String selectedTime = (String) timeCombo.getSelectedItem();

        List<Driver> availableDrivers = deliveryService.findAvailableDrivers(selectedDate, selectedTime);

        if (availableDrivers.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "No available drivers for selected date/time",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JComboBox<String> driverCombo = new JComboBox<>();
        availableDrivers.forEach(d -> driverCombo.addItem(d.getDriverId() + " - " + d.getName()));

        formPanel.add(new JLabel("Available Drivers:"));
        formPanel.add(driverCombo);

        // Add listeners for dynamic updates
        dateCombo.addActionListener(ev -> {
            LocalDate newDate = LocalDate.parse((String) dateCombo.getSelectedItem());
            String newTime = (String) timeCombo.getSelectedItem();
            updateAvailableDrivers(driverCombo, newDate, newTime);
        });

        timeCombo.addActionListener(ev -> {
            LocalDate newDate = LocalDate.parse((String) dateCombo.getSelectedItem());
            String newTime = (String) timeCombo.getSelectedItem();
            updateAvailableDrivers(driverCombo, newDate, newTime);
        });

        JButton saveButton = new JButton("Schedule Delivery");
        saveButton.addActionListener(ev -> {
            String shipmentId = ((String) shipmentCombo.getSelectedItem()).split(" - ")[0];
            LocalDate date = LocalDate.parse((String) dateCombo.getSelectedItem());
            String timeSlot = (String) timeCombo.getSelectedItem();
            String driverId = ((String) driverCombo.getSelectedItem()).split(" - ")[0];

            boolean success = deliveryService.scheduleDelivery(shipmentId, driverId, date, timeSlot);

            if (success) {
                JOptionPane.showMessageDialog(dialog, "Delivery scheduled successfully",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                // Notify driver and customer
                Shipment shipment = dataService.getAllShipments().stream()
                        .filter(s -> s.getShipmentId().equals(shipmentId))
                        .findFirst()
                        .orElse(null);

                Driver driver = dataService.getAllDrivers().stream()
                        .filter(d -> d.getDriverId().equals(driverId))
                        .findFirst()
                        .orElse(null);

                if (shipment != null && driver != null) {
                    notificationService.sendCustomerNotification(
                            shipment.getReceiverContact(),
                            "Your shipment " + shipmentId + " has been scheduled for delivery on " +
                                    date + " (" + timeSlot + "). Driver: " + driver.getName()
                    );

                    notificationService.sendDriverNotification(
                            driver.getContact(),
                            "You have been assigned to deliver shipment " + shipmentId + " on " +
                                    date + " (" + timeSlot + "). Customer: " + shipment.getReceiverName()
                    );
                }

                dialog.dispose();
                loadSchedules();
                populateFilters();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to schedule delivery",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void updateAvailableDrivers(JComboBox<String> driverCombo, LocalDate date, String timeSlot) {
        driverCombo.removeAllItems();
        List<Driver> availableDrivers = deliveryService.findAvailableDrivers(date, timeSlot);

        if (availableDrivers.isEmpty()) {
            driverCombo.addItem("No available drivers");
            driverCombo.setEnabled(false);
        } else {
            availableDrivers.forEach(d -> driverCombo.addItem(d.getDriverId() + " - " + d.getName()));
            driverCombo.setEnabled(true);
        }
    }

    private void updateStatus(ActionEvent e) {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a schedule to update",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String scheduleId = (String) tableModel.getValueAt(selectedRow, 0);
        DeliverySchedule schedule = dataService.getAllSchedules().stream()
                .filter(s -> s.getScheduleId().equals(scheduleId))
                .findFirst()
                .orElse(null);

        if (schedule == null) {
            JOptionPane.showMessageDialog(this, "Schedule not found",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog();
        dialog.setTitle("Update Schedule Status");
        dialog.setSize(300, 200);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Current Status: " + schedule.getStatus()));

        JComboBox<String> statusCombo = new JComboBox<>(
                new String[]{"PENDING", "IN_PROGRESS", "COMPLETED"});
        statusCombo.setSelectedItem(schedule.getStatus());
        panel.add(new JLabel("New Status:"));
        panel.add(statusCombo);

        JButton saveButton = new JButton("Update");
        saveButton.addActionListener(ev -> {
            String newStatus = (String) statusCombo.getSelectedItem();
            boolean success = deliveryService.updateScheduleStatus(scheduleId, newStatus);

            if (success) {
                JOptionPane.showMessageDialog(dialog, "Status updated successfully",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadSchedules();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update status",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(saveButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSchedule(ActionEvent e) {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a schedule to delete",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String scheduleId = (String) tableModel.getValueAt(selectedRow, 0);
        DeliverySchedule schedule = dataService.getAllSchedules().stream()
                .filter(s -> s.getScheduleId().equals(scheduleId))
                .findFirst()
                .orElse(null);

        if (schedule == null) {
            JOptionPane.showMessageDialog(this, "Schedule not found",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (schedule.getStatus().equals("IN_PROGRESS")) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete a schedule that is in progress",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this schedule?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Update shipment status if needed
            Shipment shipment = dataService.getAllShipments().stream()
                    .filter(s -> s.getShipmentId().equals(schedule.getShipmentId()))
                    .findFirst()
                    .orElse(null);

            if (shipment != null && shipment.getStatus().equals("SCHEDULED")) {
                shipment.setStatus("PENDING");
                shipment.setAssignedDriverId(null);
                dataService.updateShipment(shipment);
            }

            // Update driver availability if needed
            Driver driver = dataService.getAllDrivers().stream()
                    .filter(d -> d.getDriverId().equals(schedule.getDriverId()))
                    .findFirst()
                    .orElse(null);

            if (driver != null) {
                driver.setAvailable(true);
                dataService.updateDriver(driver);
            }

            // Delete the schedule
            List<DeliverySchedule> schedules = dataService.getAllSchedules();
            schedules = schedules.stream()
                    .filter(s -> !s.getScheduleId().equals(scheduleId))
                    .collect(Collectors.toList());

            dataService.saveAllSchedules(schedules);
            loadSchedules();
        }
    }
}