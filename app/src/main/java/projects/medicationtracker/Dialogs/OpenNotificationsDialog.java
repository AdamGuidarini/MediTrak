package projects.medicationtracker.Dialogs;

import static projects.medicationtracker.Helpers.DBHelper.DATE_FORMAT;
import static projects.medicationtracker.Helpers.DBHelper.TIME_FORMAT;
import static projects.medicationtracker.MainActivity.preferences;
import static projects.medicationtracker.MediTrak.DATABASE_PATH;

import android.app.Dialog;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.view.LayoutInflater;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import projects.medicationtracker.Helpers.NativeDbHelper;
import projects.medicationtracker.Models.Medication;
import projects.medicationtracker.Models.Notification;
import projects.medicationtracker.R;

public class OpenNotificationsDialog extends DialogFragment {
    NativeDbHelper nativeDbHelper;
    StatusBarNotification[] openNotifications;
    ArrayList<Medication> meds;
    ArrayList<CheckBox> doseCheckBoxes;

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

        builder.setPositiveButton(R.string.mark_as_taken, ((dialog, which) -> dismiss()));
        builder.setNegativeButton(R.string.cancel, ((dialog, which) -> dismiss()));

        openNotificationsDialog = builder.create();
        openNotificationsDialog.show();

        generateCheckBoxes();

        return openNotificationsDialog;
    }

    private void generateCheckBoxes() {
        String timeFormat = preferences.getString(TIME_FORMAT);
        String dateFormat = preferences.getString(DATE_FORMAT);

        for (final StatusBarNotification notification : openNotifications) {
            CheckBox box = new CheckBox(getActivity());

            box.setTag(notification);


        }
    }
}
