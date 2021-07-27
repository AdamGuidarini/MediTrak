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

import java.time.LocalTime;


public class EditMedicationHelper
{
    final DBHelper db;
    final Medication medication;
    final Activity activity;
    final LocalTime[] medicationTimes;

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
        setFrequencyButton();

        // Set listeners
        setNameRadioButtonListeners();
        setFrequencyButtonListeners();
    }

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

    private void setMedicationName()
    {
        EditText enterMedicationName = activity.findViewById(R.id.editMedicationName);
        enterMedicationName.setText(medication.getMedName());
    }

    private void setAlias()
    {
        EditText enterAlias = activity.findViewById(R.id.editAlias);
        enterAlias.setText(medication.getAlias());
    }

    private void setDosage()
    {
        EditText enterDosage = activity.findViewById(R.id.editMedDosageEnter);
        enterDosage.setText(String.valueOf(medication.getMedDosage()));

        EditText enterDosageUnits = activity.findViewById(R.id.editEnterMedUnits);
        enterDosageUnits.setText(medication.getMedDosageUnits());
    }

    private void setFrequencyButton()
    {
        RadioButton button;

        LinearLayout customerFreqLayout = activity.findViewById(R.id.editCustomFrequencyLayout);
        EditText enterNumberOfTimesPerDay = activity.findViewById(R.id.editNumTimesTaken);
        TextView timeTaken = activity.findViewById(R.id.editTimeTaken1);

        if (medication.getMedFrequency() != 1440)
        {
            button = activity.findViewById(R.id.editCustomFreqButton);
            customerFreqLayout.setVisibility(View.VISIBLE);
        }
        else if (medicationTimes.length > 1)
        {
            button = activity.findViewById(R.id.editMultiplePerDay);
            enterNumberOfTimesPerDay.setVisibility(View.VISIBLE);
        }
        else
        {
            button = activity.findViewById(R.id.editDailyButton);
            timeTaken.setVisibility(View.VISIBLE);
        }
        
        button.setChecked(true);
    }

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

    private void setFrequencyButtonListeners()
    {
        RadioButton multiplePerDay = activity.findViewById(R.id.editMultiplePerDay);
        RadioButton dailyButton = activity.findViewById(R.id.editDailyButton);
        RadioButton customFreq = activity.findViewById(R.id.editCustomFreqButton);

        LinearLayout customerFreqLayout = activity.findViewById(R.id.editCustomFrequencyLayout);
        EditText enterNumberOfTimesPerDay = activity.findViewById(R.id.editNumTimesTaken);
        TextView timeTaken = activity.findViewById(R.id.editTimeTaken1);

        multiplePerDay.setOnCheckedChangeListener(((compoundButton, b) ->
        {
            if (multiplePerDay.isChecked())
            {
                enterNumberOfTimesPerDay.setVisibility(View.VISIBLE);
                timeTaken.setVisibility(View.GONE);
                customerFreqLayout.setVisibility(View.GONE);
            }
        }));

        dailyButton.setOnCheckedChangeListener(((compoundButton, b) ->
        {
            if (dailyButton.isChecked())
            {
                timeTaken.setVisibility(View.VISIBLE);
                enterNumberOfTimesPerDay.setVisibility(View.GONE);
                customerFreqLayout.setVisibility(View.GONE);
            }
        }));

        customFreq.setOnCheckedChangeListener(((compoundButton, b) ->
        {
            if (customFreq.isChecked())
            {
                customerFreqLayout.setVisibility(View.VISIBLE);
                timeTaken.setVisibility(View.GONE);
                enterNumberOfTimesPerDay.setVisibility(View.GONE);

                setFrequencySpinner();
            }
        }));
    }

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
                    }
                }
            }
        });
    }

    private void setFrequencySpinner()
    {
        Spinner timesSpinner = activity.findViewById(R.id.editFrequencySpinner);

        String[] spinnerFrequencies = {"Hour(s)", "Day(s)", "week(s)"};
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, spinnerFrequencies);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timesSpinner.setAdapter(frequencyAdapter);
    }
}
