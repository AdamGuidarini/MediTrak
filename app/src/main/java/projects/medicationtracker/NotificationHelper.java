package projects.medicationtracker;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static android.content.Context.ALARM_SERVICE;
import static projects.medicationtracker.TimeFormatting.*;

import androidx.core.app.NotificationCompat;

public class NotificationHelper
{
    final static String GROUP_KEY = "medicationTrackerNotificationGroup";
    final static String CHANNEL_ID = "med_reminder";

    public static void scheduleNotification(Context notificationContext, Medication medication,
                                            LocalDateTime time, long notificationId)
    {
        final String TITLE = notificationContext.getString(R.string.app_name);
        PendingIntent alarmIntent;
        AlarmManager alarmManager;

        final DBHelper db = new DBHelper(notificationContext);

        Notification notification = createNotification(TITLE, notificationContext, medication);

        String doseTime = localDateTimeToString(medication.getStartDate());
        // long id = db.getDoseId(medication.getMedId(), doseTime);

        ZonedDateTime zdt = time.atZone(ZoneId.systemDefault());

        long alarmTimeMillis = zdt.toInstant().toEpochMilli();

        Intent notificationIntent = new Intent(notificationContext, NotificationReceiver.class);

        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);
        // notificationIntent.putExtra("DOSE_ID", id);

        alarmIntent = PendingIntent.getBroadcast(notificationContext, (int) notificationId,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager = (AlarmManager) notificationContext.getSystemService(ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeMillis, alarmIntent);
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTimeMillis, AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    private static Notification createNotification(String title, Context notificationContext,
                                                   Medication medication)
    {
        NotificationCompat.Builder builder
                = new NotificationCompat.Builder(notificationContext, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(createMedicationReminderMessage(medication))
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true)
                .setGroup(GROUP_KEY)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL);

        Intent resIntent
                = new Intent(notificationContext.getApplicationContext(), MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(notificationContext);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resIntent);

        PendingIntent resPendingIntent
                = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resPendingIntent);

        return builder.build();
    }

    private static String createMedicationReminderMessage(Medication medication)
    {
        String message;
        String patientName = medication.getPatientName();
        String medicationName = medication.getMedName();

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

}
