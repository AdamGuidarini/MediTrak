package projects.medicationtracker;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.TimeZone;
import android.media.RingtoneManager;
import android.os.Build;
import android.widget.Toast;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;

import static android.content.Context.ALARM_SERVICE;
import static projects.medicationtracker.TimeFormatting.*;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper
{
    final static String CHANNEL_ID = "med_reminder";


    public static void scheduleNotification(Context notificationContext, Medication medication, LocalDateTime time, long notificationId)
    {
        final String TITLE = notificationContext.getString(R.string.app_name);
        PendingIntent alarmIntent;
        AlarmManager alarmManager;

        // final DBHelper db = new DBHelper(notificationContext);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(notificationContext, CHANNEL_ID)
                .setContentTitle(TITLE)
                .setContentText(createMedicationReminderMessage(medication))
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true);

        Intent resIntent = new Intent(notificationContext, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(notificationContext);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resIntent);

        PendingIntent resPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resPendingIntent);

        Notification notification = builder.build();

        String doseTime = localDateTimeToString(medication.getStartDate());
        // long id = db.getDoseId(medication.getMedId(), doseTime);

        Intent notificationIntent = new Intent(notificationContext, NotificationReceiver.class);

        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);

        alarmIntent = PendingIntent.getBroadcast(notificationContext, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager = (AlarmManager) notificationContext.getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, 2000, alarmIntent);
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
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = "Medication Reminder";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel( CHANNEL_ID, name, importance);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setShowBadge(true);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
