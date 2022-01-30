package projects.medicationtracker;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static android.content.Context.ALARM_SERVICE;

public class NotificationHelper
{
    public final static String GROUP_KEY = "medicationTrackerNotificationGroup";
    public final static String CHANNEL_ID = "med_reminder";
    public final static String MESSAGE = "message";
    public final static String DOSE_TIME = "doseTime";
    public final static String MEDICATION_ID = "medicationId";

    public static void scheduleNotification(Context notificationContext, Medication medication,
                                            LocalDateTime time, long notificationId)
    {
        PendingIntent alarmIntent;
        AlarmManager alarmManager;
//        final DBHelper db = new DBHelper(notificationContext);

        ZonedDateTime zdt = time.atZone(ZoneId.systemDefault());
        long alarmTimeMillis = zdt.toInstant().toEpochMilli();

        Intent notificationIntent = new Intent(notificationContext, NotificationReceiver.class);

        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(MESSAGE, createMedicationReminderMessage(medication));
        notificationIntent.putExtra(DOSE_TIME, time);
        notificationIntent.putExtra(MEDICATION_ID, medication.getMedId());

        alarmIntent = PendingIntent.getBroadcast(notificationContext, (int) notificationId,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager = (AlarmManager) notificationContext.getSystemService(ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeMillis, alarmIntent);
    }

    private static String createMedicationReminderMessage(Medication medication)
    {
        String message;
        String patientName = medication.getPatientName();
        String medicationName = medication.getMedName();

        if (!medication.getAlias().isEmpty())
            medicationName = medication.getAlias();

        if (patientName.equals("ME!"))
        {
            if (medicationName.equalsIgnoreCase("vitamin d"))
                // Inside joke with a potential tested who repeatedly asked me
                // "did you take your vitamin D?" after I started taking it.
                message = "Did you take your Vitamin D?";
            else
                message = "It's time to take your " + medicationName;
        }
        else
            message = "It's time for " + patientName + "'s " + medicationName;

        return message;
    }

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

    public static void deletePendingNotification(Medication medication, Context context)
    {
        Intent intent = new Intent(context, NotificationReceiver.class);

        PendingIntent.getBroadcast(context, (int) medication.getMedId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT).cancel();
    }
}
