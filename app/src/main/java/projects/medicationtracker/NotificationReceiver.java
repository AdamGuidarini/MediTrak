package projects.medicationtracker;

import static projects.medicationtracker.NotificationHelper.CHANNEL_ID;
import static projects.medicationtracker.NotificationHelper.DOSE_TIME;
import static projects.medicationtracker.NotificationHelper.MEDICATION_ID;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class NotificationReceiver extends BroadcastReceiver
{
    public static String NOTIFICATION_ID = "notification-id";
    public static String MESSAGE = "message";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent service = new Intent(context, NotificationService.class);
        Bundle extras = intent.getExtras();
        DBHelper db = new DBHelper(context);

        service.putExtra(NOTIFICATION_ID, extras.getLong(NOTIFICATION_ID, 0));
        service.putExtra(MESSAGE, extras.getString(MESSAGE));
        LocalDateTime doseTime = (LocalDateTime) extras.get(DOSE_TIME);
        long medicationId = extras.getLong(MEDICATION_ID);

        Medication medication = db.getMedication(medicationId);

        // Set new Intent for a new notification
        NotificationHelper.scheduleNotification(context, medication,
                doseTime.plusMinutes(medication.getMedFrequency()),
                medication.getMedId());

        context.startService(service);
    }
}
