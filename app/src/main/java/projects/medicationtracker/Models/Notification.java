package projects.medicationtracker.Models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.TimeFormatting;

public class Notification {
    private final long id;
    private final long medId;
    private final LocalDateTime doseTime;

    public Notification(long notificationId, long medicationId, LocalDateTime dosageTime) {
        id = notificationId;
        medId = medicationId;
        doseTime = dosageTime;
    }

    public Notification(long notificationId, long medicationId, String dosageTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DBHelper.DateFormats.DB_DATE_FORMAT, Locale.getDefault());

        id = notificationId;
        medId = medicationId;
        doseTime = LocalDateTime.parse(dosageTime, formatter);
    }

    public long getId() {
        return id;
    }

    public long getMedId() {
        return medId;
    }

    public LocalDateTime getDoseTime() {
        return doseTime;
    }

    public String getDoseTimeString() {
        return TimeFormatting.localDateTimeToDbString(doseTime);
    }
}
