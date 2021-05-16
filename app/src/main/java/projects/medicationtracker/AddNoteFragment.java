package projects.medicationtracker;

import android.accessibilityservice.GestureDescription;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.annotation.NonNull;


public class AddNoteFragment extends DialogFragment
{
    final Context context;
    final long medId;

    public AddNoteFragment(Context context, long medicationId)
    {
        this.context = context;
        medId = medicationId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.fragment_add_note, null))
                .setTitle("Add Note")
                .setPositiveButton("OK", (dialogInterface, i) ->
                {
                    DBHelper db = new DBHelper(context);
                    EditText editText = getDialog().findViewById(R.id.editNote);

                    if (editText != null)
                    {
                        String note = editText.getText().toString();

                        if (!note.isEmpty())
                        {
                            db.addNote(note, medId);
                            Note newNote = db.getNotes(medId).get(db.getNotes(medId).size() - 1);
                            CardCreator.createNoteCard(newNote, getActivity().findViewById(R.id.notesLayout));
                        }
                    }

                    dismiss();
                }).setNegativeButton("Cancel", (dialogInterface, i) ->
                {
                    dismiss();
                });
        return builder.create();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog)
    {
        super.onCancel(dialog);
    }
}