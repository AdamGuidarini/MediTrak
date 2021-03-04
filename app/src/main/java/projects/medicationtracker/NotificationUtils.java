package projects.medicationtracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

public class NotificationUtils extends ContextWrapper
{
    private NotificationManager manager;
    public static final String CHANNEL_ID = "projects.medicationtracker";
    public static final String CHANNEL_NAME = "MEDICATION CHANNEL";

    public NotificationUtils(Context base)
    {
        super(base);
    }

    public void createChannel ()
    {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(channel);
    }

    NotificationManager getManager()
    {
        if (manager == null)
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        return manager;
    }

    // TODO Add action button
    // TODO create app icon for "setSmallIcon"
    public Notification.Builder getChannelNotification (String title, String body)
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification.Builder builder =  new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= 29)
            builder.setAllowSystemGeneratedContextualActions(false);

        return builder;
    }
}
