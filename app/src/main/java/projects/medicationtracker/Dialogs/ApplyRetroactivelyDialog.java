package projects.medicationtracker.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.R;
import projects.medicationtracker.SimpleClasses.Medication;

public class ApplyRetroactivelyDialog extends DialogFragment {

    private final Medication medication;
    private final DBHelper db;

    public ApplyRetroactivelyDialog(Medication medication, DBHelper database) {
        this.medication = medication;
        db = database;
    }

    /**
     * Builds dialog upon creation
     *
     * @param savedInstances The last saved instance state of the Fragment,
     *                       or null if this is a freshly created Fragment.
     * @return Built dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstances) {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setPositiveButton(R.string.yes, ((dialogInterface, i) -> dismiss()));

        builder.setNegativeButton(R.string.no, ((dialogInterface, i) -> {
            

            dismiss();
            requireActivity().finish();
        }));

        dialog = builder.create();

        return dialog;
    }


}
