package services;

import models.DeliverySchedule;
import models.Driver;
import models.Shipment;
import utils.FileUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class DataService {
    private static final String DRIVERS_FILE = "data/drivers.csv";
    private static final String SHIPMENTS_FILE = "data/shipments.csv";
    private static final String SCHEDULES_FILE = "data/schedules.csv";

    // Driver operations
    public List<Driver> getAllDrivers() {
        List<String> lines = FileUtils.readLines(DRIVERS_FILE);
        return lines.stream()
                .map(Driver::fromString)
                .filter(d -> d != null)
                .collect(Collectors.toList());
    }

    public void saveDriver(Driver driver) {
        List<Driver> drivers = getAllDrivers();
        drivers.add(driver);
        saveAllDrivers(drivers);
    }

    public void updateDriver(Driver updatedDriver) {
        List<Driver> drivers = getAllDrivers();
        drivers = drivers.stream()
                .map(d -> d.getDriverId().equals(updatedDriver.getDriverId()) ? updatedDriver : d)
                .collect(Collectors.toList());
        saveAllDrivers(drivers);
    }

    public void deleteDriver(String driverId) {
        List<Driver> drivers = getAllDrivers();
        drivers = drivers.stream()
                .filter(d -> !d.getDriverId().equals(driverId))
                .collect(Collectors.toList());
        saveAllDrivers(drivers);
    }

    private void saveAllDrivers(List<Driver> drivers) {
        List<String> lines = drivers.stream()
                .map(Driver::toString)
                .collect(Collectors.toList());
        FileUtils.writeLines(DRIVERS_FILE, lines);
    }

    // Shipment operations
    public List<Shipment> getAllShipments() {
        List<String> lines = FileUtils.readLines(SHIPMENTS_FILE);
        return lines.stream()
                .map(Shipment::fromString)
                .filter(s -> s != null)
                .collect(Collectors.toList());
    }

    public void saveShipment(Shipment shipment) {
        List<Shipment> shipments = getAllShipments();
        shipments.add(shipment);
        saveAllShipments(shipments);
    }

    public void updateShipment(Shipment updatedShipment) {
        List<Shipment> shipments = getAllShipments();
        shipments = shipments.stream()
                .map(s -> s.getShipmentId().equals(updatedShipment.getShipmentId()) ? updatedShipment : s)
                .collect(Collectors.toList());
        saveAllShipments(shipments);
    }

    public void deleteShipment(String shipmentId) {
        List<Shipment> shipments = getAllShipments();
        shipments = shipments.stream()
                .filter(s -> !s.getShipmentId().equals(shipmentId))
                .collect(Collectors.toList());
        saveAllShipments(shipments);
    }

    private void saveAllShipments(List<Shipment> shipments) {
        List<String> lines = shipments.stream()
                .map(Shipment::toString)
                .collect(Collectors.toList());
        FileUtils.writeLines(SHIPMENTS_FILE, lines);
    }

    // Delivery Schedule operations
    public List<DeliverySchedule> getAllSchedules() {
        List<String> lines = FileUtils.readLines(SCHEDULES_FILE);
        return lines.stream()
                .map(DeliverySchedule::fromString)
                .filter(s -> s != null)
                .collect(Collectors.toList());
    }

    public void saveSchedule(DeliverySchedule schedule) {
        List<DeliverySchedule> schedules = getAllSchedules();
        schedules.add(schedule);
        saveAllSchedules(schedules);
    }

    public void saveAllSchedules(List<DeliverySchedule> schedules) {
        List<String> lines = schedules.stream()
                .map(DeliverySchedule::toString)
                .collect(Collectors.toList());
        FileUtils.writeLines(SCHEDULES_FILE, lines);
    }

    // Business logic methods
    public List<Driver> getAvailableDrivers() {
        return getAllDrivers().stream()
                .filter(Driver::isAvailable)
                .collect(Collectors.toList());
    }

    public List<Shipment> getPendingShipments() {
        return getAllShipments().stream()
                .filter(s -> s.getStatus().equals("PENDING"))
                .collect(Collectors.toList());
    }

    public List<Shipment> getShipmentsByDriver(String driverId) {
        return getAllShipments().stream()
                .filter(s -> s.getAssignedDriverId() != null &&
                        s.getAssignedDriverId().equals(driverId))
                .collect(Collectors.toList());
    }

    public List<Shipment> getShipmentsByDate(LocalDate date) {
        return getAllShipments().stream()
                .filter(s -> s.getDeliveryDate().equals(date))
                .collect(Collectors.toList());
    }
}