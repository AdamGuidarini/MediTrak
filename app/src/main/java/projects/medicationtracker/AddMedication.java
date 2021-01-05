package projects.medicationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AddMedication extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    RadioGroup patientGroup;
    RadioGroup frequencyGroup;
    LinearLayout linearLayout;
    Spinner timeSpinner;
    EditText takenEvery;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Medication");

        timeSpinner = findViewById(R.id.timeSpinner);
        takenEvery = findViewById(R.id.medFrequencyEnter);
        patientGroup = findViewById(R.id.patientGroup);
        frequencyGroup = findViewById(R.id.frequencyGroup);
        linearLayout = findViewById(R.id.frequencyLayout);


        // Add array list of all patient names
        AutoCompleteTextView nameInput = findViewById(R.id.patientNameNotMe);

        takenEvery.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){}
            @Override
            public void afterTextChanged(Editable editable)
            {
//                linearLayout.removeAllViews();
//
//                if (!takenEvery.getText().toString().equals(""))
//                {
//                    for (int i = 0; i < Integer.parseInt(takenEvery.getText().toString()); i++)
//                    {
//                        TextView textView = new TextView(linearLayout.getContext());
//                        textView.setId(i);
//                        textView.setText("Taken at: ");
//                        linearLayout.addView(textView);
//                        textView.setOnClickListener(new View.OnClickListener()
//                        {
//                            @Override
//                            public void onClick(View view)
//                            {
//                                textView.setText("Hello");
////                                DialogFragment dialogFragment = new TimePickerFragment();
////                                dialogFragment.show(getSupportFragmentManager(), "timePicker");
//                            }
//                        });
//                    }
//                }
            }
        });

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

        frequencyGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i)
            {
                TextView takenDaily = findViewById(R.id.takenDaily);

                switch (radioGroup.findViewById(i).getId())
                {
                    case R.id.multplePerDayButton:
                        takenDaily.setVisibility(View.GONE);
                        break;
                    case R.id.dailyButton:
                        takenDaily.setVisibility(View.VISIBLE);
                        takenDaily.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                DialogFragment dialogFragment = new TimePickerFragment();
                                dialogFragment.show(getSupportFragmentManager(), "timePicker");
                            }
                        });
                        break;
                    case R.id.weeklyButton:
                        break;
                    case R.id.monthlyButton:
                        break;
                    case R.id.customFreqButton:
                        break;
                }
            }
        });

        ArrayList<String> timeUnits = new ArrayList<>();
        timeUnits.add("Day");
        timeUnits.add("Week");
        timeUnits.add("Month");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeUnits);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(arrayAdapter);
        timeSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        EditText editText = findViewById(R.id.medFrequencyEnter);
        LinearLayout linearLayout = findViewById(R.id.timeLayout);

        if (!editText.getText().toString().equals(""))
        {
            int editTextValue = Integer.parseInt(editText.getText().toString());
            switch (i)
            {
                case 0:
                    break;
                case 1:
                    Toast.makeText(this, "Weeks", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(this, "Months", Toast.LENGTH_SHORT).show();
                    break;
                default:
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {
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
        // Submit to database, return to MainActivity
    }
}