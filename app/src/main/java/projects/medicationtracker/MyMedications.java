package projects.medicationtracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.time.LocalTime;
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
                    createMyMedCards(medication, myMedsLayout);
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
                            createMyMedCards(medication, myMedsLayout);
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

    /**
     * Creates a CardView containing all information on a Medication
     * @param medication The Medication whose details will be displayed.
     * @param baseLayout The LinearLayout in which to place the card
     **************************************************************************/
    private void createMyMedCards(Medication medication, LinearLayout baseLayout)
    {
        StandardCardView thisMedCard = new StandardCardView(this);
        LinearLayout thisMedLayout = new LinearLayout(this);
        LinearLayout buttonLayout = new LinearLayout(this);
        thisMedLayout.setOrientation(LinearLayout.VERTICAL);
        baseLayout.addView(thisMedCard);

        thisMedCard.addView(thisMedLayout);

        // Add name to thisMedLayout
        TextView name = new TextView(this);
        String nameLabel = "Medication name: " + medication.getMedName();
        TextViewUtils.setTextViewParams(name, nameLabel, thisMedLayout);

        // Add Dosage
        TextView doseInfo = new TextView(this);
        String doseInfoLabel = "Dosage: " + medication.getMedDosage() + " " + medication.getMedDosageUnits();
        TextViewUtils.setTextViewParams(doseInfo, doseInfoLabel, thisMedLayout);

        // Add Frequency
        TextView freq = new TextView(this);
        StringBuilder freqLabel;

        if (medication.getMedFrequency() == 1440 && (medication.getTimes().length == 1))
        {
            String time = TimeFormatting.localTimeToString(medication.getTimes()[0].toLocalTime());
            freqLabel = new StringBuilder("Taken daily at: " + time);
        }
        else if (medication.getMedFrequency() == 1440 && (medication.getTimes().length > 1))
        {
            freqLabel = new StringBuilder("Taken daily at: ");

            for (int i = 0; i < medication.getTimes().length; i++)
            {
                LocalTime time = medication.getTimes()[i].toLocalTime();
                freqLabel.append(TimeFormatting.localTimeToString(time));

                if (i != (medication.getTimes().length - 1))
                    freqLabel.append(", ");
            }
        }
        else
            freqLabel = new StringBuilder("Taken every: " + TimeFormatting.freqConversion(medication.getMedFrequency()));

        TextViewUtils.setTextViewParams(freq, freqLabel.toString(), thisMedLayout);

        // Add alias (if exists)
        if (!medication.getAlias().equals(""))
        {
            TextView alias = new TextView(this);
            String aliasLabel = "Alias: " + medication.getAlias();
            TextViewUtils.setTextViewParams(alias, aliasLabel, thisMedLayout);
        }

        // Add start date
        TextView startDate = new TextView(this);
        String startDateLabel = "Taken Since: " + TimeFormatting.localDateToString(medication.getStartDate().toLocalDate());
        TextViewUtils.setTextViewParams(startDate, startDateLabel, thisMedLayout);

        // Add LinearLayout for buttons
        Intent intent = new Intent(this, MedicationNotes.class);
        intent.putExtra("medId", medication.getMedId());

        Button notesButton = new Button(this);
        notesButton.setText("Notes");

        notesButton.setOnClickListener(view ->
        {
            this.finish();
            this.startActivity(intent);
        });

        Intent editMedIntent = new Intent(this, EditMedication.class);
        editMedIntent.putExtra("medId", medication.getMedId());

        Button editMedButton = new Button(this);
        editMedButton.setText("Edit");


        editMedButton.setOnClickListener(view ->
        {
            this.finish();
            this.startActivity(intent);
        });

        thisMedLayout.addView(notesButton);
        thisMedLayout.addView(editMedButton);
    }
}