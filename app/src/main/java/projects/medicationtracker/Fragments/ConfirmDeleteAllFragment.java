package projects.medicationtracker.Fragments;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Utils.NotificationUtils;
import projects.medicationtracker.R;
import projects.medicationtracker.Models.Medication;

public class ConfirmDeleteAllFragment extends DialogFragment {
    private final DBHelper db;
    private ArrayList<Medication> medications;
    private NotificationManager manager;

    public ConfirmDeleteAllFragment(DBHelper database) {
        db = database;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstances) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        manager = (NotificationManager) getActivity().getSystemService(
            Context.NOTIFICATION_SERVICE
        );

        builder.setTitle(getString(R.string.delete_all_data));

        builder.setMessage(getString(R.string.delete_all_data_cannot_be_undone));

        builder.setPositiveButton(getString(R.string.yes), ((DialogInterface, i) ->
        {
            medications = db.getMedications();
            deletePendingNotifications();

            db.purge();
            Toast.makeText(this.getContext(), getString(R.string.all_data_deleted), Toast.LENGTH_SHORT)
                    .show();
        }));

        builder.setNegativeButton(getString(R.string.no), ((DialogInterface, i) -> dismiss()));

        return builder.create();
    }

    private void deletePendingNotifications() {
        for (Medication medication : medications) {
            if (medication.getFrequency() == 1440) {
                NotificationUtils.deletePendingNotification(medication.getId(), getContext());
            } else {
                long[] timeIds = db.getMedicationTimeIds(medication);

                for (long timeId : timeIds) {
                    NotificationUtils.deletePendingNotification(timeId * -1, getContext());
                }
            }
        }

        if (manager.getActiveNotifications().length > 0) {
            manager.cancelAll();
        }
    }
}
