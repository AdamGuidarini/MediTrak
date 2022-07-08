package projects.medicationtracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class ConfirmDeleteAllFragment extends DialogFragment
{
    private final DBHelper db;
    private ArrayList<Medication> medications;

    ConfirmDeleteAllFragment(DBHelper database)
    {
        db = database;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstances)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Delete All Saved Data");

        builder.setMessage("Delete all saved data? This action cannot be undone.");

        builder.setPositiveButton("Yes", ((DialogInterface, i) ->
        {
            medications = db.getMedications();
            deletePendingNotifications();

            db.purge();
            Toast.makeText(this.getContext(), "All data has been deleted", Toast.LENGTH_SHORT)
                    .show();
        }));

        builder.setNegativeButton("No", ((DialogInterface, i) -> dismiss()));

        return builder.create();
    }

    private void deletePendingNotifications()
    {
        for (Medication medication : medications)
        {
            if (medication.getMedFrequency() == 1440)
            {
                NotificationHelper.deletePendingNotification(medication.getMedId(), getContext());
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
