package projects.medicationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AddMedication<volitile> extends AppCompatActivity
{
    RadioGroup patientGroup;
    RadioGroup frequencyGroup;
    LinearLayout linearLayout;
    LinearLayout timeLayout;
    LinearLayout timesOfTheDay;
    LinearLayout customFrequencyLayout;
    EditText numTimesTaken;
    TextView hiddenTextView;
    Spinner frequencySpinner;

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
        frequencySpinner = findViewById(R.id.frequencySpinner);

        String[] spinnerFrequencies = {"Hour(s)", "Day(s)", "week(s)"};
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerFrequencies);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(frequencyAdapter);

        // Add array list of all patient names
        // they will then be displayed in the input field
        AutoCompleteTextView nameInput = findViewById(R.id.patientNameNotMe);


        patientGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i)
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
                        textView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                DialogFragment dialogFragment = new TimePickerFragment();
                                dialogFragment.show(getSupportFragmentManager(), null);
                                textView.setTag(hiddenTextView.getTag());
                                textView.setText(hiddenTextView.getText());
                            }
                        });
                    }
                }
            }
        });

        frequencyGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i)
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
                        timeTaken1.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                DialogFragment dialogFragment = new TimePickerFragment();
                                dialogFragment.show(getSupportFragmentManager(), null);
                                timeTaken1.setTag(hiddenTextView.getTag());
                                timeTaken1.setText(hiddenTextView.getText().toString());
                            }
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

                        textViews[0].setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                DialogFragment dialogFragment = new SelectDateFragment();
                                dialogFragment.show(getSupportFragmentManager(), null);
                            }
                        });

                        textViews[1].setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                DialogFragment dialogFragment = new TimePickerFragment();
                                dialogFragment.show(getSupportFragmentManager(), null);
                                textViews[1].setTag(hiddenTextView.getTag());
                                textViews[1].setText(hiddenTextView.getText().toString());
                            }
                        });
                        break;
                }
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

        // Submit to database, return to MainActivity
    }


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

    public boolean allFieldsFilled()
    {
        int trueCount = 0;
        RadioButton[] patientButtons = new RadioButton[2];
        RadioButton[] frequencyButtons = new RadioButton[3];
        EditText nameEntry = findViewById(R.id.patientNameNotMe);
        EditText dosage = findViewById(R.id.medDosageEnter);
        EditText units = findViewById(R.id.editTextUnits);

        patientButtons[0] = findViewById(R.id.meButton);
        patientButtons[1] = findViewById(R.id.otherButton);

        if (patientButtons[0].isSelected())
            trueCount++;
        else if (patientButtons[1].isSelected())
        {
            if (TextUtils.isEmpty(nameEntry.getText().toString()))
                nameEntry.setError("Please enter a name");
            else if (!nameEntry.getText().toString().equals("ME!"))
                nameEntry.setError("Name cannot be \"ME!\"");
            else
                trueCount++;
        }

        if (TextUtils.isEmpty(dosage.getText().toString()) && Integer.parseInt(dosage.getText().toString()) > 0)
            dosage.setError("Please enter a dosage");
        else
            trueCount++;

        if (TextUtils.isEmpty(units.getText().toString()))
            units.setError("Please enter a unit (e.g. mg, ml, tablets)");
        else
            trueCount++;

        frequencyButtons[0] = findViewById(R.id.multplePerDayButton);
        frequencyButtons[1] = findViewById(R.id.dailyButton);
        frequencyButtons[2] = findViewById(R.id.customFreqButton);

        if (frequencyButtons[0].isSelected())
        {
            EditText timesPerDay = findViewById(R.id.numTimesTaken);
            if (TextUtils.isEmpty(timesPerDay.toString()))
                timesPerDay.setError("Please enter the number of times per day this medication is taken");
        }
        else if (frequencyButtons[1].isSelected())
        {

        }
        else if (frequencyButtons[2].isSelected())
        {

        }

        return trueCount == 5;
    }
}