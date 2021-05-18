package projects.medicationtracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class EditNoteFragment extends DialogFragment
{
    /**
     * Creates a DialogFragment allowing the user to change a note
     * @param savedInstanceState Saved instances
     * @return The Created Dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.fragment_edit_note, null));
        builder.setTitle("Edit Note");

        builder.setPositiveButton("OK", ((dialogInterface, i) ->
        {

        }));

        builder.setNegativeButton("Cancel", ((dialogInterface, i) -> dismiss()));

        return builder.create();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog)
    {
        super.onCancel(dialog);
    }
}
