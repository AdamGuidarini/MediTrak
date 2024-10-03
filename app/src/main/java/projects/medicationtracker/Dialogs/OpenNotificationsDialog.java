package projects.medicationtracker.Dialogs;

import android.app.Dialog;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import projects.medicationtracker.Models.Medication;
import projects.medicationtracker.R;

public class OpenNotificationsDialog extends DialogFragment {
    StatusBarNotification[] openNotifications;
    ArrayList<Medication> meds;

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

        builder.setView(inflater.inflate(R.layout.dialog_open_notifications, null));
        builder.setTitle(R.string.open_notifications);

        builder.setPositiveButton(R.string.mark_as_taken, ((dialog, which) -> dismiss()));
        builder.setNegativeButton(R.string.cancel, ((dialog, which) -> dismiss()));

        openNotificationsDialog = builder.create();
        openNotificationsDialog.show();

        return openNotificationsDialog;
    }
}
