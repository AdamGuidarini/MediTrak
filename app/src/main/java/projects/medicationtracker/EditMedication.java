package projects.medicationtracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import projects.medicationtracker.Fragments.ConfirmMedicationDeleteFragment;
import projects.medicationtracker.Fragments.SelectDateFragment;
import projects.medicationtracker.Fragments.TimePickerFragment;
import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.InputValidation;
import projects.medicationtracker.Helpers.NotificationHelper;
import projects.medicationtracker.Helpers.TextViewUtils;
import projects.medicationtracker.Helpers.TimeFormatting;
import projects.medicationtracker.SimpleClasses.Medication;

public class EditMedication extends AppCompatActivity
{
    final DBHelper db = new DBHelper(this);
    LocalTime[] medicationTimes;
    Medication medication;

    /**
     * Instructions on how to build the EditMedications activity
     * @param savedInstanceState Saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_medication);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Medication");

        medication = db.getMedication(getIntent().getLongExtra("medId", 0));

        medicationTimes = db.getMedicationTimes(medication.getMedId());

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
     * Creates options menu for activity
     * @param menu Displayed menu
     * @return True if can be created, else false
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.edit_meds_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Determines which button was selected
     * @param item Selected menu option
     * @return Selected option
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            Intent intent = new Intent(this, MyMedications.class);
            finish();
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Return to MainActivity if back arrow is pressed.
     */
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MyMedications.class);
        finish();
        startActivity(intent);
    }

    /**
     * Deletes medication
     * @param item Menu item represented by a garbage can icon.
     */
    public void onDeleteMedClick(MenuItem item)
    {
        ConfirmMedicationDeleteFragment confirmMedicationDeleteFragment =
                new ConfirmMedicationDeleteFragment(db, medication);
        confirmMedicationDeleteFragment.show(getSupportFragmentManager(), null);
    }

    /**
     * Saves changes to medication.
     * @param item The save icon.
     */
    public void onSaveEditClick(MenuItem item)
    {
        if (validateUpdate())
        {
            Intent intent = new Intent(this, MyMedications.class);

            changeMedicationValues();

            clearPreviousNotifications();

            db.updateMedication(medication);

            finish();
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Please complete the required fields",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates buttons for editing patient name.
     */
    private void setPatientButtons()
    {
        // Set radio button
        if (medication.getPatientName().equals("ME!"))
        {
            RadioButton meButton = this.findViewById(R.id.meButtonEdit);
            meButton.setChecked(true);
        }
        else
        {
            RadioButton otherButton = this.findViewById(R.id.otherButtonEdit);
            otherButton.setChecked(true);

            EditText enterPatientName = this.findViewById(R.id.editPatientNameEditText);
            enterPatientName.setText(medication.getPatientName());
            enterPatientName.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Pre-fills medication name
     */
    private void setMedicationName()
    {
        EditText enterMedicationName = this.findViewById(R.id.editMedicationName);
        enterMedicationName.setText(medication.getMedName());
    }

    /**
     * Enters alias if exists
     */
    private void setAlias()
    {
        EditText enterAlias = this.findViewById(R.id.editAlias);
        enterAlias.setText(medication.getAlias());
    }

    /**
     * Fills dosage field
     */
    private void setDosage()
    {
        EditText enterDosage = this.findViewById(R.id.editMedDosageEnter);
        enterDosage.setText(String.valueOf(medication.getMedDosage()));

        EditText enterDosageUnits = this.findViewById(R.id.editEnterMedUnits);
        enterDosageUnits.setText(medication.getMedDosageUnits());
    }

    /**
     * Selects the correct
     */
    private void setFrequencyButtons()
    {
        RadioButton button;

        LinearLayout customerFreqLayout = this.findViewById(R.id.editCustomFrequencyLayout);
        EditText enterNumberOfTimesPerDay = this.findViewById(R.id.editNumTimesTaken);
        TextView timeTaken = this.findViewById(R.id.editTimeTaken1);
        Spinner frequencySpinner = this.findViewById(R.id.editFrequencySpinner);
        long medFrequency = medication.getMedFrequency();

        if (medFrequency != 1440)
        {
            button = this.findViewById(R.id.editCustomFreqButton);
            customerFreqLayout.setVisibility(View.VISIBLE);
            EditText takenEvery = this.findViewById(R.id.editEnterFrequency);
            TextView dateSelect = this.findViewById(R.id.editStartDate);
            TextView timeSelect = this.findViewById(R.id.editStartTime);

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
            button = this.findViewById(R.id.editMultiplePerDay);
            enterNumberOfTimesPerDay.setVisibility(View.VISIBLE);

            int numTimes = medicationTimes.length;

            enterNumberOfTimesPerDay.setText(String.valueOf(numTimes));
            createMultiplePerDayTextViews(numTimes);
        }
        else
        {
            button = this.findViewById(R.id.editDailyButton);
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
        RadioButton meButton = this.findViewById(R.id.meButtonEdit);
        RadioButton otherPatient = this.findViewById(R.id.otherButtonEdit);
        EditText enterName = this.findViewById(R.id.editPatientNameEditText);

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
        RadioButton multiplePerDay = this.findViewById(R.id.editMultiplePerDay);
        RadioButton dailyButton = this.findViewById(R.id.editDailyButton);
        RadioButton customFreq = this.findViewById(R.id.editCustomFreqButton);

        LinearLayout customerFreqLayout = this.findViewById(R.id.editCustomFrequencyLayout);
        LinearLayout timesOfDay = this.findViewById(R.id.editTimesInDay);
        EditText enterNumberOfTimesPerDay = this.findViewById(R.id.editNumTimesTaken);
        TextView timeTaken = this.findViewById(R.id.editTimeTaken1);
        TextView timesPerDay = this.findViewById(R.id.timesPerDayLabel);

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
        EditText timesPerDay = this.findViewById(R.id.editNumTimesTaken);
        LinearLayout timesOfDay = this.findViewById(R.id.editTimesInDay);

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
        TextView dailyTime = this.findViewById(R.id.editTimeTaken1);

        dailyTime.setOnClickListener(view ->
        {
            FragmentManager fm = this.getSupportFragmentManager();

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
        TextView selectDate = this.findViewById(R.id.editStartDate);
        TextView selectTime = this.findViewById(R.id.editStartTime);
        FragmentManager fm = this.getSupportFragmentManager();

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
        Spinner timesSpinner = this.findViewById(R.id.editFrequencySpinner);

        String[] spinnerFrequencies = {"Hour(s)", "Day(s)", "week(s)"};
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerFrequencies);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timesSpinner.setAdapter(frequencyAdapter);
    }

    /**
     * Creates TextViews based on the number of times per day the user takes a medication
     * @param dailyDoses The number of dose taken per day.
     */
    private void createMultiplePerDayTextViews(int dailyDoses)
    {
        LinearLayout timesOfDay = this.findViewById(R.id.editTimesInDay);

        for (int i = 0; i < dailyDoses; i++)
        {
            final int id = i;
            TextView tv = new TextView(this);
            tv.setId(i);

            TextViewUtils.setTextViewParams(tv, "Tap to set time", timesOfDay);

            tv.setOnClickListener(view ->
            {
                FragmentManager fm = this.getSupportFragmentManager();

                DialogFragment dialogFragment = new TimePickerFragment(id);
                dialogFragment.show(fm, null);
            });

            if (i < medicationTimes.length)
            {
                int hour = medicationTimes[i].getHour();
                int minute = medicationTimes[i].getMinute();

                String dbTime = TimeFormatting.formatTimeForDB(hour, minute);

                String time = TimeFormatting.formatTimeForUser(hour, minute);

                tv.setText(time);
                tv.setTag(dbTime);
            }
        }
    }

    /**
     * Validates input.
     * @return True if submitted, false if not.
     */
    public boolean validateUpdate()
    {
        boolean inputIsValid = true;
        LinearLayout timesOfDay = findViewById(R.id.editTimesInDay);
        RadioGroup patientGroup = findViewById(R.id.editPatientGroup);
        RadioButton multiplePerDay = findViewById(R.id.editMultiplePerDay);
        RadioButton dailyButton = findViewById(R.id.editDailyButton);
        EditText patientName = findViewById(R.id.editPatientNameEditText);
        EditText medName = findViewById(R.id.editMedicationName);
        EditText dosage = findViewById(R.id.editMedDosageEnter);
        EditText dosageUnits = findViewById(R.id.editEnterMedUnits);
        Toast fillOutFields = Toast.makeText(this, "Please set all requested times", Toast.LENGTH_SHORT);

        if (patientGroup.getChildAt(1).isSelected() && patientName.getText().toString().equals(""))
        {
            inputIsValid = false;
            patientName.setError("Enter a name or select \"Me\"");
        }

        if (medName.getText().toString().equals(""))
        {
            inputIsValid = false;
            medName.setError("Enter a name for this medication");
        }

        String enteredDosage = dosage.getText().toString();
        if (enteredDosage.equals("") || !InputValidation.isValidInt(enteredDosage))
        {
            inputIsValid = false;
            dosage.setError("Enter medication dosage");
        }

        if (dosageUnits.getText().toString().equals(""))
        {
            inputIsValid = false;
            dosageUnits.setError("Enter a unit for this medication's dosage");
        }

        if (multiplePerDay.isChecked())
        {
            EditText timesTaken = findViewById(R.id.editNumTimesTaken);

            if (timesTaken.getText().toString().equals(""))
            {
                inputIsValid = false;
                timesTaken.setError("Enter number of times per day to take medication");
            }
            else
            {
                int childCount = timesOfDay.getChildCount();

                for (int i = 0; i < childCount; i++)
                {
                    TextView tv = (TextView) timesOfDay.getChildAt(i);

                    if (tv.getText().toString().equals("Tap to set time"))
                    {
                        inputIsValid = false;
                        fillOutFields.show();
                    }
                }
            }
        }
        else if (dailyButton.isChecked())
        {
            TextView dailyTime = findViewById(R.id.editTimeTaken1);

            if (dailyTime.getText().toString().equals(getResources().getString(R.string.atThisTime)))
            {
                inputIsValid = false;
                fillOutFields.show();
            }
        }
        else
        {
            EditText numberOfTimes = findViewById(R.id.editEnterFrequency);

            if (numberOfTimes.getText().toString().equals(""))
            {
                inputIsValid = false;
                numberOfTimes.setError("Enter a frequency.");
            }
            else
            {
                TextView selectDate = findViewById(R.id.editStartDate);
                TextView selectTime = findViewById(R.id.editStartTime);

                String selectDateText = selectDate.getText().toString();
                String selectTimeText = selectTime.getText().toString();

                inputIsValid = selectDateText.equals(getResources()
                        .getString(R.string.tap_to_set_date)) ==
                        selectTimeText.equals(getResources().getString(R.string.tap_to_set_time));

                if (!inputIsValid)
                    fillOutFields.show();
            }
        }

        return inputIsValid;
    }

    /**
     *  Updates the medication object to contain new medication information
     */
    private void changeMedicationValues()
    {
        RadioButton otherPatient = findViewById(R.id.otherButtonEdit);
        RadioButton multiplePerDay = findViewById(R.id.editMultiplePerDay);
        RadioButton dailyButton = findViewById(R.id.editDailyButton);
        EditText patientName = findViewById(R.id.editPatientNameEditText);
        EditText medicationName = findViewById(R.id.editMedicationName);
        EditText alias = findViewById(R.id.editAlias);
        EditText dosage = findViewById(R.id.editMedDosageEnter);
        EditText dosageUnits = findViewById(R.id.editEnterMedUnits);

        if (otherPatient.isChecked())
            medication.setPatientName(patientName.getText().toString());
        else
            medication.setPatientName("ME!");

        medication.setMedName(medicationName.getText().toString());
        medication.setAlias(alias.getText().toString());
        medication.setMedDosage(Integer.parseInt(dosage.getText().toString()));
        medication.setMedDosageUnits(dosageUnits.getText().toString());

        if (multiplePerDay.isChecked())
        {
            LinearLayout timeLayout = findViewById(R.id.editTimesInDay);
            LocalTime[] times = new LocalTime[timeLayout.getChildCount()];

            for (int i = 0; i < timeLayout.getChildCount(); i++)
            {
                TextView tv = (TextView) timeLayout.getChildAt(i);

                String time = (String) tv.getTag();

                times[i] = LocalTime.parse(time);
            }

            LocalDateTime[] dateTimes = new LocalDateTime[times.length];

            for (int i = 0; i < times.length; i++)
            {
                dateTimes[i] = LocalDateTime.of(medication.getStartDate().toLocalDate(), times[i]);
            }

            medication.setMedFrequency(1440);
            medication.setTimes(dateTimes);
        }
        else if (dailyButton.isChecked())
        {
            TextView dailyTime = findViewById(R.id.editTimeTaken1);

            LocalTime time;

            try
            {
                time = (LocalTime) dailyTime.getTag();
            }
            catch (ClassCastException e)
            {
                e.getCause();
                time = LocalTime.parse((String) dailyTime.getTag());
            }

            LocalDateTime[] dateTime = {LocalDateTime.of(medication.getStartDate().toLocalDate(), time)};

            medication.setMedFrequency(1440);
            medication.setTimes(dateTime);
        }
        else
        {
            EditText takenEvery = findViewById(R.id.editEnterFrequency);
            Spinner frequencySpinner = findViewById(R.id.editFrequencySpinner);
            TextView startDate = findViewById(R.id.editStartDate);
            TextView startTime = findViewById(R.id.editStartTime);

            int every = Integer.parseInt(takenEvery.getText().toString());

            switch (frequencySpinner.getSelectedItemPosition())
            {
                case 2:
                    every *= 7;
                case 1:
                    every *= 24;
                case 0:
                    every *= 60;
            }

            String timeString;
            LocalTime time;

            if (startTime.getTag() != null)
            {
                timeString = (String) startTime.getTag();
                time = LocalTime.parse(timeString);
            }
            else
                time = medication.getStartDate().toLocalTime();

            LocalDate date;
            if (startDate.getTag() != null)
                date = (LocalDate) startDate.getTag();
            else
                date = medication.getStartDate().toLocalDate();

            LocalDateTime[] dateTimes = {LocalDateTime.of(date, time)};

            medication.setStartDate(dateTimes[0]);
            medication.setMedFrequency(every);
            medication.setTimes(dateTimes);
        }

        if (dailyButton.isChecked())
        {
            NotificationHelper.scheduleNotification(getApplicationContext(), medication,
                   medication.getTimes()[0], medication.getMedId());
        }
        else if (multiplePerDay.isChecked())
        {
            long[] timeIds = db.getMedicationTimeIds(medication);

            for (int i = 0; i < timeIds.length; i++)
            {
                NotificationHelper.scheduleNotification(
                        getApplicationContext(),
                        medication,
                        medication.getTimes()[i],
                        timeIds[i] * -1
                );
            }

        }
        else
        {
            NotificationHelper.scheduleNotification(
                    getApplicationContext(), medication, medication.getTimes()[0], medication.getMedId()
            );
        }

    }

    /**
     * Clears all pending notifications for a medication when it's changed
     */
    private void clearPreviousNotifications()
    {
        long[] medIds = db.getMedicationTimeIds(medication);

        if (medIds.length == 0)
        {
            NotificationHelper.deletePendingNotification(medication.getMedId(), this);
        }
        else
        {
            for (long id : medIds)
            {
                NotificationHelper.deletePendingNotification(id * -1, this);
            }
        }

    }
}
