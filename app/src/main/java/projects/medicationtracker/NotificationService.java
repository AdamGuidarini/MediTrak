package projects.medicationtracker;

import static projects.medicationtracker.NotificationHelper.CHANNEL_ID;
import static projects.medicationtracker.NotificationHelper.GROUP_KEY;
import static projects.medicationtracker.NotificationReceiver.MESSAGE;
import static projects.medicationtracker.NotificationReceiver.NOTIFICATION_ID;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class NotificationService extends IntentService
{
    public NotificationService()
    {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String message = intent.getStringExtra(MESSAGE);

        Notification notification = createNotification(this, message);

        long notificationId = intent.getLongExtra(NOTIFICATION_ID, System.currentTimeMillis());

        notificationManager.notify((int) notificationId, notification);
    }

    private Notification createNotification(Context notificationContext, String message)
    {
        NotificationCompat.Builder builder
                = new NotificationCompat.Builder(notificationContext, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
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
}
