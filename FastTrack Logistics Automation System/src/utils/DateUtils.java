package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String formatDisplayDate(LocalDate date) {
        return date.format(DISPLAY_FORMATTER);
    }

    public static String formatDBDate(LocalDate date) {
        return date.format(DB_FORMATTER);
    }

    public static LocalDate parseDBDate(String dateString) {
        return LocalDate.parse(dateString, DB_FORMATTER);
    }

    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    public static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek().getValue() > 5;
    }

    public static LocalDate getNextBusinessDay(LocalDate date) {
        LocalDate result = date.plusDays(1);
        while (isWeekend(result)) {
            result = result.plusDays(1);
        }
        return result;
    }
}