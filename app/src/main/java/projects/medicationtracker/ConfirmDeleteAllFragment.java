package projects.medicationtracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

public class ConfirmDeleteAllFragment extends DialogFragment
{
    private final DBHelper db;

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
            db.purge();
            Toast.makeText(this.getContext(), "All data has been deleted", Toast.LENGTH_SHORT)
                    .show();
        }));

        builder.setNegativeButton("No", ((DialogInterface, i) -> dismiss()));

        return builder.create();
    }
}
