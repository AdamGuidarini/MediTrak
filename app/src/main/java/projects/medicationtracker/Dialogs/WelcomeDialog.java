package projects.medicationtracker.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.R;

public class WelcomeDialog extends DialogFragment
{
    private DBHelper db;

    public WelcomeDialog(DBHelper database)
    {
        db = database;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstances)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.welcome);
        builder.setView(R.layout.dialog_welcome);

        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> dismiss());

        return builder.create();
    }
}
