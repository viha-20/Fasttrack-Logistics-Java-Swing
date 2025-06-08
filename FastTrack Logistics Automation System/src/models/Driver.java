package models;

import java.util.ArrayList;
import java.util.List;

public class Driver {
    private String driverId;
    private String name;
    private String contact;
    private String licenseNumber;
    private boolean available;
    private List<String> assignedShipments;
    private String vehicleType;

    public Driver() {
        this.assignedShipments = new ArrayList<>();
    }

    public Driver(String driverId, String name, String contact, String licenseNumber,
                  boolean available, String vehicleType) {
        this();
        this.driverId = driverId;
        this.name = name;
        this.contact = contact;
        this.licenseNumber = licenseNumber;
        this.available = available;
        this.vehicleType = vehicleType;
    }

    // Getters and Setters
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public List<String> getAssignedShipments() { return assignedShipments; }
    public void setAssignedShipments(List<String> assignedShipments) {
        this.assignedShipments = assignedShipments;
    }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    @Override
    public String toString() {
        return driverId + "," + name + "," + contact + "," + licenseNumber + "," +
                available + "," + vehicleType + "," + String.join(";", assignedShipments);
    }

    public static Driver fromString(String str) {
        String[] parts = str.split(",");
        if (parts.length < 6) return null;

        Driver driver = new Driver(
                parts[0], parts[1], parts[2], parts[3],
                Boolean.parseBoolean(parts[4]), parts[5]
        );

        if (parts.length > 6 && !parts[6].isEmpty()) {
            String[] shipments = parts[6].split(";");
            for (String shipment : shipments) {
                driver.getAssignedShipments().add(shipment);
            }
        }

        return driver;
    }
}