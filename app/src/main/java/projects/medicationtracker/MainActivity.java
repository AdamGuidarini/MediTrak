package projects.medicationtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;


import static java.time.temporal.TemporalAdjusters.previous;
import static java.util.Calendar.SUNDAY;

public class MainActivity extends AppCompatActivity
{
    final private DBHelper db = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Medication Schedule");

        TextView noMeds = findViewById(R.id.noMeds);
        ScrollView scheduleScrollView = findViewById(R.id.scheduleScrollView);

        if (db.numberOfRows() == 0)
        {
            noMeds.setVisibility(View.VISIBLE);
            scheduleScrollView.setVisibility(View.GONE);
            return;
        }

        ArrayList<Medication> medications = medicationsForThisWeek();
        createMedicationViews(medications);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    public void onMyMedicationsClick(MenuItem item)
    {
    }

    public void onAddMedicationClick(MenuItem item)
    {
        Intent intent = new Intent(this, AddMedication.class);
        startActivity(intent);
    }

    public void onEditMedicationClick(MenuItem item)
    {
    }

    public void onSettingsClick(MenuItem item)
    {
    }

    // Creates a list of Medications to be taken in the current week
    public ArrayList<Medication> medicationsForThisWeek()
    {
        ArrayList<Medication> medications = db.getMedications();

        // Add times to custom frequency
        LocalDateTime thisSunday = LocalDateTime.now();
        thisSunday = thisSunday.with(previous(DayOfWeek.of(SUNDAY)));
        LocalDateTime timeToCheck;

        ArrayList<LocalDateTime> times = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Look at each medication
        for (int i = 0; i < medications.size(); i++)
        {
            // If there are no listed times, calculate them with Medication.StartDate and frequency
            timeToCheck = LocalDateTime.parse(medications.get(i).getStartDate(), formatter);

            if (Arrays.equals(medications.get(i).getTimes(), new LocalDateTime[0]))
            {
                int frequency = medications.get(i).getMedFrequency();

                while (timeToCheck.isBefore(thisSunday.plusDays(7)) && timeToCheck.isAfter(thisSunday))
                {
                    if (timeToCheck.isAfter(thisSunday))
                        times.add(timeToCheck);

                    timeToCheck = timeToCheck.plusMinutes(frequency);
                }

                LocalDateTime[] timeArr = new LocalDateTime[times.size()];
                for (int ii = 0; ii < times.size(); ii++)
                    timeArr[ii] = times.get(i);

                medications.get(i).setTimes(timeArr);
            }
        }

        return medications;
    }

    // Creates a ScrollView for each patient, a Linear layout for each day, and CheckBoxes for each
    // Medication
    public void createMedicationViews (ArrayList<Medication> medications)
    {
        LinearLayout scheduleLayout = findViewById(R.id.scheduleLayout);

        ArrayList<String> patients = new ArrayList<>();
        for (int i = 0; i < numPatients(medications); i++)
        {
            if (!patients.contains(medications.get(i).getPatientName()))
                patients.add(medications.get(i).getPatientName());
        }

        // Create Views for Each Patient
        for (int i = 0; i < patients.size(); i++)
        {
            HorizontalScrollView patientView = new HorizontalScrollView(this);

            LinearLayout eachPatientLayout = new LinearLayout(this);
            eachPatientLayout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, 30);
            eachPatientLayout.setLayoutParams(layoutParams);

            LinearLayout.LayoutParams patientNameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            patientNameParams.setMarginStart(20);

            TextView textView = new TextView(this);
            textView.setLayoutParams(patientNameParams);

            if (patients.get(i).equals("ME!"))
                textView.setText("Your Medications:");
            else
                textView.setText(patients.get(i) + "'s Medications:");

            scheduleLayout.addView(patientView);
            patientView.addView(eachPatientLayout);
            eachPatientLayout.addView(textView);

            // Put each patient's Medications in their linear layout
            for (int j = 0; j < medications.size(); j++)
            {
                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params1.setMarginStart(50);

                if (patients.get(i).equals(medications.get(j).getPatientName()))
                {
                    CheckBox medName = new CheckBox(this);
                    medName.setLayoutParams(params1);

                    String medString;

                    if (medications.get(j).getAlias().isEmpty())
                        medString = medications.get(j).getMedName();
                    else
                        medString = medications.get(j).getAlias();

                    medString += " - " + medications.get(j).getMedDosage() + " " + medications.get(j).getMedDosageUnits();

                    medName.setText(medString);
                    eachPatientLayout.addView(medName);
                }
            }
        }
    }

    public void medicationsForGivenDay (int dayOfWeek, ArrayList<Medication> medications)
    {

    }

    // Returns number of patients in database
    public int numPatients (ArrayList<Medication> medications)
    {
        HashSet<String> patients = new HashSet<>();

        // Iterate through each Medication instance and patientName to the HashSet
        for (int i = 0; i < medications.size(); i++)
            patients.add(medications.get(i).getPatientName());

        return patients.size();
    }
}