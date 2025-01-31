package projects.medicationtracker.Receivers;

import static projects.medicationtracker.Helpers.DBHelper.ENABLE_NOTIFICATIONS;
import static projects.medicationtracker.Utils.NotificationUtils.EXPORT_ALERT_CHANNEL_ID;
import static projects.medicationtracker.Utils.NotificationUtils.GROUP_KEY;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import java.time.LocalDateTime;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NativeDbHelper;
import projects.medicationtracker.R;
import projects.medicationtracker.Utils.DataExportUtils;
import projects.medicationtracker.Utils.TimeFormatting;

public class ExportReceiver extends BroadcastReceiver {
    public final static int EXPORT_ID = Integer.MAX_VALUE - 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        NativeDbHelper db = new NativeDbHelper(context);
        Bundle prefs = db.getSettings();
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String fileName = prefs.getString(DBHelper.EXPORT_FILE_NAME);
        boolean success = true;

        try {
            db.dbExport(fileName);
        } catch (Exception e) {
            success = false;
        }

        String message = success ?
                context.getString(R.string.successful_export, fileName)
                : context.getString(R.string.failed_export);

        LocalDateTime exportStart = TimeFormatting.stringToLocalDateTime(
                prefs.getString(DBHelper.EXPORT_START)
        );

        DataExportUtils.scheduleExport(
                context, exportStart, prefs.getInt(DBHelper.EXPORT_FREQUENCY)
        );

        Notification note = new NotificationCompat.Builder(context, EXPORT_ALERT_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.drawable.pill)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setSilent(success)
                .build();

        if (prefs.getBoolean(ENABLE_NOTIFICATIONS)) {
            manager.notify(EXPORT_ID, note);
        }
    }
}
