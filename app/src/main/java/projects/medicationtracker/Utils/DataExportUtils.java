package projects.medicationtracker.Utils;

import static android.os.Build.VERSION.SDK_INT;

import static projects.medicationtracker.Receivers.ExportReceiver.EXPORT_ID;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import projects.medicationtracker.Receivers.ExportReceiver;

public class DataExportUtils {

    /**
     * Schedules a PendingIntent for exporting saved data
     * @param context Application Context
     * @param exportStart scheduled start of exports
     * @param frequency export frequency
     */
    public static void scheduleExport(Context context, LocalDateTime exportStart, int frequency) {
        LocalDateTime exportTime = getScheduledTime(exportStart, frequency);

        ZonedDateTime zonedExportTime = exportTime.atZone(ZoneId.systemDefault());
        long alarmTime = zonedExportTime.toInstant().toEpochMilli();

        PendingIntent alarmIntent;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent exportIntent = new Intent(context, ExportReceiver.class);

        int flags = SDK_INT >= Build.VERSION_CODES.S ?
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                : PendingIntent.FLAG_UPDATE_CURRENT;

        alarmIntent = PendingIntent.getBroadcast(context, EXPORT_ID, exportIntent, flags);
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
    }

    /**
     * Determines the what the next scheduled time should be
     * @param start Start of schedule
     * @param frequency Schedule frequency
     * @return Next scheduled LocalDateTime
     */
    private static LocalDateTime getScheduledTime(LocalDateTime start, int frequency) {
        LocalDateTime now = LocalDateTime.now();

        if (start.isAfter(now)) {
            return start;
        }

        long diff = ChronoUnit.HOURS.between(start, now);
        long cycles = (diff / frequency) + 1;

        return start.plusHours(cycles * frequency);
    }

    /**
     * Cancels PendingIntent for upcoming
     * @param context Application context
     */
    public static void cancel(Context context) {
        Intent intent = new Intent(context, ExportReceiver.class);

        PendingIntent.getBroadcast(
                context,
                EXPORT_ID,
                intent,
                SDK_INT >= Build.VERSION_CODES.S ?
                        PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );
    }
}
