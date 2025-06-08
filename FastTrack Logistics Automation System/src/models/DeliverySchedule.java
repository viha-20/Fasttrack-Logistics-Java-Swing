package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DeliverySchedule {
    private String scheduleId;
    private String shipmentId;
    private String driverId;
    private LocalDate scheduleDate;
    private String timeSlot; // MORNING, AFTERNOON, EVENING
    private String status; // PENDING, IN_PROGRESS, COMPLETED

    public DeliverySchedule() {}

    public DeliverySchedule(String scheduleId, String shipmentId, String driverId,
                            LocalDate scheduleDate, String timeSlot, String status) {
        this.scheduleId = scheduleId;
        this.shipmentId = shipmentId;
        this.driverId = driverId;
        this.scheduleDate = scheduleDate;
        this.timeSlot = timeSlot;
        this.status = status;
    }

    // Getters and Setters
    public String getScheduleId() { return scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }

    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public LocalDate getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(LocalDate scheduleDate) { this.scheduleDate = scheduleDate; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return scheduleId + "," + shipmentId + "," + driverId + "," +
                scheduleDate.format(formatter) + "," + timeSlot + "," + status;
    }

    public static DeliverySchedule fromString(String str) {
        String[] parts = str.split(",");
        if (parts.length < 6) return null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return new DeliverySchedule(
                parts[0], parts[1], parts[2],
                LocalDate.parse(parts[3], formatter), parts[4], parts[5]
        );
    }
}