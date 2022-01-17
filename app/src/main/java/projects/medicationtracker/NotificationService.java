package projects.medicationtracker;

import static projects.medicationtracker.NotificationReceiver.NOTIFICATION;
import static projects.medicationtracker.NotificationReceiver.NOTIFICATION_ID;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

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

        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);

        notificationManager.notify(notificationId, notification);
    }
}
