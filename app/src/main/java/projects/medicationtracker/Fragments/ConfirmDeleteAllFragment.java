package projects.medicationtracker.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NotificationHelper;
import projects.medicationtracker.R;
import projects.medicationtracker.SimpleClasses.Medication;

public class ConfirmDeleteAllFragment extends DialogFragment
{
    private final DBHelper db;
    private ArrayList<Medication> medications;

    public ConfirmDeleteAllFragment(DBHelper database)
    {
        db = database;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstances)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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

    private void deletePendingNotifications()
    {
        for (Medication medication : medications)
        {
            if (medication.getFrequency() == 1440)
            {
                NotificationHelper.deletePendingNotification(medication.getId(), getContext());
            }
            else
            {
                long[] timeIds = db.getMedicationTimeIds(medication);

                for (long timeId : timeIds)
                {
                    NotificationHelper.deletePendingNotification(timeId * -1, getContext());
                }
            }
        }
    }
}
