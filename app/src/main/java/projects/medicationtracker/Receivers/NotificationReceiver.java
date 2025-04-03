package projects.medicationtracker.Receivers;

import static projects.medicationtracker.Utils.NotificationUtils.DOSE_TIME;
import static projects.medicationtracker.Utils.NotificationUtils.MEDICATION_ID;
import static projects.medicationtracker.Utils.NotificationUtils.MESSAGE;
import static projects.medicationtracker.Utils.NotificationUtils.NOTIFICATION_ID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.time.LocalDateTime;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NativeDbHelper;
import projects.medicationtracker.Utils.NotificationUtils;
import projects.medicationtracker.Models.Dose;
import projects.medicationtracker.Models.Medication;
import projects.medicationtracker.Workers.NotificationWorker;

public class NotificationReceiver extends BroadcastReceiver {
    /**
     * Receiver for notification PendingIntent
     *
     * @param context Context of caller.
     * @param intent  Intent to receive.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        DBHelper db = new DBHelper(context);
        NativeDbHelper nativeDbHelper = new NativeDbHelper(context);
        long medicationId = extras.getLong(MEDICATION_ID, -1);
        Medication medication = db.getMedication(medicationId);
        LocalDateTime doseTime = (LocalDateTime) extras.get(DOSE_TIME);
        Dose dose;

        long notificationId = extras.getLong(NOTIFICATION_ID, System.currentTimeMillis());

        if (!medication.isActive()) {
            db.close();
            nativeDbHelper.deleteNotification(notificationId);

            return;
        }

        // Set new Intent for a new notification
        NotificationUtils.scheduleNotificationInFuture(
                context,
                medication,
                doseTime,
                notificationId
        );

        try {
            dose = nativeDbHelper.findDose(medicationId, doseTime);
        } catch (Exception e) {
            Log.e("MediTrak", "An error occurred while retrieving a Dose for notifications");

            dose = new Dose();
        }

        // Fire notification if enabled or fire and let the OS block it in Android 13+
        if ((db.getNotificationEnabled() || Build.VERSION.SDK_INT >= 33) && !dose.isTaken()) {
            Data workerData = new Data.Builder()
                    .putLong(
                            NOTIFICATION_ID,
                            extras.getLong(NOTIFICATION_ID, System.currentTimeMillis())
                    )
                    .putString(MESSAGE, extras.getString(MESSAGE))
                    .putString(DOSE_TIME, doseTime.toString())
                    .putLong(MEDICATION_ID, medicationId)
                    .build();
            OneTimeWorkRequest workRequest =
                    new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .setInputData(workerData)
                    .build();

            WorkManager.getInstance(context).enqueue(workRequest);
        }

        db.close();
    }
}
