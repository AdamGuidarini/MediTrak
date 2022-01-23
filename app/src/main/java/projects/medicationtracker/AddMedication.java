package projects.medicationtracker;

import static projects.medicationtracker.TimeFormatting.stringToLocalDateTime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class AddMedication extends AppCompatActivity
{
    RadioGroup patientGroup;
    RadioGroup frequencyGroup;
    LinearLayout linearLayout;
    LinearLayout timeLayout;
    LinearLayout timesOfTheDay;
    LinearLayout customFrequencyLayout;
    SwitchCompat aliasSwitch;
    EditText numTimesTaken;
    EditText aliasEnter;
    Spinner frequencySpinner;
    private final DBHelper dbHelper = new DBHelper(this);

    /**
     * Builds AddMedication Activity
     * @param savedInstanceState Previous state
     **************************************************************************/
    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Medication");

        patientGroup = findViewById(R.id.patientGroup);
        frequencyGroup = findViewById(R.id.frequencyGroup);
        linearLayout = findViewById(R.id.frequencyLayout);
        timeLayout = findViewById(R.id.timeLayout);
        timesOfTheDay = findViewById(R.id.timesOfTheDay);
        numTimesTaken = findViewById(R.id.numTimesTaken);
        aliasEnter = findViewById(R.id.aliasEnter);
        customFrequencyLayout = findViewById(R.id.customFrequencyLayout);
        aliasSwitch = findViewById(R.id.aliasSwitch);
        aliasSwitch = findViewById(R.id.aliasSwitch);
        frequencySpinner =  findViewById(R.id.frequencySpinner);

        CardView addMedCard = findViewById(R.id.addMedsCard);
        CardCreator.setCardParams(addMedCard);

        String[] spinnerFrequencies = {"Hour(s)", "Day(s)", "week(s)"};
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerFrequencies);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(frequencyAdapter);

        // Add array list of all patient names
        // they will then be displayed in the input field
        AutoCompleteTextView nameInput = findViewById(R.id.patientNameNotMe);
        ArrayList<String> patientNames = dbHelper.getPatients();

        patientNames.remove("ME!");

        ArrayAdapter<String> patientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, patientNames);
        nameInput.setAdapter(patientAdapter);
        nameInput.setThreshold(1);

        // Create listeners
        nameInput.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                Log.d("beforeTextChanged", String.valueOf(charSequence));
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                Log.d("OnTextChanged", String.valueOf(charSequence));
            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                Log.d("afterTextChanged", String.valueOf(editable));
            }
        });

        patientGroup.setOnCheckedChangeListener((radioGroup, i) ->
        {
            switch (radioGroup.findViewById(i).getId())
            {
                // Enables or disables to ability to add custom patient name
                // based on chosen RadioButton
                case R.id.meButton:
                    if (nameInput.getVisibility() == View.VISIBLE)
                        nameInput.setVisibility(View.GONE);
                    break;
                case R.id.otherButton:
                    nameInput.setVisibility(View.VISIBLE);
                    break;
            }
        });

        aliasSwitch.setOnCheckedChangeListener((compoundButton, b) ->
        {
            // Enables or disables ability to add an alias based on switch status
            if (aliasEnter.getVisibility() == View.GONE)
                aliasEnter.setVisibility(View.VISIBLE);
            else
            {
                aliasEnter.setVisibility(View.GONE);
                aliasEnter.setText("");
            }
        });

        // Creates a TimePickerFragment to allow the user to enter the time they wish to take
        // the medication
        numTimesTaken.addTextChangedListener(new TextWatcher()
        {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){}

            @Override
            public void afterTextChanged(Editable editable)
            {
                timesOfTheDay.removeAllViews();

                if (!numTimesTaken.getText().toString().equals(""))
                {
                    int dailyDoses = Integer.parseInt(numTimesTaken.getText().toString());
                    for (int ii = 0; ii < dailyDoses; ii++)
                    {
                        final int id = ii;

                        TextView textView = new TextView(timesOfTheDay.getContext());
                        textView.setId(id);
                        textView.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
                        // set layout params
                        TextViewUtils.setTextViewParams(textView, "Tap to set time", timesOfTheDay);

                        textView.setOnClickListener(view ->
                        {
                            DialogFragment dialogFragment = new TimePickerFragment(id);
                            dialogFragment.show(getSupportFragmentManager(), null);
                        });
                    }
                }
            }
        });

        // Listens for selected RadioButton in FrequencyGroup
        frequencyGroup.setOnCheckedChangeListener((radioGroup, i) ->
        {
            TextView timeTaken1 = findViewById(R.id.timeTaken1);
            switch (radioGroup.findViewById(i).getId())
            {
                case R.id.multplePerDayButton:
                    timeTaken1.setVisibility(View.GONE);
                    customFrequencyLayout.setVisibility(View.GONE);
                    numTimesTaken.setVisibility(View.VISIBLE);
                    numTimesTaken.setText("");
                    break;
                case R.id.dailyButton:
                    numTimesTaken.setVisibility(View.GONE);
                    customFrequencyLayout.setVisibility(View.GONE);
                    timesOfTheDay.removeAllViews();
                    timeTaken1.setVisibility(View.VISIBLE);
                    timeTaken1.setText(R.string.atThisTime);

                    TextViewUtils.setTextViewFontAndPadding(timeTaken1);

                    final int id = timeTaken1.getId();

                    timeTaken1.setOnClickListener(view ->
                    {
                        DialogFragment dialogFragment = new TimePickerFragment(id);
                        dialogFragment.show(getSupportFragmentManager(), null);
                    });
                    break;
                case R.id.customFreqButton:
                    timeTaken1.setVisibility(View.GONE);
                    numTimesTaken.setVisibility(View.GONE);
                    timesOfTheDay.removeAllViews();
                    customFrequencyLayout.setVisibility(View.VISIBLE);

                    TextView startDate = findViewById(R.id.startDate);
                    TextView startTime = findViewById(R.id.startTime);

                    TimeFormatting.getCurrentTimeAndDate(startDate, startTime);

                    startDate.setOnClickListener(view ->
                    {
                        DialogFragment dialogFragment = new SelectDateFragment(R.id.startDate);
                        dialogFragment.show(getSupportFragmentManager(), null);
                    });

                    startTime.setOnClickListener(view ->
                    {
                        DialogFragment dialogFragment = new TimePickerFragment(R.id.startTime);
                        dialogFragment.show(getSupportFragmentManager(), null);
                    });
                    break;
            }
        });
    }

    /**
     * Determines which button was selected
     * @param item Selected menu option
     * @return Selected option
     **************************************************************************/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    /**
     * Return to MainActivity if back arrow is pressed
     **************************************************************************/
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }

    /**
     * Validates input and adds values to database
     * @param view The "Submit" button
     **************************************************************************/
    // First checks that all inputs are filled, then adds the new medication to the database,
    // then ends the activity
    public void onSubmitClick(View view)
    {
        // Check that all fields are fill and contain valid values
        if (!allFieldsFilled())
            return;

        // Prepare data for submission to database
        RadioButton meButton = findViewById(R.id.meButton);
        RadioButton multiplePerDay = findViewById(R.id.multplePerDayButton);
        RadioButton dailyButton = findViewById(R.id.dailyButton);
        EditText patientNameNotMe = findViewById(R.id.patientNameNotMe);
        EditText medicationNameEntry = findViewById(R.id.medNameEnter);
        EditText medicationDosage = findViewById(R.id.medDosageEnter);
        EditText medicationUnits = findViewById(R.id.editTextUnits);
        EditText takenEvery = findViewById(R.id.enterFrequency);
        aliasSwitch = findViewById(R.id.aliasSwitch);
        aliasEnter = findViewById(R.id.aliasEnter);
        ArrayList<String> times = new ArrayList<>();
        String patient;
        String medName;
        String dosage;
        String medUnits;
        String alias = "";
        int frequency = 24 * 60;

        // Convert first dose to String
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String firstDoseDate = simpleDateFormat.format(Calendar.getInstance().getTime());

        // Set patient name
        if (meButton.isChecked())
            patient = meButton.getTag().toString();
        else
            patient = patientNameNotMe.getText().toString();

        medName = medicationNameEntry.getText().toString();
        dosage = medicationDosage.getText().toString();
        medUnits = medicationUnits.getText().toString();

        if (aliasSwitch.isChecked() && !aliasEnter.getText().toString().equals(""))
            alias = aliasEnter.getText().toString();

        // Determine frequency
        // More than once per day
        if (multiplePerDay.isChecked())
        {
            LinearLayout timesOfDay = findViewById(R.id.timesOfTheDay);
            TextView textView;

            for (int i = 0; i < timesOfDay.getChildCount(); i++)
            {
                textView = findViewById(i);
                times.add((String) textView.getTag());
            }
        }
        // Daily
        else if (dailyButton.isChecked())
        {
            TextView timeTaken = findViewById(R.id.timeTaken1);
            times.add((String) timeTaken.getTag());
        }
        // Custom frequency
        else
        {
            int timesPerFrequency = Integer.parseInt(takenEvery.getText().toString());
            Spinner frequencySpinner = findViewById(R.id.frequencySpinner);
            Adapter adapter = frequencySpinner.getAdapter();
            ArrayList<String> spinnerOptions = new ArrayList<>();
            String frequencyUnit = frequencySpinner.getSelectedItem().toString();
            TextView startTime = findViewById(R.id.startTime);
            TextView startDate = findViewById(R.id.startDate);


            // Get spinner values
            for (int i = 0; i < adapter.getCount(); i++)
                spinnerOptions.add((String) adapter.getItem(i));

            // And doses every X hours
            if (frequencyUnit.equals(spinnerOptions.get(0)))
            {
                frequency = timesPerFrequency * 60;
            }
            // Add doses every X Days
            else if (frequencyUnit.equals(spinnerOptions.get(1)))
            {
                frequency = 24 * timesPerFrequency * 60;
            }
            // Add doses every X weeks
            else
            {
                frequency = 7 * timesPerFrequency * 24 * 60;
            }

            LocalDate firstDate;


            if (startTime.getTag() != null)
                times.add((String) startTime.getTag());
            else
                times.add(LocalTime.now().toString());

            try
            {
               firstDate = (LocalDate) startDate.getTag();
            }
            catch(Exception e)
            {
                firstDate = LocalDate.now();
            }

            LocalDateTime startDateTime =
                    LocalDateTime.of(firstDate, LocalTime.parse(times.get(0)));

            firstDoseDate = TimeFormatting.localDateTimeToString(startDateTime);
        }

        // Submit to database, return to MainActivity
        DBHelper helper = new DBHelper(this);

        long rowid = helper.addMedication(medName, patient, dosage, medUnits, firstDoseDate,
                        frequency, alias);

        // Ensure dose was submitted successfully
        if (rowid == -1)
        {
            Toast.makeText(this, "An error Occurred", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < times.size(); i++)
        {
            if (helper.addDose(rowid, times.get(i)) == -1)
            {
                Toast.makeText(this, "An error Occurred", Toast.LENGTH_SHORT).show();
                return;
            }
        }

         //TODO Use AlarmManager to add medication doses to database

        Medication medication = new Medication(medName, patient, medUnits, new LocalDateTime[0],
                stringToLocalDateTime(firstDoseDate), (int) rowid, frequency, Integer.parseInt(dosage), alias);

        if (dailyButton.isChecked())
        {
            NotificationHelper.scheduleNotification(getApplicationContext(), medication,
                    LocalDateTime.of(LocalDate.now(), LocalTime.parse(times.get(0))), medication.getMedId());
        }
        else if (multiplePerDay.isChecked())
        {
            Toast.makeText(this, "Repeating notifications not supported for this scheduling type", Toast.LENGTH_LONG).show();
        }
        else
        {
            NotificationHelper.scheduleNotification(getApplicationContext(), medication, LocalDateTime.of(LocalDate.now(), LocalTime.parse(times.get(0))), medication.getMedId());
        }

        finish();
    }

    /**
     * Ensure all fields are set
     * @return True if all fields are set, else False
     **************************************************************************/
    public boolean allFieldsFilled()
    {
        RadioGroup patientGroup = findViewById(R.id.patientGroup);
        RadioGroup frequencyGroup = findViewById(R.id.frequencyGroup);
        EditText nameEntry = findViewById(R.id.patientNameNotMe);
        EditText medName = findViewById(R.id.medNameEnter);
        EditText dosage = findViewById(R.id.medDosageEnter);
        EditText units = findViewById(R.id.editTextUnits);

        boolean allClear = true;

        RadioButton patientButton = InputValidation.checkRadioGroup(patientGroup);

        // Check which option in patientButtons RadioGroup is selected
        if (patientButton == patientGroup.getChildAt(1))
        {
            if (TextUtils.isEmpty(nameEntry.getText().toString()))
            {
                nameEntry.setError("Please enter a name");
                allClear = false;
            }
            else if (nameEntry.getText().toString().equals("ME!"))
            {
                nameEntry.setError("Name cannot be \"ME!\"");
                allClear = false;
            }
        }
        else if (patientButton != patientGroup.getChildAt(0) && patientButton != patientGroup.getChildAt(1))
            Toast.makeText(this, "Please choose a patient option", Toast.LENGTH_SHORT).show();

        // Ensures medication has a given name
        if (TextUtils.isEmpty(medName.getText().toString()))
        {
            medName.setError("Please enter the medication's name");
            allClear = false;
        }

        // Ensures a dosage is given
        // Ensure dosage is not empty
        if (TextUtils.isEmpty(dosage.getText().toString()))
        {
            dosage.setError("Please enter a dosage");
            allClear = false;
        }
        else
        {
            // Ensure valid entry in dosage
            if (InputValidation.checkEditText(dosage, new char[]{',', '.', ' ', '-'}, "Please enter a positive integer"))
            {
                if (InputValidation.isValidInt(dosage.getText().toString()))
                {
                    if (Integer.parseInt(dosage.getText().toString()) <= 0)
                    {
                        dosage.setError("Enter a number greater than 0");
                        allClear = false;
                    }
                }
                else
                {
                    dosage.setError("Entered number was too large");
                    dosage.setText("");
                    allClear = false;
                }
            }
            else
            {
                allClear = false;
            }
        }

        // Ensures a unit for the dosage
        if (TextUtils.isEmpty(units.getText().toString()))
        {
            units.setError("Please enter a unit (e.g. mg, ml, tablets)");
            allClear = false;
        }


        RadioButton frequencyButton = InputValidation.checkRadioGroup(frequencyGroup);
        EditText takenEvery = findViewById(R.id.enterFrequency);
        TextView timeTaken1 = findViewById(R.id.timeTaken1);

        if (frequencyButton != null)
        {
            // Checks which frequency option is set and validates the given data
            if (frequencyButton == findViewById(R.id.multplePerDayButton))
            {
                EditText timesPerDay = findViewById(R.id.numTimesTaken);
                if (TextUtils.isEmpty(timesPerDay.getText().toString()))
                {
                    timesPerDay.setError("Please enter the number of times per day this medication is taken");
                    allClear = false;
                }
            }
            else if (frequencyButton == findViewById(R.id.dailyButton))
            {
                if (timeTaken1.getText().toString().equals(getString(R.string.atThisTime)))
                {
                    Toast.makeText(this, "Please enter a time", Toast.LENGTH_SHORT).show();
                    allClear = false;
                }
            }
            else if (frequencyButton == findViewById(R.id.customFreqButton))
            {
                if (TextUtils.isEmpty(takenEvery.getText().toString()))
                {
                    takenEvery.setError("Please enter a number");
                    allClear = false;
                }
            }
            else
            {
                Toast.makeText(this, "Please choose a frequency option", Toast.LENGTH_SHORT).show();
                allClear = false;
            }
        }

        // If all fields have been approved, returns true
        return allClear;
    }
}