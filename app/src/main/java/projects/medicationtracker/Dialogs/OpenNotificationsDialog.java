package projects.medicationtracker.Dialogs;

import static projects.medicationtracker.Helpers.DBHelper.DATE_FORMAT;
import static projects.medicationtracker.Helpers.DBHelper.TIME_FORMAT;
import static projects.medicationtracker.MainActivity.preferences;
import static projects.medicationtracker.MediTrak.DATABASE_PATH;
import static projects.medicationtracker.Workers.NotificationWorker.SUMMARY_ID;

import android.app.Dialog;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NativeDbHelper;
import projects.medicationtracker.Models.Medication;
import projects.medicationtracker.Models.Notification;
import projects.medicationtracker.R;

public class OpenNotificationsDialog extends DialogFragment {
    private NativeDbHelper nativeDbHelper;
    private StatusBarNotification[] openNotifications;
    private ArrayList<Medication> meds;
    private ArrayList<CheckBox> doseCheckBoxes = new ArrayList<>();
    private LinearLayout checkBoxHolder;
    private CheckBox checkAll;
    private SwitchCompat dismissUnselected;
    private NotificationManager manager;

    public OpenNotificationsDialog(StatusBarNotification[] notifications, ArrayList<Medication> medications) {
        openNotifications = notifications;
        meds = medications;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        AlertDialog openNotificationsDialog;

        nativeDbHelper = new NativeDbHelper(DATABASE_PATH);

        builder.setView(inflater.inflate(R.layout.dialog_open_notifications, null));
        builder.setTitle(R.string.open_notifications);

        builder.setPositiveButton(R.string.mark_as_taken, ((dialog, which) -> onTake()));
        builder.setNegativeButton(R.string.cancel, ((dialog, which) -> dismiss()));

        openNotificationsDialog = builder.create();
        openNotificationsDialog.show();

        checkBoxHolder = openNotificationsDialog.findViewById(R.id.checkboxes);
        checkAll = openNotificationsDialog.findViewById(R.id.check_all);
        dismissUnselected = openNotificationsDialog.findViewById(R.id.dismiss_unselected);

        manager = (NotificationManager) getActivity().getSystemService(
            Context.NOTIFICATION_SERVICE
        );

        checkAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (final CheckBox cb : doseCheckBoxes) {
                cb.setChecked(isChecked);
            }
        });

        generateCheckBoxes();

        return openNotificationsDialog;
    }

    /**
     * Set checkboxes and
     */
    private void generateCheckBoxes() {
        ArrayList<Notification> notes = nativeDbHelper.getNotifications();

        for (final StatusBarNotification notification : openNotifications) {

            if (notification.getId() == SUMMARY_ID) {
                continue;
            }

            CheckBox box = new CheckBox(getActivity());
            Medication m;
            String label;
            Notification thisNotification = notes.stream().filter(
                _n -> _n.getId() == notification.getId()
            ).findFirst().orElse(null);

            if (thisNotification == null) {
                Log.e(
                    "Notifications Dialog",
                    "Cannot find notification with ID: " + notification.getId()
                );

                continue;
            }

            m = meds.stream().filter(
                _m -> _m.getId() == thisNotification.getMedId()
            ).findFirst().orElse(null);

            if (m == null) {
                Log.e(
                    "Notifications Dialog",
                    "Cannot find notification for med: " + thisNotification.getMedId()
                );

                continue;
            }

            label = m.getName() + " - " + m.getDosage() + " " + m.getDosageUnits() + " - ";

            label += DateTimeFormatter.ofPattern(
                preferences.getString(DATE_FORMAT),
                Locale.getDefault()
            ).format(thisNotification.getDoseTime().toLocalDate());

            label += " " + getString(R.string.at) + " " + DateTimeFormatter.ofPattern(
                preferences.getString(TIME_FORMAT),
                Locale.getDefault()
            ).format(thisNotification.getDoseTime().toLocalTime());

            box.setText(label);
            box.setTag(thisNotification);

            box.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    final long selectedLength = doseCheckBoxes.stream().filter(
                        cb -> cb.isChecked()
                    ).count();

                    checkAll.setChecked(selectedLength == doseCheckBoxes.size());
                }
            );

            doseCheckBoxes.add(box);
        }

        doseCheckBoxes.sort((a, b) -> {
            LocalDateTime timeA = ((Notification) a.getTag()).getDoseTime();
            LocalDateTime timeB = ((Notification) b.getTag()).getDoseTime();

            if (timeA.isEqual(timeB)) {
                return 0;
            }

            return timeA.isBefore(timeB) ? 1: -1;
        });

        for (final CheckBox cb : doseCheckBoxes) {
            checkBoxHolder.addView(cb);
        }
    }

    private void onTake() {
        DBHelper db = new DBHelper(getActivity());

        for (final CheckBox box : doseCheckBoxes) {
            Notification notification = (Notification) box.getTag();
            Medication med = meds.stream().filter(
                _m -> _m.getId() == notification.getMedId()
            ).findFirst().orElse(null);

            if (box.isChecked()) {
                manager.cancel((int) notification.getNotificationId());

                db.addToMedicationTracker(med, notification.getDoseTime());

                nativeDbHelper.deleteNotification(notification.getNotificationId());
            } else if (dismissUnselected.isChecked()) {
                manager.cancel((int) notification.getNotificationId());
                nativeDbHelper.deleteNotification(notification.getNotificationId());
            }
        }

        if (manager.getActiveNotifications().length == 1 && manager.getActiveNotifications()[0].getId() == SUMMARY_ID) {
            manager.cancelAll();
        }
    }
}
