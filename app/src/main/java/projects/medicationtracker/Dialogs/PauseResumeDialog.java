package projects.medicationtracker.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Utils.NotificationUtils;
import projects.medicationtracker.R;
import projects.medicationtracker.Models.Medication;

public class PauseResumeDialog extends DialogFragment {
    private final Medication medication;
    private DBHelper db;
    private final MenuItem pauseButton;
    private final MenuItem resumeButton;

    public PauseResumeDialog(Medication medication, MenuItem pause_button, MenuItem resume_button) {
        this.medication = medication;
        pauseButton = pause_button;
        resumeButton = resume_button;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstances) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());

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

            if (isActive) {
                resumeButton.setVisible(true);
                pauseButton.setVisible(false);

                NotificationUtils.clearPendingNotifications(medication, getActivity());
            } else {
                resumeButton.setVisible(false);
                pauseButton.setVisible(true);

                NotificationUtils.createNotifications(medication, getActivity());
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
