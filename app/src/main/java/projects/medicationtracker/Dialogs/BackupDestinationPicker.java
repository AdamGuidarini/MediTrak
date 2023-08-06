package projects.medicationtracker.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.fragment.app.DialogFragment;

import projects.medicationtracker.R;

public class BackupDestinationPicker extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstances) {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setPositiveButton(R.string.export, ((dialogInterface, i) -> {
            onExportClick();

            dismiss();
        }));
        builder.setNegativeButton(R.string.cancel, ((dialogInterface, i) -> dismiss()));

        dialog = builder.create();
        dialog.show();

        return dialog;
    }

    private void onExportClick() {

    }

    public native void DbManager(String databaseName, String exportDirectory);
}
