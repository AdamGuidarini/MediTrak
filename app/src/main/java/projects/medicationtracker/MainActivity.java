package projects.medicationtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static java.time.temporal.TemporalAdjusters.previous;
import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.SUNDAY;
import static java.util.Calendar.WEEK_OF_YEAR;

public class MainActivity extends AppCompatActivity
{
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Medication Schedule");

        db = new DBHelper(this);

        TextView noMeds = findViewById(R.id.noMeds);
        ScrollView scheduleScrollView = findViewById(R.id.scheduleScrollView);

        if (db.numberOfRows() == 0)
        {
            noMeds.setVisibility(View.VISIBLE);
            scheduleScrollView.setVisibility(View.GONE);
        }

        ArrayList<Medication> medications = medicationsForThisWeek();
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

        // Look at each medication
        for (int i = 0; i < medications.size(); i++)
        {

            // If there are no listed times, calculate them with Medication.StartDate
            timeToCheck = LocalDateTime.parse(medications.get(i).getStartDate());

            if (Arrays.equals(medications.get(i).getTimes(), new LocalDateTime[0]))
            {
                int frequency = medications.get(i).getMedFrequency();

                while (timeToCheck.isBefore(thisSunday.plusDays(7)))
                {
                    if (timeToCheck.isAfter(thisSunday))
                        times.add(timeToCheck);

                    timeToCheck.plusMinutes(frequency);
                }
                medications.get(i).setTimes((LocalDateTime[]) times.toArray());
            }
        }

        return medications;
    }
}