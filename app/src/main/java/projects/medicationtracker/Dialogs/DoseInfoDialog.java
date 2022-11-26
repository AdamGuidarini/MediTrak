package projects.medicationtracker.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDateTime;

import projects.medicationtracker.Fragments.SelectDateFragment;
import projects.medicationtracker.Fragments.TimePickerFragment;
import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.TimeFormatting;
import projects.medicationtracker.R;

public class DoseInfoDialog extends DialogFragment
{
    private final long doseId;
    private DBHelper db;

    public DoseInfoDialog(long doseId, DBHelper database)
    {
        this.doseId = doseId;
        db = database;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstances)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_dose_info, null));
        builder.setTitle(R.string.this_dose);

        builder.setPositiveButton(R.string.close, ((dialogInterface, i) -> dismiss()));

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
            TextInputEditText timeTaken = getDialog().findViewById(R.id.dose_time_taken);
            TextInputEditText dateTaken = getDialog().findViewById(R.id.dose_date_taken);

            timeTaken.setShowSoftInputOnFocus(false);
            dateTaken.setShowSoftInputOnFocus(false);

            timeTaken.setOnFocusChangeListener((view, b) ->
            {
                if (b)
                {
                    DialogFragment timePicker = new TimePickerFragment(timeTaken);
                    timePicker.show(getParentFragmentManager(), null);
                }
            });

            dateTaken.setOnFocusChangeListener((view, b) ->
            {
                if (b)
                {
                    DialogFragment datePicker = new SelectDateFragment(dateTaken);
                    datePicker.show(getParentFragmentManager(), null);
                }
            });

            LocalDateTime doseDate = db.getTimeTaken(doseId);

            timeTaken.setText(TimeFormatting.localTimeToString(doseDate.toLocalTime()));
            dateTaken.setText(TimeFormatting.localDateToString(doseDate.toLocalDate()));

            getDialog().findViewById(R.id.dose_time_details).setVisibility(View.VISIBLE);
        }
    }
}
