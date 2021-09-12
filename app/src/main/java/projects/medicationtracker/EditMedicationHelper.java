package projects.medicationtracker;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.w3c.dom.Text;

import java.time.LocalTime;


public class EditMedicationHelper
{
    final DBHelper db;
    final Medication medication;
    final Activity activity;
    final LocalTime[] medicationTimes;

    /**
     * Builds UI for edit medication activity.
     * @param medication The medication to edit.
     * @param activity Edit medication activity.
     */
    public EditMedicationHelper(Medication medication, Activity activity)
    {
        this.medication = medication;
        this.activity = activity;
        this.db = new DBHelper(activity.getBaseContext());
        this.medicationTimes = db.getMedicationTimes(medication.getMedId());

        // Load values into GUI
        setPatientButtons();
        setMedicationName();
        setAlias();
        setDosage();
        setFrequencySpinner();
        setFrequencyButtons();

        // Set listeners
        setNameRadioButtonListeners();
        setFrequencyButtonListeners();
        setEnterTimesPerDayListener();
        setDailyListener();
        setCustomFrequencyTextViewListeners();
    }

    /**
     * Creates buttons for editing patient name.
     */
    private void setPatientButtons()
    {
        // Set radio button
        if (medication.getPatientName().equals("ME!"))
        {
            RadioButton meButton = activity.findViewById(R.id.meButtonEdit);
            meButton.setChecked(true);
        }
        else
        {
            RadioButton otherButton = activity.findViewById(R.id.otherButtonEdit);
            otherButton.setChecked(true);

            EditText enterPatientName = activity.findViewById(R.id.editPatientNameEditText);
            enterPatientName.setText(medication.getPatientName());
            enterPatientName.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Pre-fills medication name
     */
    private void setMedicationName()
    {
        EditText enterMedicationName = activity.findViewById(R.id.editMedicationName);
        enterMedicationName.setText(medication.getMedName());
    }

    /**
     * Enters alias if exists
     */
    private void setAlias()
    {
        EditText enterAlias = activity.findViewById(R.id.editAlias);
        enterAlias.setText(medication.getAlias());
    }

    /**
     * Fills dosage field
     */
    private void setDosage()
    {
        EditText enterDosage = activity.findViewById(R.id.editMedDosageEnter);
        enterDosage.setText(String.valueOf(medication.getMedDosage()));

        EditText enterDosageUnits = activity.findViewById(R.id.editEnterMedUnits);
        enterDosageUnits.setText(medication.getMedDosageUnits());
    }

    /**
     * Selects the correct
     */
    private void setFrequencyButtons()
    {
        RadioButton button;

        LinearLayout customerFreqLayout = activity.findViewById(R.id.editCustomFrequencyLayout);
        EditText enterNumberOfTimesPerDay = activity.findViewById(R.id.editNumTimesTaken);
        TextView timeTaken = activity.findViewById(R.id.editTimeTaken1);
        Spinner frequencySpinner = activity.findViewById(R.id.editFrequencySpinner);
        long medFrequency = medication.getMedFrequency();

        if (medFrequency != 1440)
        {
            button = activity.findViewById(R.id.editCustomFreqButton);
            customerFreqLayout.setVisibility(View.VISIBLE);
            EditText takenEvery = activity.findViewById(R.id.editEnterFrequency);
            TextView dateSelect = activity.findViewById(R.id.editStartDate);
            TextView timeSelect = activity.findViewById(R.id.editStartTime);

            if (medFrequency % 1440 == 0)
            {
                takenEvery.setText(String.valueOf(medication.getMedFrequency() / 1440));
                frequencySpinner.setSelection(1);
            }
            else if (medFrequency % (1440 * 7) == 0)
            {
                takenEvery.setText(String.valueOf(medFrequency % (1440 / 7)));
                frequencySpinner.setSelection(2);
            }
            else
            {
                takenEvery.setText(String.valueOf(medFrequency / 60));
                frequencySpinner.setSelection(0);
            }

            dateSelect.setText(TimeFormatting.localDateToString(medication.getStartDate().toLocalDate()));
            timeSelect.setText(TimeFormatting.localTimeToString(medication.getStartDate().toLocalTime()));
        }
        else if (medicationTimes.length > 1)
        {
            button = activity.findViewById(R.id.editMultiplePerDay);
            enterNumberOfTimesPerDay.setVisibility(View.VISIBLE);

            int numTimes = medicationTimes.length;

            enterNumberOfTimesPerDay.setText(String.valueOf(numTimes));
            createMultiplePerDayTextViews(numTimes);
        }
        else
        {
            button = activity.findViewById(R.id.editDailyButton);
            timeTaken.setVisibility(View.VISIBLE);
            timeTaken.setText(TimeFormatting.localTimeToString(medicationTimes[0]));
            timeTaken.setTag(medicationTimes[0]);
        }
        
        button.setChecked(true);
    }

    /**
     * Sets listeners for frequency name input radio buttons
     */
    private void setNameRadioButtonListeners()
    {
        RadioButton meButton = activity.findViewById(R.id.meButtonEdit);
        RadioButton otherPatient = activity.findViewById(R.id.otherButtonEdit);
        EditText enterName = activity.findViewById(R.id.editPatientNameEditText);

        meButton.setOnCheckedChangeListener((compoundButton, b) ->
        {
            if (meButton.isChecked())
                enterName.setVisibility(View.GONE);
            else
                enterName.setVisibility(View.VISIBLE);
        });

        otherPatient.setOnCheckedChangeListener(((CompoundButton, b) ->
        {
            if (otherPatient.isChecked())
                enterName.setVisibility(View.VISIBLE);
            else
                enterName.setVisibility(View.GONE);
        }));
    }

    /**
     *  Sets listeners for frequency options
     */
    private void setFrequencyButtonListeners()
    {
        RadioButton multiplePerDay = activity.findViewById(R.id.editMultiplePerDay);
        RadioButton dailyButton = activity.findViewById(R.id.editDailyButton);
        RadioButton customFreq = activity.findViewById(R.id.editCustomFreqButton);

        LinearLayout customerFreqLayout = activity.findViewById(R.id.editCustomFrequencyLayout);
        LinearLayout timesOfDay = activity.findViewById(R.id.editTimesInDay);
        EditText enterNumberOfTimesPerDay = activity.findViewById(R.id.editNumTimesTaken);
        TextView timeTaken = activity.findViewById(R.id.editTimeTaken1);
        TextView timesPerDay = activity.findViewById(R.id.timesPerDayLabel);

        multiplePerDay.setOnCheckedChangeListener(((compoundButton, b) ->
        {
            if (multiplePerDay.isChecked())
            {
                enterNumberOfTimesPerDay.setVisibility(View.VISIBLE);
                timeTaken.setVisibility(View.GONE);
                customerFreqLayout.setVisibility(View.GONE);
                timesPerDay.setVisibility(View.VISIBLE);
                timesOfDay.setVisibility(View.VISIBLE);
            }
        }));

        dailyButton.setOnCheckedChangeListener(((compoundButton, b) ->
        {
            if (dailyButton.isChecked())
            {
                timeTaken.setVisibility(View.VISIBLE);
                timesPerDay.setVisibility(View.GONE);
                enterNumberOfTimesPerDay.setVisibility(View.GONE);
                customerFreqLayout.setVisibility(View.GONE);
                timesOfDay.setVisibility(View.GONE);
            }
        }));

        customFreq.setOnCheckedChangeListener(((compoundButton, b) ->
        {
            if (customFreq.isChecked())
            {
                customerFreqLayout.setVisibility(View.VISIBLE);
                timeTaken.setVisibility(View.GONE);
                enterNumberOfTimesPerDay.setVisibility(View.GONE);
                timesPerDay.setVisibility(View.GONE);
                timesOfDay.setVisibility(View.GONE);
            }
        }));
    }

    /**
     * Sets listeners for time entry text views
     */
    private void setEnterTimesPerDayListener()
    {
        EditText timesPerDay = activity.findViewById(R.id.editNumTimesTaken);
        LinearLayout timesOfDay = activity.findViewById(R.id.editTimesInDay);

        timesPerDay.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable)
            {
                timesOfDay.removeAllViews();

                if (!timesPerDay.getText().toString().equals(""))
                {
                    int dailyDoses = Integer.parseInt(timesPerDay.getText().toString());

                    createMultiplePerDayTextViews(dailyDoses);
                }
            }
        });
    }

    /**
     * Sets listener for daily time TextView, launches time picker fragment
     */
    private void setDailyListener()
    {
        TextView dailyTime = activity.findViewById(R.id.editTimeTaken1);

        dailyTime.setOnClickListener(view ->
        {
            FragmentManager fm = ((FragmentActivity)activity).getSupportFragmentManager();

            DialogFragment dialogFragment = new TimePickerFragment(dailyTime.getId());
            dialogFragment.show(fm, null);
        });
    }

    /**
     * Sets listeners for editStartDate and editStartTime. Tapping editStartDate opens a
     * SelectDateFragment and tapping editStartTime opens a TimePickerFragment.
     */
    private void setCustomFrequencyTextViewListeners()
    {
        TextView selectDate = activity.findViewById(R.id.editStartDate);
        TextView selectTime = activity.findViewById(R.id.editStartTime);
        FragmentManager fm = ((FragmentActivity)activity).getSupportFragmentManager();

        selectDate.setOnClickListener(view ->
        {
            DialogFragment df = new SelectDateFragment(selectDate.getId());
            df.show(fm, null);
        });

        selectTime.setOnClickListener(view ->
        {
            DialogFragment df = new TimePickerFragment(selectTime.getId());
            df.show(fm, null);
        });
    }

    /**
     * Fills time frequency spinner.
     */
    private void setFrequencySpinner()
    {
        Spinner timesSpinner = activity.findViewById(R.id.editFrequencySpinner);

        String[] spinnerFrequencies = {"Hour(s)", "Day(s)", "week(s)"};
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, spinnerFrequencies);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timesSpinner.setAdapter(frequencyAdapter);
    }

    /**
     * Creates TextViews based on the number of times per day the user takes a medication
     * @param dailyDoses The number of dose taken per day.
     */
    private void createMultiplePerDayTextViews(int dailyDoses)
    {
        LinearLayout timesOfDay = activity.findViewById(R.id.editTimesInDay);

        for (int i = 0; i < dailyDoses; i++)
        {
            final int id = i;
            TextView tv = new TextView(activity);
            tv.setId(i);

            TextViewUtils.setTextViewParams(tv, "Tap to set time", timesOfDay);

            tv.setOnClickListener(view ->
            {
                FragmentManager fm = ((FragmentActivity)activity).getSupportFragmentManager();

                DialogFragment dialogFragment = new TimePickerFragment(id);
                dialogFragment.show(fm, null);
            });

            if (i < medicationTimes.length)
            {
                int hourAndMin[] = {medicationTimes[i].getHour(), medicationTimes[i].getMinute()};
                String time = TimeFormatting.formatTimeForDB(hourAndMin[0], hourAndMin[1]);

                tv.setText(TimeFormatting.localTimeToString(medicationTimes[i]));
                tv.setTag(time);
            }
        }
    }
}
