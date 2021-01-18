package projects.medicationtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

    public void createMedicationViews (ArrayList<Medication> medications)
    {
        LinearLayout scheduleLayout = findViewById(R.id.scheduleLayout);

        for (int i = 0; i < medications.size(); i++)
        {
            LinearLayout eachPatientLayout = new LinearLayout(this);
            eachPatientLayout.setOrientation(LinearLayout.HORIZONTAL);
            eachPatientLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            scheduleLayout.addView(eachPatientLayout);

            TextView textView = new TextView(this);
            textView.setText(medications.get(i).getPatientName());

            eachPatientLayout.addView(textView);
        }
    }

    public int numPatients (ArrayList<Medication> medications)
    {
        HashSet<String> patients = new HashSet<>();

        for (int i = 0; i < medications.size(); i++)
            patients.add(medications.get(i).getPatientName());

        return patients.size();
    }
}