package projects.medicationtracker.Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Utils.NotificationUtils;
import projects.medicationtracker.MyMedications;
import projects.medicationtracker.Models.Medication;

public class ConfirmMedicationDeleteFragment extends DialogFragment {
    final DBHelper db;
    final Medication medication;

    /**
     * Creates a dialog fragment offering to delete the provided medication.
     *
     * @param database           The DBHelper connected to the database with the medication.
     * @param medicationToDelete The medication to delete from the database.
     */
    public ConfirmMedicationDeleteFragment(DBHelper database, Medication medicationToDelete) {
        db = database;
        medication = medicationToDelete;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());

        builder.setTitle("Are you sure you want to delete this medication?");

        builder.setPositiveButton("Yes", ((dialogInterface, i) ->
        {
            Intent intent = new Intent(getContext(), MyMedications.class);
            db.deleteMedication(medication);
            NotificationUtils.deletePendingNotification(medication.getId(), getContext());
            getActivity().finish();
            startActivity(intent);
        }));

        builder.setNegativeButton("Cancel", (((dialogInterface, i) -> dismiss())));

        return builder.create();
    }
}
