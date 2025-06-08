package views;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;

    public MainFrame() {
        setTitle("FastTrack Logistics System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            initComponents();
            initializeDataFiles();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error initializing application: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();

        // Add all panels as tabs
        tabbedPane.addTab("Shipments", new ShipmentPanel());
        tabbedPane.addTab("Drivers", new DriverPanel());
        tabbedPane.addTab("Schedule", new SchedulePanel());
        tabbedPane.addTab("Tracking", new TrackingPanel());
        tabbedPane.addTab("Reports", new ReportPanel());
        tabbedPane.addTab("Notifications", new NotificationPanel());

        add(tabbedPane);
    }

    private void initializeDataFiles() {
        // Create data directory if it doesn't exist
        java.io.File dataDir = new java.io.File("data");
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }

        // Create empty files if they don't exist
        String[] files = {"drivers.csv", "shipments.csv", "notifications.csv", "schedules.csv"};
        for (String file : files) {
            java.io.File dataFile = new java.io.File("data/" + file);
            if (!dataFile.exists()) {
                try {
                    dataFile.createNewFile();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error creating data files: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}