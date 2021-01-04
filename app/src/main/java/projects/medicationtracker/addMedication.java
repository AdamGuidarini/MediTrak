package projects.medicationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;

public class addMedication extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    Spinner timeSpinner;
    EditText takenEvery;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        timeSpinner = findViewById(R.id.timeSpinner);
        takenEvery = findViewById(R.id.medFrequencyEnter);

        ArrayList<String> timeUnits = new ArrayList<>();
        timeUnits.add("Hour(s)");
        timeUnits.add("Day(s)");
        timeUnits.add("Week(s)");
        timeUnits.add("Month(s)");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeUnits);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(arrayAdapter);
        timeSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        EditText editText = findViewById(R.id.medFrequencyEnter);
        LinearLayout linearLayout = findViewById(R.id.linearLayout);

        if (!editText.getText().toString().equals(""))
        {
            int editTextValue = Integer.parseInt(editText.getText().toString());
            switch (i)
            {
                case 0:
                    for (int j = 0; j < editTextValue; j++)
                    {
                        EditText timeToMedicate = new EditText(this);
                        timeToMedicate.setInputType(InputType.TYPE_CLASS_DATETIME);
                        linearLayout.addView(timeToMedicate);
                    }
                    break;
                case 1:
                    Toast.makeText(this, "Days", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(this, "Weeks", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
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
}