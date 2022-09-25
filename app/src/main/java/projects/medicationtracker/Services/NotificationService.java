package projects.medicationtracker.Services;

import static projects.medicationtracker.Helpers.NotificationHelper.CHANNEL_ID;
import static projects.medicationtracker.Helpers.NotificationHelper.DOSE_TIME;
import static projects.medicationtracker.Helpers.NotificationHelper.GROUP_KEY;
import static projects.medicationtracker.Receivers.NotificationReceiver.MESSAGE;
import static projects.medicationtracker.Receivers.NotificationReceiver.NOTIFICATION_ID;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import projects.medicationtracker.MainActivity;
import projects.medicationtracker.R;
import projects.medicationtracker.Receivers.EventReceiver;

public class NotificationService extends IntentService
{
    public static String MARK_AS_TAKEN_ACTION = "markAsTaken";

    public NotificationService()
    {
        super("NotificationService");
    }

    /**
     * Handles intent sent from NotificationReceiver and issues notification.
     * @param intent Intent sent from NotificationReceiver.
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String message = intent.getStringExtra(MESSAGE);
        String doseTime = intent.getStringExtra(DOSE_TIME);
        long notificationId = intent.getLongExtra(NOTIFICATION_ID, System.currentTimeMillis());

        Notification notification = createNotification(message, doseTime, notificationId);

        notificationManager.notify((int) notificationId, notification);
    }

    /**
     * Creates a notification
     * @param message Message to display in the notification.
     * @return A built notification.
     */
    private Notification createNotification(String message, String doseTime, long notificationId)
    {
        Intent markTakenIntent = new Intent(this.getApplicationContext(), EventReceiver.class);
        markTakenIntent.setAction(MARK_AS_TAKEN_ACTION);
        markTakenIntent.putExtra(NOTIFICATION_ID,   notificationId);
        markTakenIntent.putExtra(DOSE_TIME, doseTime);

        PendingIntent markAsTakenPendingIntent =
                PendingIntent.getBroadcast(
                        this.getApplicationContext(),
                        0,
                        markTakenIntent,
                        0
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true)
                .setGroup(GROUP_KEY)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
                .addAction(
                        android.R.drawable.ic_dialog_info,
                        "Mark as Taken",
                        markAsTakenPendingIntent
                );

        Intent resIntent =
                new Intent(this.getApplicationContext(), MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resIntent);

        PendingIntent resPendingIntent=
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resPendingIntent);

        return builder.build();
    }
}
