package projects.medicationtracker.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NotificationHelper;
import projects.medicationtracker.R;
import projects.medicationtracker.SimpleClasses.Medication;

public class PauseResumeDialog extends DialogFragment
{
    private final Medication medication;
    private DBHelper db;
    private final MenuItem pauseButton;
    private final MenuItem resumeButton;

    public PauseResumeDialog(Medication medication, MenuItem pause_button, MenuItem resume_button)
    {
        this.medication = medication;
        pauseButton = pause_button;
        resumeButton = resume_button;
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
                getString(R.string.pause_message, medication.getName()) :
                getString(R.string.resume_message, medication.getName());

        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(R.string.yes, (dialog, which) ->
        {
            db.pauseResumeMedication(medication, !isActive);
            db.close();

            if (isActive)
            {
                resumeButton.setVisible(true);
                pauseButton.setVisible(false);

                NotificationHelper.clearPendingNotifications(medication, getActivity());
            }
            else
            {
                resumeButton.setVisible(false);
                pauseButton.setVisible(true);

                NotificationHelper.createNotifications(medication, getActivity());
            }

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
