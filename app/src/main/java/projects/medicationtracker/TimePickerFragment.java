package projects.medicationtracker;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener
{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        String chosenTime = "At: \n\n";
        String amOrPm;

        if (hourOfDay >= 12)
        {
            chosenTime += String.valueOf(hourOfDay - 12);
            amOrPm = " PM";
        }
        else
        {
            chosenTime += String.valueOf(hourOfDay);
            amOrPm = " AM";
        }

        chosenTime += ":" + String.valueOf(minute) + amOrPm;

        TextView textView = getActivity().findViewById(R.id.takenDaily);
        textView.setText(chosenTime);
    }
}
