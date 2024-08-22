package projects.medicationtracker.Models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.TimeFormatting;

public class Notification {
    private final long id;
    private final long medId;
    private final long notificationId;
    private final LocalDateTime doseTime;

    public Notification(long rowId, long medicationId, long notificationId, LocalDateTime dosageTime) {
        id = rowId;
        medId = medicationId;
        this.notificationId = notificationId;
        doseTime = dosageTime;
    }

    public Notification(long rowId, long medicationId, long notificationId, String dosageTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DBHelper.DateFormats.DB_DATE_FORMAT, Locale.getDefault());

        id = rowId;
        medId = medicationId;
        this.notificationId = notificationId;
        doseTime = LocalDateTime.parse(dosageTime, formatter);
    }

    public long getId() {
        return id;
    }

    public long getMedId() {
        return medId;
    }

    public long getNotificationId() {
        return notificationId;
    }

    public LocalDateTime getDoseTime() {
        return doseTime;
    }

    public String getDoseTimeString() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DBHelper.DateFormats.DB_DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(doseTime);
    }
}
