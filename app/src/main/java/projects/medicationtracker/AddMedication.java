package projects.medicationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.Time;
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
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class AddMedication extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    RadioGroup patientGroup;
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
                    for (int j = 0; j < editTextValue; j++)
                    {
                        TextView textView = new TextView(this);
                        textView.setText("Time goes here");
                        linearLayout.addView(textView);
                        textView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                DialogFragment dialogFragment = new TimePickerFragment();
                                dialogFragment.show(getSupportFragmentManager(), "timePicker");
                            }
                        });
                    }
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
    }
}