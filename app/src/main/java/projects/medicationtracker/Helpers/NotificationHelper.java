package projects.medicationtracker.Helpers;

import static android.content.Context.ALARM_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import projects.medicationtracker.SimpleClasses.Medication;
import projects.medicationtracker.Receivers.NotificationReceiver;

public class NotificationHelper
{
    public final static String GROUP_KEY = "medicationTrackerNotificationGroup";
    public final static String CHANNEL_ID = "med_reminder";
    public final static String MESSAGE = "message";
    public final static String DOSE_TIME = "doseTime";
    public final static String MEDICATION_ID = "medicationId";
    public final static String NOTIFICATION_ID = "notification-id";

    /**
     * Sets an alarm to create a notification.
     * @param notificationContext Context for the alarm.
     * @param medication Medication form which the user will be notified.
     * @param time Time the notification will be set.
     * @param notificationId ID for the PendingIntent that stores data for the notification.
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    public static void scheduleNotification(Context notificationContext, Medication medication,
                                            LocalDateTime time, long notificationId)
    {
        // Loops to increase time, prevents notification bombardment when editing time.
        while (time.isBefore(LocalDateTime.now().minusSeconds(1)))
        {
            time = time.plusMinutes(medication.getMedFrequency());
        }

        PendingIntent alarmIntent;
        AlarmManager alarmManager;

        ZonedDateTime zdt = time.atZone(ZoneId.systemDefault());
        long alarmTimeMillis = zdt.toInstant().toEpochMilli();

        Intent notificationIntent = new Intent(notificationContext, NotificationReceiver.class);

        notificationIntent.putExtra(NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(MESSAGE, createMedicationReminderMessage(medication));
        notificationIntent.putExtra(DOSE_TIME, time);
        notificationIntent.putExtra(MEDICATION_ID, medication.getMedId());

        alarmIntent = PendingIntent.getBroadcast(notificationContext, (int) notificationId,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager = (AlarmManager) notificationContext.getSystemService(ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeMillis, alarmIntent);
    }

    /**
     * Creates a message for a notification.
     * @param medication Medication corresponding to the notification.
     * @return The content text to display in the notification.
     */
    private static String createMedicationReminderMessage(Medication medication)
    {
        String message;
        String patientName = medication.getPatientName();
        String medicationName = medication.getMedName();

        if (!medication.getAlias().isEmpty())
            medicationName = medication.getAlias();

        message = patientName.equals("ME!") ?
            "It's time to take your " + medicationName :
            "It's time for " + patientName + "'s " + medicationName;

        return message;
    }

    /**
     * Creates a channel for notifications
     * @param context Application context
     */
    public static void createNotificationChannel(Context context)
    {
        CharSequence name = "Medication Reminder";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel( CHANNEL_ID, name, importance);
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setShowBadge(true);

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * Deletes a pending intent based on context and notification ID
     * @param notificationId ID of the notification to be deleted
     * @param context Pending intent's context
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    public static void deletePendingNotification(long notificationId, Context context)
    {
        Intent intent = new Intent(context, NotificationReceiver.class);

        PendingIntent.getBroadcast(context, (int) notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT).cancel();
    }
}
