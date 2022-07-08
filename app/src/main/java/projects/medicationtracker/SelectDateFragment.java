package projects.medicationtracker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDate;

public class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
{
    private final int id;

    /**
     * Creates a new SelectDateFragment
     * @param viewId The id of the View, ideally a Text View, to write the selected date to
     *************************************************************************/
    SelectDateFragment (int viewId) { id = viewId; }

    /**
     * Instructions for how to create fragment
     * @param savedInstanceState
     * @return A date picker dialog
     *************************************************************************/
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, yy, mm, dd);
    }

    /**
     * Instructions for what to do when user picks a date
     * @param view The DatePicker
     * @param yy Year
     * @param mm Month
     * @param dd Day
     *************************************************************************/
    public void onDateSet(DatePicker view, int yy, int mm, int dd)
    {
        populateSetDate(yy, mm+1, dd);
    }

    /**
     * Set writes date chosen by user to View whose id was passed to constructor
     * @param year Year chosen by user
     * @param month Month chosen by user
     * @param day Day chosen by user
     *************************************************************************/
    public void populateSetDate(int year, int month, int day)
    {
        TextView textView = getActivity().findViewById(id);
        LocalDate localDate = LocalDate.of(year, month, day);

        String date = month + "/" + day + "/" + year;
        textView.setText(date);
        textView.setTag(localDate);
    }
}
