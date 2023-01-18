package projects.medicationtracker.Fragments;

import static projects.medicationtracker.Helpers.TimeFormatting.formatTimeForUser;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.time.LocalTime;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener
{
    private final TextView tv;

    /**
     * Constructor
     * @param textView TextView that will store the selected time.
     */
    public TimePickerFragment(TextView textView)
    {
        tv = textView;
    }

    /**
     * Creates a TimePickerFragment allowing user to set time
     * @param savedInstanceState Saved instance from previous state
     * @return A TimePickerDialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    /**
     * Set text of TextView whose ID was passed to the constructor to chosen time
     * @param view The TimePicker created in onCreateDialog
     * @param hourOfDay Hour chosen by user
     * @param minute Minute chosen by user
     */
    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        String chosenTime;

        chosenTime = formatTimeForUser(hourOfDay, minute);

        tv.clearFocus();

        tv.setTag(LocalTime.of(hourOfDay, minute));
        tv.setText(chosenTime);
    }
}
