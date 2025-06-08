package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Shipment {
    private String shipmentId;
    private String senderName;
    private String senderAddress;
    private String senderContact;
    private String receiverName;
    private String receiverAddress;
    private String receiverContact;
    private String packageContents;
    private double weight;
    private String dimensions;
    private LocalDate shipmentDate;
    private LocalDate deliveryDate;
    private String status; // PENDING, IN_TRANSIT, DELIVERED, DELAYED
    private String assignedDriverId;
    private String currentLocation;
    private String priority; // NORMAL, EXPRESS, URGENT

    public Shipment() {}

    public Shipment(String shipmentId, String senderName, String senderAddress,
                    String senderContact, String receiverName, String receiverAddress,
                    String receiverContact, String packageContents, double weight,
                    String dimensions, LocalDate shipmentDate, LocalDate deliveryDate,
                    String status, String assignedDriverId, String currentLocation,
                    String priority) {
        this.shipmentId = shipmentId;
        this.senderName = senderName;
        this.senderAddress = senderAddress;
        this.senderContact = senderContact;
        this.receiverName = receiverName;
        this.receiverAddress = receiverAddress;
        this.receiverContact = receiverContact;
        this.packageContents = packageContents;
        this.weight = weight;
        this.dimensions = dimensions;
        this.shipmentDate = shipmentDate;
        this.deliveryDate = deliveryDate;
        this.status = status;
        this.assignedDriverId = assignedDriverId;
        this.currentLocation = currentLocation;
        this.priority = priority;
    }

    // Getters and Setters
    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAddress() { return senderAddress; }
    public void setSenderAddress(String senderAddress) { this.senderAddress = senderAddress; }

    public String getSenderContact() { return senderContact; }
    public void setSenderContact(String senderContact) { this.senderContact = senderContact; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }

    public String getReceiverContact() { return receiverContact; }
    public void setReceiverContact(String receiverContact) { this.receiverContact = receiverContact; }

    public String getPackageContents() { return packageContents; }
    public void setPackageContents(String packageContents) { this.packageContents = packageContents; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }

    public LocalDate getShipmentDate() { return shipmentDate; }
    public void setShipmentDate(LocalDate shipmentDate) { this.shipmentDate = shipmentDate; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAssignedDriverId() { return assignedDriverId; }
    public void setAssignedDriverId(String assignedDriverId) { this.assignedDriverId = assignedDriverId; }

    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return shipmentId + "," + senderName + "," + senderAddress + "," + senderContact + "," +
                receiverName + "," + receiverAddress + "," + receiverContact + "," +
                packageContents + "," + weight + "," + dimensions + "," +
                shipmentDate.format(formatter) + "," + deliveryDate.format(formatter) + "," +
                status + "," + assignedDriverId + "," + currentLocation + "," + priority;
    }

    public static Shipment fromString(String str) {
        String[] parts = str.split(",");
        if (parts.length < 16) return null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return new Shipment(
                parts[0], parts[1], parts[2], parts[3], parts[4], parts[5],
                parts[6], parts[7], Double.parseDouble(parts[8]), parts[9],
                LocalDate.parse(parts[10], formatter), LocalDate.parse(parts[11], formatter),
                parts[12], parts[13], parts[14], parts[15]
        );
    }
}