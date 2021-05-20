package projects.medicationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

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

        if (db.numberOfRows() == 0)
            return;
        else
        {
            TextView noMeds = findViewById(R.id.noMyMeds);
            noMeds.setVisibility(View.GONE);
            ScrollView scrollMyMeds = findViewById(R.id.scrollMyMeds);
            scrollMyMeds.setVisibility(View.VISIBLE);
        }

        final Spinner nameSpinner = findViewById(R.id.nameSpinner);
        final LinearLayout myMedsLayout = findViewById(R.id.medLayout);

        ArrayList<String> patientNames = db.getPatients();

        if (patientNames.size() >= 1)
        {
            if (patientNames.size() == 1)
            {
                ArrayList<Medication> patientMeds = db.getMedications();

                for (Medication medication : patientMeds)
                    CardCreator.createMyMedCards(medication, myMedsLayout);

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
                        myMedsLayout.removeAllViews();

                        String patient = adapterView.getSelectedItem().toString();

                        if (patient.equals("You"))
                            patient = "ME!";

                        ArrayList<Medication> patientMeds = db.getMedicationsForPatient(patient);

                        for (Medication medication : patientMeds)
                            CardCreator.createMyMedCards(medication, myMedsLayout);
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
        {
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Return to MainActivity if back arrow is pressed
     **************************************************************************/
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }
}