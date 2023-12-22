package projects.medicationtracker.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.MedicationNotes;
import projects.medicationtracker.R;
import projects.medicationtracker.SimpleClasses.Note;

public class AddNoteDialog extends DialogFragment {
    final DBHelper db;
    final Note note;
    private EditText alterNote;

    public AddNoteDialog(Note note, DBHelper db) {
        this.note = note;
        this.db = db;
    }

    /**
     * Creates a DialogFragment allowing the user to change a note
     *
     * @param savedInstanceState Saved instances
     * @return The Created Dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        AlertDialog noteDialog;
        builder.setView(inflater.inflate(R.layout.dialog_add_note, null));
        builder.setTitle(getString(R.string.edit_note));

        builder.setPositiveButton(getString(R.string.save), ((dialogInterface, i) ->
        {
            save();
            restartActivity();
        }));

        builder.setNegativeButton(getString(R.string.cancel), ((dialogInterface, i) -> dismiss()));

        if (note.getNoteId() != -1) {
            builder.setNeutralButton(getString(R.string.delete), ((dialogInterface, i) ->
            {
                db.deleteNote(note);
                restartActivity();
            }));
        }

        noteDialog = builder.create();
        noteDialog.show();

        noteDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        alterNote = noteDialog.findViewById(R.id.alterNote);

        if (note.getNoteId() != -1) {
            alterNote.setText(note.getNote());
        }

        alterNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                noteDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(
                        editable.toString().length() > 0
                );
            }
        });

        return noteDialog;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
    }

    private void save() {
        alterNote = getDialog().findViewById(R.id.alterNote);

        if (note.getNoteId() != -1) {
            db.updateNote(note, alterNote.getText().toString());
        } else {
            db.addNote(alterNote.getText().toString(), note.getMedId());
        }
    }

    public void restartActivity() {
        Intent intent = new Intent(getContext(), MedicationNotes.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("medId", note.getMedId());
        startActivity(intent);
    }
}
