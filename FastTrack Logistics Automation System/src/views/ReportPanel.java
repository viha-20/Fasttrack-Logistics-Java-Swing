package views;


import services.ReportService;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;

import java.util.Map;

public class ReportPanel extends JPanel {
    private ReportService reportService;
    private JTabbedPane reportTabs;

    public ReportPanel() {
        reportService = new ReportService();
        initUI();
        loadReports();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        reportTabs = new JTabbedPane();

        // Add tabs for different report types
        reportTabs.addTab("Monthly Shipments", createMonthlyShipmentsReport());
        reportTabs.addTab("Status Distribution", createStatusDistributionReport());
        reportTabs.addTab("Driver Performance", createDriverPerformanceReport());
        reportTabs.addTab("Delivery Times", createDeliveryTimesReport());
        reportTabs.addTab("Priority Distribution", createPriorityDistributionReport());

        add(reportTabs, BorderLayout.CENTER);

        // Refresh button
        JButton refreshButton = new JButton("Refresh Reports");
        refreshButton.addActionListener(e -> loadReports());
        add(refreshButton, BorderLayout.SOUTH);
    }

    private void loadReports() {
        reportTabs.setComponentAt(0, createMonthlyShipmentsReport());
        reportTabs.setComponentAt(1, createStatusDistributionReport());
        reportTabs.setComponentAt(2, createDriverPerformanceReport());
        reportTabs.setComponentAt(3, createDeliveryTimesReport());
        reportTabs.setComponentAt(4, createPriorityDistributionReport());
    }

    private JPanel createMonthlyShipmentsReport() {
        JPanel panel = new JPanel(new BorderLayout());

        // Year selection
        JPanel yearPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        yearPanel.add(new JLabel("Year:"));
        JComboBox<Integer> yearCombo = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear - 2; year <= currentYear + 1; year++) {
            yearCombo.addItem(year);
        }
        yearCombo.setSelectedItem(currentYear);
        yearPanel.add(yearCombo);

        JButton generateButton = new JButton("Generate Report");
        yearPanel.add(generateButton);

        // Table for results
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Month", "Shipment Count"}, 0);
        JTable reportTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reportTable);

        // Generate initial report
        generateMonthlyShipmentsReport(tableModel, currentYear);

        // Button action
        generateButton.addActionListener(e -> {
            int selectedYear = (Integer) yearCombo.getSelectedItem();
            generateMonthlyShipmentsReport(tableModel, selectedYear);
        });

        panel.add(yearPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void generateMonthlyShipmentsReport(DefaultTableModel tableModel, int year) {
        tableModel.setRowCount(0);
        Map<String, Long> monthlyCounts = reportService.getMonthlyShipmentCounts(year);

        for (Month month : Month.values()) {
            String monthName = month.toString();
            Long count = monthlyCounts.getOrDefault(monthName, 0L);
            tableModel.addRow(new Object[]{monthName, count});
        }
    }

    private JPanel createStatusDistributionReport() {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Status", "Count"}, 0);
        JTable reportTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reportTable);

        Map<String, Long> statusDistribution = reportService.getStatusDistribution();
        statusDistribution.forEach((status, count) ->
                tableModel.addRow(new Object[]{status, count}));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDriverPerformanceReport() {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Driver ID", "Deliveries Completed"}, 0);
        JTable reportTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reportTable);

        Map<String, Long> driverPerformance = reportService.getDriverPerformance();
        driverPerformance.forEach((driverId, count) ->
                tableModel.addRow(new Object[]{driverId, count}));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDeliveryTimesReport() {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Month", "Average Delivery Time (Days)"}, 0);
        JTable reportTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reportTable);

        Map<String, Double> deliveryTimes = reportService.getMonthlyDeliveryTimes();
        deliveryTimes.forEach((month, avgDays) ->
                tableModel.addRow(new Object[]{month, String.format("%.1f", avgDays)}));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPriorityDistributionReport() {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"Priority", "Count"}, 0);
        JTable reportTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(reportTable);

        Map<String, Long> priorityDistribution = reportService.getPriorityDistribution();
        priorityDistribution.forEach((priority, count) ->
                tableModel.addRow(new Object[]{priority, count}));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
}