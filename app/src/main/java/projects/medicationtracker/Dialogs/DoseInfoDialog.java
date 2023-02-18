package projects.medicationtracker.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
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

import kotlin.Triple;
import projects.medicationtracker.Fragments.SelectDateFragment;
import projects.medicationtracker.Fragments.TimePickerFragment;
import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.TimeFormatting;
import projects.medicationtracker.Interfaces.IDialogCloseListener;
import projects.medicationtracker.R;
import projects.medicationtracker.SimpleClasses.Medication;

public class DoseInfoDialog extends DialogFragment
{
    private final long doseId;
    private final DBHelper db;
    private final TextView textView;
    private boolean changed = false;
    private TextInputEditText timeTaken;
    private TextInputEditText dateTaken;

    public DoseInfoDialog(long doseId, DBHelper database, TextView tv)
    {
        this.doseId = doseId;
        db = database;
        textView = tv;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstances)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        Medication med = ((Triple<Medication, Long, LocalDateTime>) textView.getTag()).getFirst();

        builder.setView(inflater.inflate(R.layout.dialog_dose_info, null));
        builder.setTitle(R.string.this_dose);

        builder.setPositiveButton(getString(R.string.save), ((dialogInterface, i) -> save()));
        builder.setNegativeButton(R.string.close, ((dialogInterface, i) -> dismiss()));

        if (med.getFrequency() == 0)
        {
            builder.setNeutralButton(R.string.delete, ((dialogInterface, i) -> deleteAsNeededDose()));
        }

        return builder.create();
    }


    @Override
    public void onCancel(@NonNull DialogInterface dialog)
    {
        super.onCancel(dialog);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (!db.getTaken(doseId))
        {
            getDialog().findViewById(R.id.notTakenMessage).setVisibility(View.VISIBLE);
        }
        else
        {
            LocalDateTime doseDate = db.getTimeTaken(doseId);

            timeTaken = getDialog().findViewById(R.id.dose_time_taken);
            dateTaken = getDialog().findViewById(R.id.dose_date_taken);

            timeTaken.setShowSoftInputOnFocus(false);
            dateTaken.setShowSoftInputOnFocus(false);

            timeTaken.setText(TimeFormatting.localTimeToString(doseDate.toLocalTime()));
            timeTaken.setTag(doseDate.toLocalTime());

            dateTaken.setText(TimeFormatting.localDateToString(doseDate.toLocalDate()));
            dateTaken.setTag(doseDate.toLocalDate());

            timeTaken.setOnFocusChangeListener((view, b) ->
            {
                if (b)
                {
                    DialogFragment timePicker = new TimePickerFragment(timeTaken);
                    timePicker.show(getParentFragmentManager(), null);

                    changed = true;
                }
            });

            dateTaken.setOnFocusChangeListener((view, b) ->
            {
                if (b)
                {
                    DialogFragment datePicker = new SelectDateFragment(dateTaken);
                    datePicker.show(getParentFragmentManager(), null);

                    changed = true;
                }
            });

            getDialog().findViewById(R.id.dose_time_details).setVisibility(View.VISIBLE);
        }
    }

    private void save()
    {
        if(changed)
        {
            LocalDate date = (LocalDate) dateTaken.getTag();
            LocalTime time = (LocalTime) timeTaken.getTag();
            LocalDateTime dateTime = LocalDateTime.of(date, time);

            db.updateDoseStatus(
                    doseId,
                    TimeFormatting.localDateTimeToString(dateTime),
                    true
            );
        }

        dismiss();
    }

    private void deleteAsNeededDose()
    {
        db.deleteDose(doseId);

        Fragment fragment = getParentFragment();

        if (fragment instanceof IDialogCloseListener)
        {
            ((IDialogCloseListener) fragment).handleDialogClose(
                    IDialogCloseListener.Action.DELETE, doseId
            );
        }
    }
}
