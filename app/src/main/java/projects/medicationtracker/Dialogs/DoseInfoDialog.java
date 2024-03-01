package projects.medicationtracker.Dialogs;

import static projects.medicationtracker.Helpers.DBHelper.DATE_FORMAT;
import static projects.medicationtracker.Helpers.DBHelper.TIME_FORMAT;
import static projects.medicationtracker.MainActivity.preferences;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import kotlin.Triple;
import projects.medicationtracker.Fragments.SelectDateFragment;
import projects.medicationtracker.Fragments.TimePickerFragment;
import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.TimeFormatting;
import projects.medicationtracker.Interfaces.IDialogCloseListener;
import projects.medicationtracker.R;
import projects.medicationtracker.SimpleClasses.Dose;
import projects.medicationtracker.SimpleClasses.Medication;

public class DoseInfoDialog extends DialogFragment {
    private final long doseId;
    private final DBHelper db;
    private final TextView textView;
    private TextInputEditText timeTaken;
    private TextInputEditText dateTaken;

    public DoseInfoDialog(long doseId, DBHelper database, TextView tv) {
        this.doseId = doseId;
        db = database;
        textView = tv;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstances) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        Medication med = ((Triple<Medication, Long, LocalDateTime>) textView.getTag()).getFirst();
        AlertDialog infoDialog;

        builder.setView(inflater.inflate(R.layout.dialog_dose_info, null));
        builder.setTitle(R.string.this_dose);

        builder.setPositiveButton(getString(R.string.save), ((dialogInterface, i) -> save()));
        builder.setNegativeButton(R.string.close, ((dialogInterface, i) -> dismiss()));

        if (med.getFrequency() == 0) {
            builder.setNeutralButton(R.string.delete, ((dialogInterface, i) -> deleteAsNeededDose()));
        }

        infoDialog = builder.create();
        infoDialog.show();

        infoDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        timeTaken = infoDialog.findViewById(R.id.dose_time_taken);
        dateTaken = infoDialog.findViewById(R.id.dose_date_taken);

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
            TextWatcher tw = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    infoDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                }
            };

            timeTaken.addTextChangedListener(tw);
            dateTaken.addTextChangedListener(tw);
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
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        db.updateDoseStatus(
                doseId,
                TimeFormatting.localDateTimeToString(dateTime),
                true
        );

        dismiss();
    }

    private void deleteAsNeededDose() {
        db.deleteDose(doseId);

        Fragment fragment = getParentFragment();

        Dose dose = new Dose(doseId, -1, true, LocalDateTime.of((LocalDate) dateTaken.getTag(), (LocalTime) timeTaken.getTag()));

        if (fragment instanceof IDialogCloseListener) {
            ((IDialogCloseListener) fragment).handleDialogClose(
                    IDialogCloseListener.Action.DELETE, dose
            );
        }
    }
}
