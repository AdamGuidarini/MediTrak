package projects.medicationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    TextView hiddenTextView;
    Spinner frequencySpinner;
    DBHelper dbHelper;

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
        hiddenTextView = findViewById(R.id.hiddenTextView);
        customFrequencyLayout = findViewById(R.id.customFrequencyLayout);
        aliasEnter = findViewById(R.id.aliasEnter);
        frequencySpinner = findViewById(R.id.frequencySpinner);
        aliasSwitch = findViewById(R.id.aliasSwitch);
        dbHelper = new DBHelper(this);

        String[] spinnerFrequencies = {"Hour(s)", "Day(s)", "week(s)"};
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerFrequencies);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(frequencyAdapter);

        // Add array list of all patient names
        // they will then be displayed in the input field
        AutoCompleteTextView nameInput = findViewById(R.id.patientNameNotMe);
        ArrayList<String> patientNames = dbHelper.getPatients();
        ArrayAdapter<String> patientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, patientNames);
        nameInput.setAdapter(patientAdapter);
        nameInput.setThreshold(1);

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
                case R.id.meButton:
                    if (nameInput.getVisibility() == View.VISIBLE)
                        nameInput.setVisibility(View.GONE);
                    break;
                case R.id.otherButton:
                    nameInput.setVisibility(View.VISIBLE);
                    break;
            }
        });

        aliasSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if (aliasEnter.getVisibility() == View.GONE)
                    aliasEnter.setVisibility(View.VISIBLE);
                else
                {
                    aliasEnter.setVisibility(View.GONE);
                    aliasEnter.setText("");
                }
            }
        });

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
                        TextView textView = new TextView(timesOfTheDay.getContext());
                        textView.setHint("Tap to set time");
                        textView.setId(ii);
                        textView.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
                        // set layout params
                        timesOfTheDay.addView(textView);
                        textView.setOnClickListener(view ->
                        {
                            DialogFragment dialogFragment = new TimePickerFragment();
                            dialogFragment.show(getSupportFragmentManager(), null);
                            delayer(textView, hiddenTextView);
                        });
                    }
                }
            }
        });

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
                    timeTaken1.setOnClickListener(view ->
                    {
                        DialogFragment dialogFragment = new TimePickerFragment();
                        dialogFragment.show(getSupportFragmentManager(), null);
                        delayer(timeTaken1, hiddenTextView);
                    });
                    break;
                case R.id.customFreqButton:
                    timeTaken1.setVisibility(View.GONE);
                    numTimesTaken.setVisibility(View.GONE);
                    timesOfTheDay.removeAllViews();
                    customFrequencyLayout.setVisibility(View.VISIBLE);

                    TextView[] textViews = new TextView[2];
                    textViews[0] = findViewById(R.id.startDate);
                    textViews[1] = findViewById(R.id.startTime);

                    getCurrentTimeAndDate(textViews[0], textViews[1]);

                    textViews[0].setOnClickListener(view ->
                    {
                        DialogFragment dialogFragment = new SelectDateFragment();
                        dialogFragment.show(getSupportFragmentManager(), null);
                    });

                    textViews[1].setOnClickListener(view ->
                    {
                        DialogFragment dialogFragment = new TimePickerFragment();
                        dialogFragment.show(getSupportFragmentManager(), null);
                        delayer(textViews[1], hiddenTextView);
                    });
                    break;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }

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
        String alias = new String();
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
                int[] timeArr = (int[]) textView.getTag();
                times.add(formatTimeForDB(timeArr[0], timeArr[1]));
            }
        }
        // Daily
        else if (dailyButton.isChecked())
        {
            TextView timeTaken = findViewById(R.id.timeTaken1);
            int[] timeArr = (int[]) timeTaken.getTag();
            times.add(formatTimeForDB(timeArr[0], timeArr[1]));
        }
        // Custom frequency
        else
        {
            TextView startDate = findViewById(R.id.startDate);
            TextView startTime = findViewById(R.id.startTime);
            int[] hourMin = (int[]) startTime.getTag();
            int timesPerFrequency = Integer.parseInt(takenEvery.getText().toString());
            Spinner frequencySpinner = findViewById(R.id.frequencySpinner);
            Adapter adapter = frequencySpinner.getAdapter();
            ArrayList<String> spinnerOptions = new ArrayList<>();
            String frequencyUnit = frequencySpinner.getSelectedItem().toString();
            String startDatetime = startDate.getText().toString() + " " + formatTime(hourMin[0], hourMin[1]);

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
        }

        // Submit to database, return to MainActivity
        DBHelper helper = new DBHelper(this);

        long rowid = helper.addMedication(medName, patient, dosage, medUnits, firstDoseDate, frequency, alias);
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

        finish();
    }

    // Set text views to current time and date
    public void getCurrentTimeAndDate(TextView date, TextView time)
    {
        String[] dateForUser = new String[2];
        String dateTime;

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateForDb = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        Date myDate = new Date();
        dateForUser[0] = dateFormat.format(myDate);
        dateForUser[1] = dateFormat1.format(myDate);
        dateTime = dateForDb.format(myDate);

        date.setTag(dateTime);
        date.setText(dateForUser[0]);
        time.setText(dateForUser[1]);
    }

    // Set format time from TextView's tag to UTC
    public String formatTime (int hours, int minutes)
    {
        String dateTime;
        Date today = new Date();
        LocalDate localDate = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();

        dateTime = year + "/" + month + "/" + day + " ";

        if (hours < 10)
            dateTime += "0" + hours + ":";
        else
            dateTime += hours + ":";

        if (minutes < 10)
            dateTime += "0" + minutes + ":00";
        else
            dateTime += minutes + ":00";

        return dateTime;
    }

    public String formatTimeForDB(int hour, int minute)
    {
        String time;

        if (hour < 10)
            time = "0" + hour;
        else
            time = String.valueOf(hour);

        if (minute < 10)
            time += ":0" + minute;
        else
            time += ":" + minute;

        return time + ":00";
    }

    // Check to ensure all necessary views are set
    public boolean allFieldsFilled()
    {
        int trueCount = 0;
        RadioButton[] patientButtons = new RadioButton[2];
        RadioButton[] frequencyButtons = new RadioButton[3];
        EditText nameEntry = findViewById(R.id.patientNameNotMe);
        EditText medName = findViewById(R.id.medNameEnter);
        EditText dosage = findViewById(R.id.medDosageEnter);
        EditText units = findViewById(R.id.editTextUnits);

        patientButtons[0] = findViewById(R.id.meButton);
        patientButtons[1] = findViewById(R.id.otherButton);

        if (patientButtons[0].isChecked())
            trueCount++;
        else if (patientButtons[1].isChecked())
        {
            if (TextUtils.isEmpty(nameEntry.getText().toString()))
                nameEntry.setError("Please enter a name");
            else if (nameEntry.getText().toString().equals("ME!"))
                nameEntry.setError("Name cannot be \"ME!\"");
            else
                trueCount++;
        }
        else
            Toast.makeText(this, "Please choose a patient option", Toast.LENGTH_SHORT).show();

        if (TextUtils.isEmpty(medName.getText().toString()))
            medName.setError("Please enter the medication's name");
        else
            trueCount++;

        if (TextUtils.isEmpty(dosage.getText().toString()))
            dosage.setError("Please enter a dosage");
        else if (Integer.parseInt(dosage.getText().toString()) <= 0)
        {
            dosage.setError("Enter a number greater than 0");
        }
        else
            trueCount++;

        if (TextUtils.isEmpty(units.getText().toString()))
            units.setError("Please enter a unit (e.g. mg, ml, tablets)");
        else
            trueCount++;

        frequencyButtons[0] = findViewById(R.id.multplePerDayButton);
        frequencyButtons[1] = findViewById(R.id.dailyButton);
        frequencyButtons[2] = findViewById(R.id.customFreqButton);
        EditText takenEvery = findViewById(R.id.enterFrequency);
        TextView timeTaken1 = findViewById(R.id.timeTaken1);

        if (frequencyButtons[0].isChecked())
        {
            EditText timesPerDay = findViewById(R.id.numTimesTaken);
            if (TextUtils.isEmpty(timesPerDay.getText().toString()))
                timesPerDay.setError("Please enter the number of times per day this medication is taken");
            else
                trueCount++;
        }
        else if (frequencyButtons[1].isChecked())
        {
            if (timeTaken1.getText().toString().equals(getString(R.string.atThisTime)))
                Toast.makeText(this, "Please enter a time", Toast.LENGTH_SHORT).show();
            else
                trueCount++;
        }
        else if (frequencyButtons[2].isChecked())
        {
            if (TextUtils.isEmpty(takenEvery.getText().toString()))
                takenEvery.setError("Please enter a number");
            else
                trueCount++;
        }
        else
            Toast.makeText(this, "Please choose a frequency option", Toast.LENGTH_SHORT).show();

        return trueCount == 5;
    }

    public void delayer (TextView clickedText, TextView hiddenTextView)
    {
        clickedText.postDelayed(() ->
        {
            clickedText.setText(hiddenTextView.getText().toString());
            clickedText.setTag(hiddenTextView.getTag());
        }, 5000);
    }
}