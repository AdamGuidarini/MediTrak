package projects.medicationtracker;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static android.content.Context.ALARM_SERVICE;
import static projects.medicationtracker.TimeFormatting.*;

import androidx.core.app.NotificationCompat;

public class NotificationHelper
{
    public static void scheduleNotification(Context notificationContext, Medication medication, LocalDateTime time)
    {
        final String TITLE = "Medication Tracker";
        PendingIntent alarmIntent;
        AlarmManager alarmManager;

        final DBHelper db = new DBHelper(notificationContext);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(notificationContext)
                .setContentTitle(TITLE)
                .setContentText(createMedicationReminderMessage(medication))
                .setAutoCancel(true)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        Notification notification = builder.build();

        String doseTime = localDateTimeToString(medication.getStartDate());
        // long id = db.getDoseId(medication.getMedId(), doseTime);

        Intent notificationIntent = new Intent(notificationContext, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);

        alarmIntent = PendingIntent.getBroadcast(notificationContext, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        ZonedDateTime zdt = time.atZone(ZoneId.systemDefault());
        long timeInMillis = zdt.toInstant().toEpochMilli();

        alarmManager = (AlarmManager) notificationContext.getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeInMillis, alarmIntent);
    }

    private static String createMedicationReminderMessage(Medication medication)
    {
        String message;
        String patientName = medication.getPatientName();
        String medicationName = medication.getMedName();

        if (patientName.equals("ME!"))
        {
            if (medicationName.toLowerCase().equals("vitamin d"))
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
}
