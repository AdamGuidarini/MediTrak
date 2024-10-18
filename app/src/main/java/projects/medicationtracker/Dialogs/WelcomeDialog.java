package projects.medicationtracker.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.R;

public class WelcomeDialog extends DialogFragment {
    private DBHelper db;

    public WelcomeDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstances) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        AlertDialog dialog;
        CheckBox agreeBox;

        builder.setTitle(R.string.welcome);
        builder.setView(R.layout.dialog_welcome);

        builder.setPositiveButton(R.string.ok, (dialogInterface, i) ->
        {
            db.close();
            dismiss();
        });

        dialog = builder.create();

        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        agreeBox = dialog.findViewById(R.id.termsAgreementBox);

        agreeBox.setOnCheckedChangeListener((compoundButton, b) ->
        {
            db.seenPermissionRequest(DBHelper.AGREED_TO_TERMS);
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(b);
        });

        db = new DBHelper(dialog.getContext());

        return dialog;
    }
}
