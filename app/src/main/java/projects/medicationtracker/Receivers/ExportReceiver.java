package projects.medicationtracker.Receivers;

import static projects.medicationtracker.Utils.NotificationUtils.EXPORT_ALERT_CHANNEL_ID;
import static projects.medicationtracker.Utils.NotificationUtils.GROUP_KEY;

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

    @Override
    public void onReceive(Context context, Intent intent) {
        NativeDbHelper db = new NativeDbHelper(context);
        Bundle prefs = db.getSettings();

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

        new NotificationCompat.Builder(context, EXPORT_ALERT_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.app_name))                        .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.drawable.pill)
                .setGroup(GROUP_KEY)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle())
                .build()
                .notify();

        LocalDateTime exportStart = TimeFormatting.stringToLocalDateTime(
                prefs.getString(DBHelper.EXPORT_START)
        );

        DataExportUtils.scheduleExport(
                context, exportStart, prefs.getInt(DBHelper.EXPORT_FREQUENCY)
        );
    }
}
