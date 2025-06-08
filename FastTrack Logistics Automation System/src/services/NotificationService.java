package services;

import models.Notification;
import utils.FileUtils;

import java.util.List;
import java.util.stream.Collectors;

public class NotificationService {
    private static final String NOTIFICATIONS_FILE = "data/notifications.csv";
    private DataService dataService;

    public NotificationService() {
        this.dataService = new DataService();
    }

    public void sendCustomerNotification(String customerContact, String message) {
        String notificationId = "CUST-" + System.currentTimeMillis();
        Notification notification = new Notification(
                notificationId, customerContact, message, "SMS", "PENDING"
        );
        saveNotification(notification);

        // In a real application, integrate with SMS/email gateway here
        System.out.println("Sending notification to customer: " + customerContact);
        System.out.println("Message: " + message);

        // Mark as sent
        notification.setStatus("SENT");
        updateNotification(notification);
    }

    public void sendDriverNotification(String driverContact, String message) {
        String notificationId = "DRIV-" + System.currentTimeMillis();
        Notification notification = new Notification(
                notificationId, driverContact, message, "SMS", "PENDING"
        );
        saveNotification(notification);

        // In a real application, integrate with SMS gateway here
        System.out.println("Sending notification to driver: " + driverContact);
        System.out.println("Message: " + message);

        // Mark as sent
        notification.setStatus("SENT");
        updateNotification(notification);
    }

    public List<Notification> getAllNotifications() {
        List<String> lines = FileUtils.readLines(NOTIFICATIONS_FILE);
        return lines.stream()
                .map(Notification::fromString)
                .filter(n -> n != null)
                .collect(Collectors.toList());
    }

    private void saveNotification(Notification notification) {
        List<Notification> notifications = getAllNotifications();
        notifications.add(notification);
        saveAllNotifications(notifications);
    }

    private void updateNotification(Notification updatedNotification) {
        List<Notification> notifications = getAllNotifications();
        notifications = notifications.stream()
                .map(n -> n.getNotificationId().equals(updatedNotification.getNotificationId()) ?
                        updatedNotification : n)
                .collect(Collectors.toList());
        saveAllNotifications(notifications);
    }

    private void saveAllNotifications(List<Notification> notifications) {
        List<String> lines = notifications.stream()
                .map(Notification::toString)
                .collect(Collectors.toList());
        FileUtils.writeLines(NOTIFICATIONS_FILE, lines);
    }
}