package projects.medicationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

public class MyMedications extends AppCompatActivity
{
    DBHelper db = new DBHelper(this);

    /**
     * Creates MyMedications
     * @param savedInstanceState Saved instances
     **************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_medications);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Objects.requireNonNull(getSupportActionBar()).setTitle("My Medications");

        Spinner nameSpinner = findViewById(R.id.nameSpinner);
        ArrayList<String> patientNames = db.getPatients();

        if (patientNames.size() >= 1)
        {
            ArrayList<Medication> patientMeds;

            if (patientNames.size() == 1)
            {
                patientMeds = db.getMedications();
            }
            else
            {
                if (patientNames.contains("ME!"))
                        patientNames.set(patientNames.indexOf("ME!"), "You");

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, patientNames);
                nameSpinner.setAdapter(adapter);

                nameSpinner.setVisibility(View.VISIBLE);

                if (patientNames.contains("You"))
                        nameSpinner.setSelection(adapter.getPosition("You"));

                nameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
                    {
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {}
                });

            }
        }
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
}