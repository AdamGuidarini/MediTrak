package projects.medicationtracker.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import projects.medicationtracker.Interfaces.IDialogCloseListener;
import projects.medicationtracker.MedicationNotes;
import projects.medicationtracker.R;
import projects.medicationtracker.Models.Note;

public class AddNoteDialog extends DialogFragment {
    final Note note;
    private EditText noteInput;

    public AddNoteDialog(Note note) {
        this.note = note;
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
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        AlertDialog noteDialog;
        int titleResId = note.getNoteId() == -1 ? R.string.add_note : R.string.edit_note;

        builder.setView(inflater.inflate(R.layout.dialog_add_note, null));
        builder.setTitle(getString(titleResId));

        builder.setPositiveButton(getString(R.string.save), ((dialogInterface, i) ->
        {
            save();
            dismiss();
        }));

        builder.setNegativeButton(getString(R.string.cancel), ((dialogInterface, i) -> dismiss()));

        if (note.getNoteId() != -1) {
            builder.setNeutralButton(getString(R.string.delete), ((dialogInterface, i) ->
            {
                Activity parent = getActivity();

                if (parent instanceof IDialogCloseListener) {
                    ((IDialogCloseListener) parent).handleDialogClose(IDialogCloseListener.Action.DELETE, note);
                }
                dismiss();
            }));
        }

        noteDialog = builder.create();
        noteDialog.show();

        noteDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        noteInput = noteDialog.findViewById(R.id.noteInput);

        if (note.getNoteId() != -1) {
            noteInput.setText(note.getNote());
        }

        noteInput.addTextChangedListener(new TextWatcher() {
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
        Activity parent = getActivity();
        IDialogCloseListener.Action action = note.getNoteId() == -1 ?
                IDialogCloseListener.Action.ADD : IDialogCloseListener.Action.EDIT;

        note.setNote(noteInput.getText().toString());

        if (parent instanceof IDialogCloseListener) {
            ((IDialogCloseListener) parent).handleDialogClose(action, note);
        }
    }

    public void restartActivity() {
        Intent intent = new Intent(getContext(), MedicationNotes.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("medId", note.getMedId());
        startActivity(intent);
    }
}
