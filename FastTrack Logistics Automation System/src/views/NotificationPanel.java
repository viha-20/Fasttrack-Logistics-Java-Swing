package views;

import models.Notification;
import services.NotificationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationPanel extends JPanel {
    private NotificationService notificationService;
    private JTable notificationTable;
    private DefaultTableModel tableModel;

    public NotificationPanel() {
        notificationService = new NotificationService();
        initUI();
        loadNotifications();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Table setup
        String[] columnNames = {"ID", "Recipient", "Type", "Status", "Message", "Timestamp"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        notificationTable = new JTable(tableModel);
        notificationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(notificationTable);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh");
        JButton resendButton = new JButton("Resend Failed");

        refreshButton.addActionListener(e -> loadNotifications());
        resendButton.addActionListener(e -> resendFailedNotifications());

        buttonPanel.add(refreshButton);
        buttonPanel.add(resendButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadNotifications() {
        tableModel.setRowCount(0);
        List<Notification> notifications = notificationService.getAllNotifications();
        for (Notification notification : notifications) {
            Object[] row = {
                    notification.getNotificationId(),
                    notification.getRecipient(),
                    notification.getType(),
                    notification.getStatus(),
                    notification.getMessage().length() > 50 ?
                            notification.getMessage().substring(0, 50) + "..." : notification.getMessage(),
                    notification.getTimestamp()
            };
            tableModel.addRow(row);
        }
    }

    private void resendFailedNotifications() {
        List<Notification> failedNotifications = notificationService.getAllNotifications().stream()
                .filter(n -> n.getStatus().equals("FAILED"))
                .collect(Collectors.toList());

        if (failedNotifications.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No failed notifications to resend",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int count = 0;
        for (Notification notification : failedNotifications) {
            if (notification.getType().equals("SMS")) {
                notificationService.sendDriverNotification(
                        notification.getRecipient(),
                        notification.getMessage()
                );
            } else {
                notificationService.sendCustomerNotification(
                        notification.getRecipient(),
                        notification.getMessage()
                );
            }
            count++;
        }

        JOptionPane.showMessageDialog(this,
                "Resent " + count + " failed notifications",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        loadNotifications();
    }
}