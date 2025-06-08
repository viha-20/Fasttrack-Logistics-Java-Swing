package services;

import models.DeliverySchedule;
import models.Driver;
import models.Shipment;
import utils.DateUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class DeliveryService {
    private DataService dataService;

    public DeliveryService() {
        this.dataService = new DataService();
    }

    public boolean scheduleDelivery(String shipmentId, String driverId, LocalDate date, String timeSlot) {
        Shipment shipment = dataService.getAllShipments().stream()
                .filter(s -> s.getShipmentId().equals(shipmentId))
                .findFirst()
                .orElse(null);

        if (shipment == null) {
            return false;
        }

        Driver driver = dataService.getAllDrivers().stream()
                .filter(d -> d.getDriverId().equals(driverId))
                .findFirst()
                .orElse(null);

        if (driver == null || !driver.isAvailable()) {
            return false;
        }

        String scheduleId = "SCH" + System.currentTimeMillis();
        DeliverySchedule schedule = new DeliverySchedule(
                scheduleId, shipmentId, driverId, date, timeSlot, "PENDING"
        );

        // Update shipment status and assigned driver
        shipment.setAssignedDriverId(driverId);
        shipment.setStatus("SCHEDULED");
        dataService.updateShipment(shipment);

        // Update driver availability
        driver.setAvailable(false);
        driver.getAssignedShipments().add(shipmentId);
        dataService.updateDriver(driver);

        // Save the schedule
        dataService.saveSchedule(schedule);
        return true;
    }

    public List<DeliverySchedule> getSchedulesForDriver(String driverId, LocalDate date) {
        return dataService.getAllSchedules().stream()
                .filter(s -> s.getDriverId().equals(driverId) &&
                        s.getScheduleDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<DeliverySchedule> getSchedulesForDate(LocalDate date) {
        return dataService.getAllSchedules().stream()
                .filter(s -> s.getScheduleDate().equals(date))
                .collect(Collectors.toList());
    }

    public boolean updateScheduleStatus(String scheduleId, String status) {
        DeliverySchedule schedule = dataService.getAllSchedules().stream()
                .filter(s -> s.getScheduleId().equals(scheduleId))
                .findFirst()
                .orElse(null);

        if (schedule == null) {
            return false;
        }

        schedule.setStatus(status);

        // Update shipment status if delivery is completed
        if (status.equals("COMPLETED")) {
            Shipment shipment = dataService.getAllShipments().stream()
                    .filter(s -> s.getShipmentId().equals(schedule.getShipmentId()))
                    .findFirst()
                    .orElse(null);

            if (shipment != null) {
                shipment.setStatus("DELIVERED");
                dataService.updateShipment(shipment);
            }

            // Mark driver as available
            Driver driver = dataService.getAllDrivers().stream()
                    .filter(d -> d.getDriverId().equals(schedule.getDriverId()))
                    .findFirst()
                    .orElse(null);

            if (driver != null) {
                driver.setAvailable(true);
                dataService.updateDriver(driver);
            }
        }

        // In a real implementation, we would update the schedule in the database
        // For now, we'll recreate all schedules with the updated one
        List<DeliverySchedule> schedules = dataService.getAllSchedules();
        schedules = schedules.stream()
                .map(s -> s.getScheduleId().equals(scheduleId) ? schedule : s)
                .collect(Collectors.toList());

        dataService.saveAllSchedules(schedules);
        return true;
    }

    public List<Driver> findAvailableDrivers(LocalDate date, String timeSlot) {
        // Get drivers not assigned to any shipments on this date/time
        List<String> busyDriverIds = dataService.getAllSchedules().stream()
                .filter(s -> s.getScheduleDate().equals(date) &&
                        s.getTimeSlot().equals(timeSlot))
                .map(DeliverySchedule::getDriverId)
                .collect(Collectors.toList());

        return dataService.getAllDrivers().stream()
                .filter(d -> !busyDriverIds.contains(d.getDriverId()) &&
                        d.isAvailable())
                .collect(Collectors.toList());
    }
}