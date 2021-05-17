package projects.medicationtracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.annotation.NonNull;

import java.util.ArrayList;


public class AddNoteFragment extends DialogFragment
{
    final Context context;
    final long medId;

    /**
     * Constructor for fragment
     * @param context Context for DBHelper
     * @param medicationId ID of Medication not is about
     **************************************************************************/
    public AddNoteFragment(Context context, long medicationId)
    {
        this.context = context;
        medId = medicationId;
    }

    /**
     * Creates DialogFragment that allows a user to enter notes
     * @param savedInstanceState Saved instance state
     * @return The Dialog allowing user to take notes
     **************************************************************************/
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
                            ArrayList<Note> notes = db.getNotes(medId);
                            int size = notes.size();
                            Note newNote = notes.get(size - 1);

                            if (size == 1)
                            {
                                TextView tv = getActivity().findViewById(R.id.noNotes);
                                tv.setVisibility(View.GONE);
                                ScrollView scrollNotes = getActivity().findViewById(R.id.scrollNotes);
                                scrollNotes.setVisibility(View.VISIBLE);
                            }

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

    /**
     * Cancels Dialog
     * @param dialog The Dialog to cancel
     **************************************************************************/
    @Override
    public void onCancel(@NonNull DialogInterface dialog)
    {
        super.onCancel(dialog);
    }
}