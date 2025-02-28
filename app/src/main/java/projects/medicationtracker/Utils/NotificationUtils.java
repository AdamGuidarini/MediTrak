package projects.medicationtracker.Utils;

import static android.content.Context.ALARM_SERVICE;
import static android.os.Build.VERSION.SDK_INT;

import static projects.medicationtracker.MediTrak.formatter;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.R;
import projects.medicationtracker.Receivers.NotificationReceiver;
import projects.medicationtracker.Models.Medication;

public class NotificationUtils {
    private final static int SNOOZE_TIME_MINUTES = 15;

    public final static String GROUP_KEY = "medicationTrackerNotificationGroup";
    public final static String MED_REMINDER_CHANNEL_ID = "med_reminder";
    public final static String EXPORT_ALERT_CHANNEL_ID = "export_alerts";
    public final static String MESSAGE = "message";
    public final static String DOSE_TIME = "doseTime";
    public final static String MEDICATION_ID = "medicationId";
    public final static String NOTIFICATION_ID = "notification-id";

    /**
     * Sets an alarm to create a notification. Ensures notification will fire in the future
     *
     * @param notificationContext Context for the alarm.
     * @param medication          Medication form which the user will be notified.
     * @param time                Time the notification will be set.
     * @param notificationId      ID for the PendingIntent that stores data for the notification.
     */
    public static void scheduleNotificationInFuture(Context notificationContext, Medication medication,
                                                    LocalDateTime time, long notificationId) {
        // Loops to increase time, prevents notification bombardment when editing time.
        while (time.isBefore(LocalDateTime.now())) {
            time = time.plusMinutes(medication.getFrequency());
        }

        ZonedDateTime zdt = time.atZone(ZoneId.systemDefault());
        long alarmTimeMillis = zdt.toInstant().toEpochMilli();

        createNotificationAlarm(notificationContext, alarmTimeMillis, medication, time, notificationId);
    }

    /**
     * Sets alarm for notification without ensuring the alarm is scheduled for the future.
     *
     * @param context        Context for the alarm.
     * @param medication     Medication form which the user will be notified.
     * @param scheduledTime  Time the notification will be set.
     * @param notificationId ID for the PendingIntent that stores data for the notification.
     */
    public static void scheduleNotification(
            Context context,
            Medication medication,
            LocalDateTime scheduledTime,
            long notificationId
    ) {
        ZonedDateTime zdt = scheduledTime.atZone(ZoneId.systemDefault());
        long alarmTimeMillis = zdt.toInstant().toEpochMilli();

        createNotificationAlarm(context, alarmTimeMillis, medication, scheduledTime, notificationId);
    }

    /**
     * Sets an alarm to create a notification in 15 minutes.
     *
     * @param notificationContext Context of notification.
     * @param medication          Medication form which the user will be notified.
     * @param time                Time the notification will be set.
     * @param notificationId      ID for the PendingIntent that stores data for the notification.
     */
    public static void scheduleIn15Minutes(
            Context notificationContext,
            Medication medication,
            LocalDateTime time,
            long notificationId
    ) {
        ZonedDateTime zdt = LocalDateTime.now()
                .plusMinutes(SNOOZE_TIME_MINUTES)
                .atZone(ZoneId.systemDefault());
        long alarmTimeMillis = zdt.toInstant().toEpochMilli();

        createNotificationAlarm(
                notificationContext, alarmTimeMillis, medication, time, notificationId
        );
    }

    /**
     * Creates an alarm with a pending intent for a notification
     *
     * @param notificationContext Context of notification.
     * @param alarmTime           Time in millis when alarm should fire.
     * @param medication          Medication form which the user will be notified.
     * @param time                Time the notification will be set.
     * @param notificationId      ID for the PendingIntent that stores data for the notification.
     */
    private static void createNotificationAlarm(
            Context notificationContext,
            long alarmTime, Medication medication,
            LocalDateTime time,
            long notificationId
    ) {
        PendingIntent alarmIntent;
        AlarmManager alarmManager;
        Intent notificationIntent = new Intent(notificationContext, NotificationReceiver.class);
        String message = createMedicationReminderMessage(medication, notificationContext);

        notificationIntent.putExtra(NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(MESSAGE,message);
        notificationIntent.putExtra(DOSE_TIME, time);
        notificationIntent.putExtra(MEDICATION_ID, medication.getId());

        alarmIntent = PendingIntent.getBroadcast(
                notificationContext,
                (int) notificationId,
                notificationIntent,
                SDK_INT >= Build.VERSION_CODES.S ?
                        PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );

        alarmManager = (AlarmManager) notificationContext.getSystemService(ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
    }

    /**
     * Creates a message for a notification.
     *
     * @param medication Medication corresponding to the notification.
     *                   * @param context Application context, needed for getString call
     * @return The content text to display in the notification.
     */
    private static String createMedicationReminderMessage(Medication medication, Context context) {
        String message;
        String patientName = medication.getPatientName();
        String medicationName = medication.getName();
        String dosage = formatter.format(medication.getDosage()) + " " + medication.getDosageUnits();

        if (medication.getAlias() != null && !medication.getAlias().isEmpty())
            medicationName = medication.getAlias();

        message = patientName.equals("ME!") ?
                context.getString(R.string.its_time_your_med, dosage, medicationName) :
                context.getString(R.string.time_for_other_med, patientName, dosage, medicationName);

        if (medication.getInstructions() != null && !medication.getInstructions().isEmpty()) {
            message += "\n\n" + context.getString(R.string.instructions) + ": " + medication.getInstructions();
        }

        return message;
    }

    /**
     * Creates a channel for notifications
     *
     * @param context Application context
     */
    public static void createNotificationChannels(Context context) {
        CharSequence reminderName = "Medication Reminder";
        CharSequence exportName = "Export Alerts";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationManager notificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel medChannel = new NotificationChannel(
                MED_REMINDER_CHANNEL_ID, reminderName, importance
        );

        NotificationChannel exportChannel = new NotificationChannel(
                EXPORT_ALERT_CHANNEL_ID, exportName, importance
        );

        for (NotificationChannel n : new NotificationChannel[]{medChannel, exportChannel}) {
            n.enableLights(false);
            n.enableVibration(false);
            n.setShowBadge(true);

            notificationManager.createNotificationChannel(n);
        }
    }

    /**
     * Deletes a pending intent based on context and notification ID
     *
     * @param notificationId ID of the notification to be deleted
     * @param context        Pending intent's context
     */
    public static void deletePendingNotification(long notificationId, Context context) {
        Intent intent = new Intent(context, NotificationReceiver.class);

        PendingIntent.getBroadcast(
                context,
                (int) notificationId,
                intent,
                SDK_INT >= Build.VERSION_CODES.S ?
                        PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                        : PendingIntent.FLAG_UPDATE_CURRENT
        ).cancel();
    }

    /**
     * Clears any and all pending notifications for a medication.
     *
     * @param medication Medication whose notifications should be cleared.
     * @param context    Application context.
     */
    public static void clearPendingNotifications(Medication medication, Context context) {
        DBHelper db = new DBHelper(context);

        long[] medIds = db.getMedicationTimeIds(medication);

        if (medIds.length == 0) {
            NotificationUtils.deletePendingNotification(medication.getId(), context);
        } else {
            for (long id : medIds) {
                NotificationUtils.deletePendingNotification(id * -1, context);
            }
        }

        db.close();
    }

    /**
     * Creates any and all notifications for a medication.
     *
     * @param medication Medication in need of notifications.
     * @param context    Application context.
     */
    public static void createNotifications(Medication medication, Context context) {
        DBHelper db = new DBHelper(context);
        long[] medicationTimeIds = db.getMedicationTimeIds(medication);
        LocalTime[] medTimes = db.getMedicationTimes(medication.getId());

        if (!db.isMedicationActive(medication) && medication.getId() != -1) {
            return;
        }

        if (medicationTimeIds.length == 1) {
            scheduleNotificationInFuture(
                    context,
                    medication,
                    LocalDateTime.of(
                            medication.getStartDate().toLocalDate(),
                            medTimes[0]
                    ),
                    medication.getId()
            );
        } else {
            for (int i = 0; i < medicationTimeIds.length; i++) {
                scheduleNotificationInFuture(
                        context,
                        medication,
                        LocalDateTime.of(
                                medication.getStartDate().toLocalDate(),
                                medTimes[i]
                        ),
                        medicationTimeIds[i] * -1
                );
            }
        }

        db.close();
    }
}
