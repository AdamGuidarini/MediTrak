package projects.medicationtracker;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import static projects.medicationtracker.TimeFormatting.formatTimeForUser;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener
{
    private final int textViewId;

    /**
     * Constructor
     * @param id Id of text view that will store selected time
     **************************************************************************/
    TimePickerFragment (int id)
    {
        textViewId = id;
    }

    /**
     * Creates a TimePickerFragment allowing user to set time
     * @param savedInstanceState Saved instance from previous state
     * @return A TimePickerDialog
     **************************************************************************/
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
     **************************************************************************/
    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        String chosenTime;
        int[] hourAndMin = {hourOfDay, minute};

        chosenTime = formatTimeForUser(hourOfDay, minute);

        TextView textView = getActivity().findViewById(textViewId);

        textView.setTag(hourAndMin);
        textView.setText(chosenTime);
    }
}
