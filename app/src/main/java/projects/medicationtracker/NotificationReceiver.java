package projects.medicationtracker;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver
{
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent service = new Intent(context, NotificationService.class);
        service.putExtra(NOTIFICATION_ID, 0);
        service.putExtra(NOTIFICATION, (Notification) intent.getParcelableExtra(NOTIFICATION));

        context.startService(service);
    }

}
