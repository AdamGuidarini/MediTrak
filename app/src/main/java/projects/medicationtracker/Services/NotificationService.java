package projects.medicationtracker.Services;

import static android.os.Build.VERSION.SDK_INT;
import static projects.medicationtracker.Helpers.NotificationHelper.CHANNEL_ID;
import static projects.medicationtracker.Helpers.NotificationHelper.DOSE_TIME;
import static projects.medicationtracker.Helpers.NotificationHelper.GROUP_KEY;
import static projects.medicationtracker.Helpers.NotificationHelper.MEDICATION_ID;
import static projects.medicationtracker.Helpers.NotificationHelper.MESSAGE;
import static projects.medicationtracker.Helpers.NotificationHelper.NOTIFICATION_ID;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import projects.medicationtracker.MainActivity;
import projects.medicationtracker.R;
import projects.medicationtracker.Receivers.EventReceiver;

public class NotificationService extends IntentService
{
    public static String MARK_AS_TAKEN_ACTION = "markAsTaken";
    public static  String SNOOZE_ACTION = "snooze15";

    public NotificationService()
    {
        super("NotificationService");
    }

    /**
     * Handles intent sent from NotificationReceiver and issues notification.
     * @param intent Intent sent from NotificationReceiver.
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String message = intent.getStringExtra(MESSAGE);
        String doseTime = intent.getStringExtra(DOSE_TIME);
        long notificationId = intent.getLongExtra(NOTIFICATION_ID, System.currentTimeMillis());

        Notification notification = createNotification(
                message, doseTime, notificationId, intent.getLongExtra(MEDICATION_ID, 0)
        );

        notificationManager.notify((int) notificationId, notification);
    }

    /**
     * Creates a notification
     * @param message Message to display in the notification.
     * @return A built notification.
     */
    private Notification createNotification(
            String message, String doseTime, long notificationId, long medId)
    {
        Intent markTakenIntent = new Intent(this.getApplicationContext(), EventReceiver.class);
        Intent snoozeIntent = new Intent(this.getApplicationContext(), EventReceiver.class);
        String embeddedMedId = "_" + medId;

        markTakenIntent.removeExtra(DOSE_TIME);
        markTakenIntent.removeExtra(DOSE_TIME);

        markTakenIntent.setAction(MARK_AS_TAKEN_ACTION + embeddedMedId);
        markTakenIntent.putExtra(MEDICATION_ID + embeddedMedId, medId);
        markTakenIntent.putExtra(NOTIFICATION_ID + embeddedMedId, notificationId);
        markTakenIntent.putExtra(DOSE_TIME + embeddedMedId, doseTime);

        snoozeIntent.setAction(SNOOZE_ACTION + embeddedMedId);
        snoozeIntent.putExtra(MEDICATION_ID + embeddedMedId, medId);
        snoozeIntent.putExtra(NOTIFICATION_ID + embeddedMedId, notificationId);
        snoozeIntent.putExtra(DOSE_TIME + embeddedMedId, doseTime);

        PendingIntent markAsTakenPendingIntent =
                PendingIntent.getBroadcast(
                        this.getApplicationContext(),
                        0,
                        markTakenIntent,
                        SDK_INT >= Build.VERSION_CODES.S ?
                                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT
                );

        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(
                        getApplicationContext(),
                        0,
                        snoozeIntent,
                        SDK_INT >= Build.VERSION_CODES.S ?
                                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.drawable.mt_icon)
                .setAutoCancel(true)
                .setGroup(GROUP_KEY)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
                .addAction(
                        0,
                        getString(R.string.mark_as_taken),
                        markAsTakenPendingIntent
                )
                .addAction(
                        0,
                        getString(R.string.snooze_message),
                        snoozePendingIntent
                );

        Intent resIntent =
                new Intent(this.getApplicationContext(), MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resIntent);

        PendingIntent resPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        SDK_INT >= Build.VERSION_CODES.S ?
                                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_CANCEL_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resPendingIntent);

        return builder.build();
    }
}
