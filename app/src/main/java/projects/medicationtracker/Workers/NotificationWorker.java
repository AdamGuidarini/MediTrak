package projects.medicationtracker.Workers;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.SystemClock.sleep;
import static projects.medicationtracker.Utils.NotificationUtils.CHANNEL_ID;
import static projects.medicationtracker.Utils.NotificationUtils.DOSE_TIME;
import static projects.medicationtracker.Utils.NotificationUtils.GROUP_KEY;
import static projects.medicationtracker.Utils.NotificationUtils.MEDICATION_ID;
import static projects.medicationtracker.Utils.NotificationUtils.MESSAGE;
import static projects.medicationtracker.Utils.NotificationUtils.NOTIFICATION_ID;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Arrays;

import projects.medicationtracker.Helpers.NativeDbHelper;
import projects.medicationtracker.MainActivity;
import projects.medicationtracker.R;
import projects.medicationtracker.Receivers.EventReceiver;

public class NotificationWorker extends Worker {
    private final Context context;
    private NotificationManager notificationManager;
    public static final int SUMMARY_ID = Integer.MAX_VALUE;
    public static String MARK_AS_TAKEN_ACTION = "markAsTaken";
    public static String SNOOZE_ACTION = "snooze15";
    public static String TAKE_ALL_ACTION = "markAllAsTaken";
    public static String DISMISSED_ACTION = "dismissed";

    NotificationWorker(Context context, WorkerParameters params) {
        super(context, params);

        this.context = context;
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    @Override
    public Result doWork() {
        notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final String message = getInputData().getString(MESSAGE);
        final String doseTime = getInputData().getString(DOSE_TIME);
        final long notificationId = getInputData().getLong(
                NOTIFICATION_ID, (int) System.currentTimeMillis()
        );
        final long medId = getInputData().getLong(MEDICATION_ID, -1);
        final StatusBarNotification[] openNotes = notificationManager.getActiveNotifications();

        try {
            Notification notification = createNotification(
                    message, doseTime, notificationId, medId
            );

            if (Arrays.stream(openNotes).noneMatch(n -> n.getId() == SUMMARY_ID)) {
                Notification notificationSummary
                        = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setSmallIcon(R.drawable.pill)
                        .setStyle(new NotificationCompat.InboxStyle())
                        .setGroup(GROUP_KEY)
                        .setSilent(true)
                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build();

                notificationManager.notify(SUMMARY_ID, notificationSummary);

                sleep(500);
            }

            // Only fire notification if not other active notification has the same ID
            if (Arrays.stream(openNotes).noneMatch(n -> n.getId() == notificationId)) {
                NativeDbHelper nativeDb = new NativeDbHelper(context);
                String doseTimeDb = doseTime.replace("T", " ") + ":00";

                projects.medicationtracker.Models.Notification alert
                        = new projects.medicationtracker.Models.Notification(
                        -1, medId, notificationId, doseTimeDb
                );

                nativeDb.stashNotification(alert);
                notificationManager.notify((int) notificationId, notification);

                // Force wait thread to prevent Muting recently noisy 0 error
                sleep(5000);
            }
        } catch (Exception e) {
            Log.e("NotificationWorker:CreateNotification", e.getMessage());

            return Result.failure();
        }

        try {
            StatusBarNotification[] filteredNotifications = Arrays.stream(
                    notificationManager.getActiveNotifications()
            ).filter(
                    n -> n.getId() != SUMMARY_ID
            ).toArray(StatusBarNotification[]::new);

            if (filteredNotifications.length == 1) {
                return Result.success();
            }

            for (final StatusBarNotification n : filteredNotifications) {
                Notification note = n.getNotification();

                long thisMedId = note.extras.getLong(MEDICATION_ID);
                String time = note.extras.getString(DOSE_TIME);

                PendingIntent takeAllIntent = createTakeAllIntent(thisMedId, n.getId(), time);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, note);

                if (builder.mActions.stream().anyMatch(a -> a.getTitle() == context.getString(R.string.take_all))) {
                    continue;
                }

                builder.addAction(0, context.getString(R.string.take_all), takeAllIntent);
                builder.setSilent(true);

                notificationManager.notify(n.getId(), builder.build());
            }
        } catch (Exception e) {
            Log.e("NotificationWorker:AddTakeAll", e.getMessage());

            return Result.failure();
        }

        return Result.success();
    }

    /**
     * Creates a notification
     *
     * @param message Message to display in the notification.
     * @return A built notification.
     */
    private Notification createNotification(
            String message,
            String doseTime,
            long notificationId,
            long medId
    ) {
        Intent markTakenIntent = new Intent(this.getApplicationContext(), EventReceiver.class);
        Intent snoozeIntent = new Intent(this.getApplicationContext(), EventReceiver.class);
        Intent deletedIntent = new Intent(this.getApplicationContext(), EventReceiver.class);
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

        deletedIntent.setAction(DISMISSED_ACTION + embeddedMedId);
        deletedIntent.putExtra(MEDICATION_ID + embeddedMedId, medId);
        deletedIntent.putExtra(NOTIFICATION_ID + embeddedMedId, notificationId);
        deletedIntent.putExtra(DOSE_TIME + embeddedMedId, doseTime);

        PendingIntent markAsTakenPendingIntent =
                PendingIntent.getBroadcast(
                        this.getApplicationContext(),
                        0,
                        markTakenIntent,
                        SDK_INT >= Build.VERSION_CODES.S ?
                                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                                : PendingIntent.FLAG_UPDATE_CURRENT
                );

        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(
                        getApplicationContext(),
                        0,
                        snoozeIntent,
                        SDK_INT >= Build.VERSION_CODES.S ?
                                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                                : PendingIntent.FLAG_UPDATE_CURRENT
                );

        PendingIntent deleteIntent =
                PendingIntent.getBroadcast(
                        getApplicationContext(),
                        0,
                        deletedIntent,
                        SDK_INT >= Build.VERSION_CODES.S ?
                                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                                : PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(message)
                        .setSmallIcon(R.drawable.pill)
                        .setGroup(GROUP_KEY)
                        .setAutoCancel(false)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setStyle(new NotificationCompat.BigTextStyle())
                        .addAction(
                                0,
                                context.getString(R.string.mark_as_taken),
                                markAsTakenPendingIntent
                        )
                        .addAction(
                                0,
                                context.getString(R.string.snooze_message),
                                snoozePendingIntent
                        )
                        .setDeleteIntent(deleteIntent);

        StatusBarNotification[] open = notificationManager.getActiveNotifications();

        if (Arrays.stream(open).filter(n -> n.getId() != SUMMARY_ID).count() >= 1) {
            builder.addAction(
                    0,
                    context.getString(R.string.take_all),
                    createTakeAllIntent(medId, notificationId, doseTime)
            );
        }

        Intent resIntent =
                new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resIntent);

        PendingIntent resPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        SDK_INT >= Build.VERSION_CODES.S ?
                                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                                : PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resPendingIntent);

        return builder.build();
    }

    private PendingIntent createTakeAllIntent(long medId, long notificationId, String doseTime) {
        String embeddedMedId = "_" + medId;
        Intent takeAllIntent = new Intent(this.getApplicationContext(), EventReceiver.class);

        takeAllIntent.setAction(TAKE_ALL_ACTION + embeddedMedId);
        takeAllIntent.putExtra(MEDICATION_ID + embeddedMedId, medId);
        takeAllIntent.putExtra(NOTIFICATION_ID + embeddedMedId, notificationId);
        takeAllIntent.putExtra(DOSE_TIME + embeddedMedId, doseTime);

        return PendingIntent.getBroadcast(
                getApplicationContext(),
                0,
                takeAllIntent,
                SDK_INT >= Build.VERSION_CODES.S ?
                        PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                        : PendingIntent.FLAG_UPDATE_CURRENT
        );
    }
}
