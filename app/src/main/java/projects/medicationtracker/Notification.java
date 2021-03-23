package projects.medicationtracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static android.content.Context.ALARM_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

public class Notification
{
    private static final String TITLE = "Medication Tracker";
    private final String notificationMessage;
    private final LocalDateTime notificationTime;
    private final Context notificationContext;

    Notification (LocalDateTime time, String message, Context context)
    {
        notificationMessage = message;
        notificationTime = time;
        notificationContext = context;
    }

    private void scheduleNotification (android.app.Notification notification, int id)
    {
        Intent notificationIntent = new Intent(notificationContext, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, id);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(notificationContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        ZonedDateTime zdt = notificationTime.atZone(ZoneId.systemDefault());
        long delay = zdt.toInstant().toEpochMilli();
        AlarmManager alarmManager = (AlarmManager) notificationContext.getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, delay, pendingIntent);
    }

    public android.app.Notification createNotification ()
    {
        final String CHANNEL_ID = "projects.medicationtracker";

        Intent intent = new Intent(notificationContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(notificationContext, 0, intent, 0);

        android.app.Notification.Builder builder =  new android.app.Notification.Builder(notificationContext, CHANNEL_ID)
                .setContentTitle(TITLE)
                .setContentText(notificationMessage)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= 29)
            builder.setAllowSystemGeneratedContextualActions(false);

        return builder.build();
    }

    public String createMedicationReminderMessage (String medicationName, String patientName)
    {
        String message;

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
