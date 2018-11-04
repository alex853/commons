package net.simforge.commons.misc;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class JavaTime {

    public static final DateTimeFormatter yMdHms = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter yMd = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter Hms = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter hhmm = DateTimeFormatter.ofPattern("HH:mm");

    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }

    public static LocalDate todayUtc() {
        return LocalDate.now(ZoneId.of("UTC"));
    }

    public static String toHhmm(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes();
        return String.format("%02d:%02d", hours, minutes - hours * 60);
    }

    public static Duration hhmmToDuration(String duration) {
        if (duration.length() != 5) {
            throw new IllegalArgumentException("Unsupported input format: " + duration);
        }
        String[] strs = duration.split(":");
        if (strs.length != 2) {
            throw new IllegalArgumentException("Unsupported input format: " + duration);
        }
        int h = Integer.parseInt(strs[0]);
        int m = Integer.parseInt(strs[1]);

        if (m < 0 || m >= 60) {
            throw new IllegalArgumentException("Unsupported input format: " + duration);
        }

        return Duration.ofHours(h).plusMinutes(m);
    }

    public static String toHhmm(LocalTime localTime) {
        long hours = localTime.getHour();
        long minutes = localTime.getMinute();
        return String.format("%02d:%02d", hours, minutes);
    }

    public static LocalTime hhmmToLocalTime(String time) {
        return LocalTime.parse(time);
    }

    public static double hoursBetween(LocalDateTime from, LocalDateTime to) {
        Duration duration = Duration.between(from, to);
        return duration.getSeconds() / 3600.0;
    }
}
