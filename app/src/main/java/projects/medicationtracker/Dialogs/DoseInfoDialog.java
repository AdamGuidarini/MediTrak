package projects.medicationtracker.Dialogs;

import static projects.medicationtracker.Helpers.DBHelper.DATE_FORMAT;
import static projects.medicationtracker.Helpers.DBHelper.TIME_FORMAT;
import static projects.medicationtracker.MainActivity.preferences;
import static projects.medicationtracker.MediTrak.DATABASE_PATH;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import kotlin.Triple;
import projects.medicationtracker.Fragments.SelectDateFragment;
import projects.medicationtracker.Fragments.TimePickerFragment;
import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NativeDbHelper;
import projects.medicationtracker.Helpers.TimeFormatting;
import projects.medicationtracker.Interfaces.IDialogCloseListener;
import projects.medicationtracker.R;
import projects.medicationtracker.Models.Dose;
import projects.medicationtracker.Models.Medication;

public class DoseInfoDialog extends DialogFragment {
    private final long doseId;
    private final DBHelper db;
    private final NativeDbHelper nativeDb;
    private Dose thisDose;
    private final TextView textView;
    private TextInputEditText timeTaken;
    private TextInputEditText dateTaken;
    private TextInputEditText dosageAmount;
    private TextInputEditText dosageUnit;
    private Medication medication;
    private boolean isDosageValid = true;
    private boolean isDosageUnitValid = true;

    public DoseInfoDialog(long doseId, DBHelper database, TextView tv) {
        this.doseId = doseId;
        db = database;
        textView = tv;
        nativeDb = new NativeDbHelper(DATABASE_PATH);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstances) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        medication = ((Triple<Medication, Long, LocalDateTime>) textView.getTag()).getFirst();
        AlertDialog infoDialog;

        builder.setView(inflater.inflate(R.layout.dialog_dose_info, null));
        builder.setTitle(R.string.this_dose);

        builder.setPositiveButton(getString(R.string.save), ((dialogInterface, i) -> save()));
        builder.setNegativeButton(R.string.close, ((dialogInterface, i) -> dismiss()));

        if (medication.getFrequency() == 0) {
            builder.setNeutralButton(R.string.delete, ((dialogInterface, i) -> deleteAsNeededDose()));
        }

        infoDialog = builder.create();
        infoDialog.show();

        infoDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        thisDose = doseId != -1 ? nativeDb.getDoseById(doseId) : new Dose();

        timeTaken = infoDialog.findViewById(R.id.dose_time_taken);
        dateTaken = infoDialog.findViewById(R.id.dose_date_taken);
        dosageAmount = infoDialog.findViewById(R.id.dosage_amount);
        dosageUnit = infoDialog.findViewById(R.id.dosage_unit);

        if (doseId != -1) {
            LocalDateTime doseDate = db.getTimeTaken(doseId);
            String date = DateTimeFormatter.ofPattern(
                    preferences.getString(DATE_FORMAT),
                    Locale.getDefault()
            ).format(doseDate);
            String time = DateTimeFormatter.ofPattern(
                    preferences.getString(TIME_FORMAT),
                    Locale.getDefault()
            ).format(doseDate);

            timeTaken.setShowSoftInputOnFocus(false);
            dateTaken.setShowSoftInputOnFocus(false);

            timeTaken.setText(time);
            timeTaken.setTag(doseDate.toLocalTime());

            dateTaken.setText(date);
            dateTaken.setTag(doseDate.toLocalDate());

            if (thisDose.getOverrideDoseAmount() == -1) {
                dosageAmount.setText(String.valueOf(medication.getDosage()));
            } else {
                dosageAmount.setText(String.valueOf(thisDose.getOverrideDoseAmount()));
            }

            if (thisDose.getOverrideDoseUnit().isEmpty()) {
                dosageUnit.setText(medication.getDosageUnits());
            } else {
                dosageUnit.setText(thisDose.getOverrideDoseUnit());
            }


            TextWatcher tw = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    infoDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(isDosageUnitValid && isDosageValid);
                }
            };

            timeTaken.addTextChangedListener(tw);
            dateTaken.addTextChangedListener(tw);

            dosageAmount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    TextInputLayout inputLayout = infoDialog.findViewById(R.id.dosage_layout);
                    inputLayout.setErrorEnabled(false);

                    if (editable.toString().isEmpty()) {
                        isDosageValid = false;
                        inputLayout.setError(getString(R.string.err_required));
                    } else {
                        try {
                            Integer.valueOf(editable.toString());
                            isDosageValid = true;
                        } catch (Exception e) {
                            inputLayout.setError(getString(R.string.val_too_big));
                            isDosageValid = false;
                        }
                    }

                    infoDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(isDosageUnitValid && isDosageValid);
                }
            });

            dosageUnit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    TextInputLayout inputLayout = infoDialog.findViewById(R.id.dosage_unit_layout);
                    inputLayout.setErrorEnabled(false);

                    if (editable.toString().isEmpty()) {
                        inputLayout.setError(getString(R.string.err_required));
                        isDosageUnitValid = false;
                    } else {
                        isDosageUnitValid = true;
                    }

                    infoDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(isDosageUnitValid && isDosageValid);
                }
            });
        }

        return infoDialog;
    }


    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!db.getTaken(doseId)) {
            getDialog().findViewById(R.id.notTakenMessage).setVisibility(View.VISIBLE);
        } else {
            timeTaken.setOnFocusChangeListener((view, b) ->
            {
                if (b) {
                    DialogFragment timePicker = new TimePickerFragment(timeTaken);
                    timePicker.show(getParentFragmentManager(), null);
                }
            });

            dateTaken.setOnFocusChangeListener((view, b) ->
            {
                if (b) {
                    DialogFragment datePicker = new SelectDateFragment(dateTaken);
                    datePicker.show(getParentFragmentManager(), null);
                }
            });

            getDialog().findViewById(R.id.dose_time_details).setVisibility(View.VISIBLE);
        }
    }

    private void save() {
        LocalDate date = (LocalDate) dateTaken.getTag();
        LocalTime time = (LocalTime) timeTaken.getTag();
        LocalDateTime dateTime = LocalDateTime.of(date, time).withSecond(0);
        int overrideAmount = Integer.parseInt(dosageAmount.getText().toString());
        String overrideUnits = dosageUnit.getText().toString();
        Fragment parent = getParentFragment();

        thisDose.setTimeTaken(dateTime);

        if (medication.getDosage() != overrideAmount) {
            thisDose.setOverrideDoseAmount(overrideAmount);
        }

        if (!medication.getDosageUnits().equals(overrideUnits)) {
            thisDose.setOverrideDoseUnit(overrideUnits);
        }

        nativeDb.updateDose(thisDose);

        if (parent instanceof IDialogCloseListener) {
            ((IDialogCloseListener) parent).handleDialogClose(IDialogCloseListener.Action.EDIT, thisDose);
        }

        dismiss();
    }

    private void deleteAsNeededDose() {
        db.deleteDose(doseId);

        Fragment fragment = getParentFragment();

        Dose dose = new Dose(doseId, -1, true, LocalDateTime.of((LocalDate) dateTaken.getTag(), (LocalTime) timeTaken.getTag()), null);

        if (fragment instanceof IDialogCloseListener) {
            ((IDialogCloseListener) fragment).handleDialogClose(
                    IDialogCloseListener.Action.DELETE, dose
            );
        }
    }

    private void setDosageValidity(boolean valid) {
        isDosageValid = valid;
    }

    private void setDosageUnitValidity(boolean valid) {
        isDosageUnitValid = valid;
    }
}
