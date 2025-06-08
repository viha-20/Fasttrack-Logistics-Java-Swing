package services;

import models.Shipment;
import utils.DateUtils;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService {
    private DataService dataService;

    public ReportService() {
        this.dataService = new DataService();
    }

    public Map<String, Long> getMonthlyShipmentCounts(int year) {
        List<Shipment> shipments = dataService.getAllShipments();

        return shipments.stream()
                .filter(s -> s.getShipmentDate().getYear() == year)
                .collect(Collectors.groupingBy(
                        s -> s.getShipmentDate().getMonth().toString(),
                        Collectors.counting()
                ));
    }

    public Map<String, Long> getStatusDistribution() {
        List<Shipment> shipments = dataService.getAllShipments();

        return shipments.stream()
                .collect(Collectors.groupingBy(
                        Shipment::getStatus,
                        Collectors.counting()
                ));
    }

    public Map<String, Long> getDriverPerformance() {
        List<Shipment> shipments = dataService.getAllShipments();

        return shipments.stream()
                .filter(s -> s.getAssignedDriverId() != null)
                .collect(Collectors.groupingBy(
                        Shipment::getAssignedDriverId,
                        Collectors.counting()
                ));
    }

    public Map<String, Double> getMonthlyDeliveryTimes() {
        List<Shipment> shipments = dataService.getAllShipments();

        return shipments.stream()
                .filter(s -> s.getStatus().equals("DELIVERED"))
                .collect(Collectors.groupingBy(
                        s -> YearMonth.from(s.getDeliveryDate()).toString(),
                        Collectors.averagingDouble(s ->
                                DateUtils.daysBetween(s.getShipmentDate(), s.getDeliveryDate()))
                ));
    }

    public Map<String, Long> getPriorityDistribution() {
        List<Shipment> shipments = dataService.getAllShipments();

        return shipments.stream()
                .collect(Collectors.groupingBy(
                        Shipment::getPriority,
                        Collectors.counting()
                ));
    }
}