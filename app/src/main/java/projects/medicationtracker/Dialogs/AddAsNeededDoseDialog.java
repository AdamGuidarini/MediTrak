package projects.medicationtracker.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.AutoCompleteTextView;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.util.ArrayList;

import projects.medicationtracker.Fragments.TimePickerFragment;
import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.R;
import projects.medicationtracker.SimpleClasses.Medication;

public class AddAsNeededDoseDialog extends DialogFragment
{
    private final ArrayList<Medication> medications;
    private final DBHelper db;
    private AutoCompleteTextView medicationNames;
    private TextInputEditText timeTaken;
    private LocalDate date;

    public AddAsNeededDoseDialog(ArrayList<Medication> medications, LocalDate dateTaken, DBHelper database)
    {
        db = database;
        this.medications = medications;
        date = dateTaken;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstances)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_add_as_needed, null));
        builder.setTitle(R.string.add_dose);

        builder.setPositiveButton(R.string.save, ((dialogInterface, i) -> save()));
        builder.setNegativeButton(R.string.close, ((dialogInterface, i) -> dismiss()));

        return builder.create();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        medicationNames = getDialog().findViewById(R.id.medicationNames);
        timeTaken = getDialog().findViewById(R.id.doseTimeTaken);

        timeTaken.setShowSoftInputOnFocus(false);

        timeTaken.setOnFocusChangeListener((view, b) ->
        {
            if (b)
            {
                DialogFragment dialogFragment = new TimePickerFragment(timeTaken);
                dialogFragment.show(getParentFragmentManager(), null);
            }
        });
    }

    private void save()
    {

    }
}
