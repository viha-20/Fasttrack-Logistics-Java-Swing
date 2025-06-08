package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {
    private String notificationId;
    private String recipient;
    private String message;
    private String type; // SMS, EMAIL
    private String status; // PENDING, SENT, FAILED
    private LocalDateTime timestamp;

    public Notification() {}

    public Notification(String notificationId, String recipient, String message,
                        String type, String status) {
        this.notificationId = notificationId;
        this.recipient = recipient;
        this.message = message;
        this.type = type;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return notificationId + "," + recipient + "," + message + "," +
                type + "," + status + "," + timestamp.format(formatter);
    }

    public static Notification fromString(String str) {
        String[] parts = str.split(",");
        if (parts.length < 6) return null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Notification notification = new Notification(
                parts[0], parts[1], parts[2], parts[3], parts[4]
        );
        notification.setTimestamp(LocalDateTime.parse(parts[5], formatter));
        return notification;
    }
}