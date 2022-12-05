package projects.medicationtracker.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.R;
import projects.medicationtracker.SimpleClasses.Medication;

public class PauseResumeDialog extends DialogFragment
{
    private final Medication medication;
    private DBHelper db;

    public PauseResumeDialog(Medication medication)
    {
        this.medication = medication;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstances)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        db = new DBHelper(getActivity());

        boolean isActive = db.isMedicationActive(medication);

        String title = isActive ?
                getString(R.string.pause_medication) : getString(R.string.resume_medication);
        String message = isActive ?
                getString(R.string.pause_message, medication.getMedName()) :
                getString(R.string.resume_message, medication.getMedName());

        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(R.string.yes, (dialog, which) ->
        {
            db.pauseResumeMedication(medication, !isActive);
            db.close();
            dismiss();
        });
        builder.setNegativeButton(R.string.no, (dialog, which) ->
        {
            db.close();
            dismiss();
        });

        return builder.create();
    }
}
