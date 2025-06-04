package projects.medicationtracker.Receivers;

import static projects.medicationtracker.Helpers.DBHelper.EXPORT_FREQUENCY;
import static projects.medicationtracker.Helpers.DBHelper.EXPORT_START;
import static projects.medicationtracker.Utils.NotificationUtils.DOSE_TIME;
import static projects.medicationtracker.Utils.NotificationUtils.MEDICATION_ID;
import static projects.medicationtracker.Utils.NotificationUtils.MED_REMINDER_CHANNEL_ID;
import static projects.medicationtracker.Utils.NotificationUtils.NOTIFICATION_ID;
import static projects.medicationtracker.Utils.NotificationUtils.clearPendingNotifications;
import static projects.medicationtracker.Utils.NotificationUtils.createNotifications;
import static projects.medicationtracker.Utils.NotificationUtils.scheduleIn15Minutes;
import static projects.medicationtracker.Workers.NotificationWorker.DISMISSED_ACTION;
import static projects.medicationtracker.Workers.NotificationWorker.SNOOZE_ACTION;
import static projects.medicationtracker.Workers.NotificationWorker.SUMMARY_ID;
import static projects.medicationtracker.Workers.NotificationWorker.TAKE_ALL_ACTION;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NativeDbHelper;
import projects.medicationtracker.Utils.DataExportUtils;
import projects.medicationtracker.Utils.NotificationUtils;
import projects.medicationtracker.Utils.TimeFormatting;
import projects.medicationtracker.Models.Medication;
import projects.medicationtracker.Models.Notification;
import projects.medicationtracker.Workers.NotificationWorker;

public class EventReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final DBHelper db = new DBHelper(context);
        final NativeDbHelper nativeDbHelper = new NativeDbHelper(context);
        final NotificationManager manager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        ArrayList<Medication> medications = db.getMedications();

        String action = Objects.requireNonNull(intent.getAction());

        if (action.contains(NotificationWorker.MARK_AS_TAKEN_ACTION)) {
            String embeddedId = "_" + intent.getAction().split("_")[1];

            markDoseTaken(
                    context,
                    intent.getLongExtra(NOTIFICATION_ID + embeddedId, 0),
                    intent.getLongExtra(MEDICATION_ID + embeddedId, 0),
                    intent.getStringExtra(DOSE_TIME + embeddedId),
                    db
            );

            nativeDbHelper.deleteNotification(intent.getLongExtra(NOTIFICATION_ID + embeddedId, 0));
        } else if (intent.getAction().contains(SNOOZE_ACTION)) {
            String embeddedId = "_" + intent.getAction().split("_")[1];

            snoozeFor15(
                    context,
                    intent.getLongExtra(NOTIFICATION_ID + embeddedId, 0),
                    intent.getLongExtra(MEDICATION_ID + embeddedId, 0),
                    intent.getStringExtra(DOSE_TIME + embeddedId),
                    db
            );
        } else if (intent.getAction().contains(DISMISSED_ACTION)) {
            String embeddedId = "_" + intent.getAction().split("_")[1];

            nativeDbHelper.deleteNotification(intent.getLongExtra(MEDICATION_ID + embeddedId, 0));
        } else if (intent.getAction().contains(TAKE_ALL_ACTION)) {
            takeAll(manager, nativeDbHelper);
        } else {
            final ArrayList<Notification> notifications = nativeDbHelper.getNotifications();

            prepareExport(context, nativeDbHelper.getSettings());
            medications.forEach(m -> prepareNotification(context, m));

            for (final Notification n : notifications) {
                Medication med = medications.stream().filter(
                        m -> m.getId() == n.getMedId()
                ).findFirst().orElse(null);

                if (med == null) {
                    Log.e(
                            "EventReceiver",
                            "Failed to create notification for Medication: " + n.getMedId()
                    );

                    continue;
                }

                NotificationUtils.scheduleNotification(
                        context, med, n.getDoseTime(), n.getNotificationId()
                );
            }
        }

        StatusBarNotification[] notifications = Arrays.stream(manager.getActiveNotifications())
                .filter(n -> n.getNotification().getChannelId().equals(MED_REMINDER_CHANNEL_ID))
                .toArray(StatusBarNotification[]::new);

        if (notifications.length == 1 && notifications[0].getId() == SUMMARY_ID) {
            manager.cancel(SUMMARY_ID);
        }

        db.close();
    }

    /**
     * Prepares pending notifications on boot
     *
     * @param context    Notification context
     * @param medication Medication in need of a notification
     */
    private void prepareNotification(Context context, Medication medication) {
        clearPendingNotifications(medication, context);
        createNotifications(medication, context);
    }

    private void prepareExport(Context context, Bundle preferences) {
        if (preferences.getString(EXPORT_START).isEmpty()) {
            return;
        }

        LocalDateTime exportStart = TimeFormatting.stringToLocalDateTime(
                Objects.requireNonNull(preferences.getString(EXPORT_START))
        );
        int frequency = preferences.getInt(EXPORT_FREQUENCY);

        DataExportUtils.scheduleExport(context, exportStart, frequency);
    }

    /**
     * Marks a dose as taken from the notification
     *
     * @param context        Application context
     * @param notificationId Id of notification to cancel
     * @param medId          ID of medication taken
     * @param doseTimeString Dose time for DB.
     */
    private void markDoseTaken(
            Context context, long notificationId, long medId, String doseTimeString, DBHelper db
    ) {
        Medication med;
        LocalDateTime doseTime = LocalDateTime.parse(doseTimeString);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        med = db.getMedication(medId);

        long doseId = db.isInMedicationTracker(med, doseTime) ?
                db.getDoseId(med.getId(), TimeFormatting.localDateTimeToDbString(doseTime)) :
                db.addToMedicationTracker(med, doseTime);

        db.updateDoseStatus(
                doseId,
                TimeFormatting.localDateTimeToDbString(LocalDateTime.now().withSecond(0)),
                true
        );

        notificationManager.cancel((int) notificationId);
    }

    private void snoozeFor15(
            Context context, long notificationId, long medId, String doseTimeString, DBHelper db
    ) {
        Medication med;
        LocalDateTime doseTime = LocalDateTime.parse(doseTimeString);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        med = db.getMedication(medId);

        scheduleIn15Minutes(
                context,
                med,
                doseTime,
                notificationId
        );

        notificationManager.cancel((int) notificationId);
    }

    private void takeAll(NotificationManager manager, NativeDbHelper nativeDbHelper) {
        StatusBarNotification[] activeNotifications
                = Arrays.stream(manager.getActiveNotifications()).filter(
                n -> n.getId() != SUMMARY_ID
        ).toArray(StatusBarNotification[]::new);
        ArrayList<Notification> notifications = nativeDbHelper.getNotifications();

        for (final StatusBarNotification n : activeNotifications) {
            Notification thisNotification = notifications.stream().filter(
                    _n -> _n.getNotificationId() == n.getId()
            ).findFirst().orElse(null);

            if (thisNotification == null) {
                Log.e(
                        "EventReceiver",
                        "Failed to find notification with ID: "
                                + n.getId() + ". Continuing to next notification."
                );

                continue;
            }

            nativeDbHelper.addDose(
                    thisNotification.getMedId(),
                    thisNotification.getDoseTime(),
                    LocalDateTime.now().withSecond(0).withNano(0),
                    true
            );

            nativeDbHelper.deleteNotification(thisNotification.getId());
        }

        manager.cancelAll();
    }
}
