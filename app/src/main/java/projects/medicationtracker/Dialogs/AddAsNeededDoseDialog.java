package projects.medicationtracker.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

import projects.medicationtracker.Fragments.TimePickerFragment;
import projects.medicationtracker.Helpers.NativeDbHelper;
import projects.medicationtracker.Utils.NotificationUtils;
import projects.medicationtracker.Interfaces.IDialogCloseListener;
import projects.medicationtracker.R;
import projects.medicationtracker.Models.Dose;
import projects.medicationtracker.Models.Medication;

public class AddAsNeededDoseDialog extends DialogFragment {
    private final ArrayList<Medication> medications;
    private final NativeDbHelper db;
    private final LocalDate date;
    private TextInputEditText timeTaken;
    private AutoCompleteTextView yourMeds;

    /**
     * Constructor
     *
     * @param medications Medications to display in dropdown. Non-as needed meds should be filtered out.
     * @param dateTaken   Date medication is take.
     * @param database    Database helper to store dose
     */
    public AddAsNeededDoseDialog(ArrayList<Medication> medications, LocalDate dateTaken, NativeDbHelper database) {
        db = database;
        this.medications = medications;
        date = dateTaken;
    }

    /**
     * Builds dialog upon creation
     *
     * @param savedInstances The last saved instance state of the Fragment,
     *                       or null if this is a freshly created Fragment.
     * @return Built dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstances) {
        AlertDialog dialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_add_as_needed, null));
        builder.setTitle(R.string.add_dose);

        builder.setPositiveButton(R.string.save, ((dialogInterface, i) -> {
            save();
            dismiss();
        }));
        builder.setNegativeButton(R.string.close, ((dialogInterface, i) -> dismiss()));

        dialog = builder.create();
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        yourMeds = dialog.findViewById(R.id.medicationNames);
        timeTaken = dialog.findViewById(R.id.doseTimeTaken);

        yourMeds.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!timeTaken.getText().toString().isEmpty())
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            }
        });

        timeTaken.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!yourMeds.getText().toString().isEmpty())
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            }
        });

        return dialog;
    }

    /**
     * Prepares Views and creates listeners after dialog is built
     */
    @Override
    public void onStart() {
        super.onStart();
        ArrayList<String> medNames = new ArrayList<>();
        ArrayAdapter<String> medNamesAdapter;

        medications.forEach(m -> medNames.add(m.getName()));

        medNamesAdapter = new ArrayAdapter<>(
                getDialog().getContext(), android.R.layout.simple_dropdown_item_1line, medNames
        );

        yourMeds.setAdapter(medNamesAdapter);

        timeTaken.setShowSoftInputOnFocus(false);

        timeTaken.setOnFocusChangeListener((view, b) ->
        {
            if (b) {
                DialogFragment dialogFragment = new TimePickerFragment(timeTaken);
                dialogFragment.show(getParentFragmentManager(), null);
            }
        });
    }

    /**
     * Creates new as needed dose
     */
    private void save() {
        String selectedMedName = yourMeds.getText().toString();
        Medication med = medications.stream().filter(
                m -> Objects.equals(m.getName(), selectedMedName)
        ).collect(Collectors.toCollection(ArrayList::new)).get(0);
        LocalTime time = (LocalTime) timeTaken.getTag();
        LocalDateTime dateTimeTaken = LocalDateTime.of(date, time);
        Fragment fragment = getParentFragment();
        long doseId = db.addDose(med.getId(), dateTimeTaken, dateTimeTaken, true);

        Medication medication = db.getMedicationById(med.getId());

        if (medication.getNotifyWhenRemaining() != -1 && medication.getNotifyWhenRemaining() >= medication.getRemainingDosesCount()) {
            NotificationUtils.notifyLowQuantity(medication, getContext());
        }

        Dose dose = new Dose(doseId, med.getId(), true, dateTimeTaken, null);

        if (fragment instanceof IDialogCloseListener) {
            ((IDialogCloseListener) fragment).handleDialogClose(
                    IDialogCloseListener.Action.ADD, dose
            );
        }
    }
}
