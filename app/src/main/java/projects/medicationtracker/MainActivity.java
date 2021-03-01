package projects.medicationtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

import static java.time.temporal.TemporalAdjusters.previous;
import static java.util.Calendar.PM;
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

        // Displays date and name of each medication to be printed
        // For debugging purposes only, will be disabled upon completion
        boolean bool = false;
        if (bool)
        {
            LinearLayout linearLayout = findViewById(R.id.scheduleLayout);
            for (int i = 0; i < medications.size(); i++)
            {
                for (int j = 0; j < medications.get(i).getTimes().length; j++)
                {
                    TextView textView = new TextView(this);
                    textView.setText(medications.get(i).getMedName() + " " + medications.get(i).getTimes()[j]);
                    linearLayout.addView(textView);
                }
            }
        }
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

    public void onSettingsClick(MenuItem item)
    {
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
            TextView patientName = new TextView(this);
            HorizontalScrollView sv = new HorizontalScrollView(this);
            ScrollView.LayoutParams lp = new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout horizontalLayout = new LinearLayout(this);

            horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

            sv.setLayoutParams(lp);

            if (patients.get(i).equals("ME!"))
                patientName.setText("Your Medications:");
            else
                patientName.setText(patients.get(i) + "'s Medications:");

            scheduleLayout.addView(patientName);
            scheduleLayout.addView(patientView);
            scheduleLayout.addView(sv);
            sv.addView(horizontalLayout);

            // Create list of medications for only this patient
            ArrayList<Medication> thisPatientsMedications = new ArrayList<>();
            for (int j = 0; j < medications.size(); j++)
            {
                if (medications.get(j).getPatientName().equals(patients.get(i)))
                    thisPatientsMedications.add(medications.get(j));
            }

            String[] days = {" Sunday", " Monday", " Tuesday", " Wednesday", " Thursday", " Friday", " Saturday"};

            // Create CardViews
            for (int j = 0; j < 7; j++)
            {
                //ArrayList<Medication> medsForDay = medicationsForToday(thisPatientsMedications, j);
                createDayOfWeekCards(days[j], j, thisPatientsMedications, horizontalLayout);
            }
        }
    }

    // Create a CardView for the given day of the week
    public void createDayOfWeekCards (String dayOfWeek, int day, ArrayList<Medication> medications, LinearLayout layout)
    {
        CardView thisDayCard = new CardView(layout.getContext());
        TextView dayLabel = new TextView(thisDayCard.getContext());
        LinearLayout ll = new LinearLayout(thisDayCard.getContext());

        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ll.setLayoutParams(llParams);
        ll.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        thisDayCard.setLayoutParams(layoutParams);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) thisDayCard.getLayoutParams();
        marginLayoutParams.setMargins(15, 40, 15, 40);
        thisDayCard.requestLayout();

        // Add day to top of card
        dayLabel.setText(dayOfWeek);
        ll.addView(dayLabel);

        // Add medications
        thisDayCard.addView(ll);

        LocalDate thisSunday = LocalDate.now().with(previous(DayOfWeek.of(SUNDAY)));

        if (medications != null)
        {
            for (int i = 0; i < medications.size(); i++)
            {
                for (LocalDateTime time : medications.get(i).getTimes())
                {
                    if (time.toLocalDate().isEqual(thisSunday.plusDays(day + 6)))
                    {
                        CheckBox thisMedication = new CheckBox(ll.getContext());
                        String medName = medications.get(i).getMedName();
                        String dosage = medications.get(i).getMedDosage() + " " + medications.get(i).getMedDosageUnits();
                        String dosageTime = time.getHour() + ":";

                        int dosageHour = time.getHour();
                        if (dosageHour < 10)
                            dosageTime += "0";

                        dosageTime += dosageHour;

                        String thisMedicationLabel = medName + " - " + dosage + "\n" + "At: " + dosageTime;

                        thisMedication.setText(thisMedicationLabel);
                        thisMedication.setOnCheckedChangeListener((compoundButton, b) ->
                        {
                            // TODO find a way to efficiently add data to MedicationTacker table
                            // TODO create method to change status of medication in MedicationTracker table
                        });

                        ll.addView(thisMedication);
                    }
                }
            }
        }

        if (ll.getChildCount() == 1)
        {
            TextView textView = new TextView(thisDayCard.getContext());
            String noMed = "No medications for " + dayOfWeek;

            textView.setText(noMed);
            ll.addView(textView);
        }

        layout.addView(thisDayCard);
    }

    // Creates a list of Medications to be taken in the current week
    public ArrayList<Medication> medicationsForThisWeek()
    {
        ArrayList<Medication> medications = db.getMedications();

        // Add times to custom frequency
        LocalDate thisSunday = LocalDate.now().with(previous(DayOfWeek.SUNDAY));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Look at each medication
        for (int i = 0; i < medications.size(); i++)
        {
            LocalDateTime[] timeArr;

            // If a medication is taken once per day
            if (medications.get(i).getTimes().length == 1 && medications.get(i).getMedFrequency() == 1440)
            {
                // if the Medication is taken once per day just add the start of each date to
                timeArr = new LocalDateTime[7];
                LocalTime localtime = medications.get(i).getTimes()[0].toLocalTime();

                for (int j = 0; j < 7; j++)
                    timeArr[j] = LocalDateTime.of(LocalDate.from(thisSunday.plusDays(j)), localtime);

                medications.get(i).setTimes(timeArr);
            }
            // If a medication is taken multiple times per day
            else if (medications.get(i).getTimes().length > 1 && medications.get(i).getMedFrequency() == 1440)
            {
                int numberOfTimes = medications.get(i).getTimes().length;
                int index = 0;

                timeArr = new LocalDateTime[numberOfTimes * 7];
                LocalTime[] drugTimes = new LocalTime[numberOfTimes];

                for (int j = 0; j < numberOfTimes; j++)
                    drugTimes[j] = medications.get(i).getTimes()[j].toLocalTime();

                for (int j = 0; j < 7; j++)
                {
                    for (int y = 0; y < numberOfTimes; y++)
                    {
                        timeArr[index] = LocalDateTime.of(LocalDate.from(thisSunday.plusDays(j)), drugTimes[y]);
                        index++;
                    }
                }

                medications.get(i).setTimes(timeArr);
            }
            // If a medication has a custom frequency, take its start date and calculate times for
            // for this week
            else
            {
                LocalDateTime timeToCheck = LocalDateTime.parse(medications.get(i).getStartDate(), formatter);
                ArrayList<LocalDateTime> times = new ArrayList<>();
                int frequency = medications.get(i).getMedFrequency();

                while (timeToCheck.toLocalDate().isBefore(thisSunday))
                    timeToCheck = timeToCheck.plusMinutes(frequency);

                while (timeToCheck.toLocalDate().isBefore(thisSunday.plusDays(7)))
                {
                    times.add(timeToCheck);
                    timeToCheck = timeToCheck.plusMinutes(frequency);
                }

                timeArr = new LocalDateTime[times.size()];

                for (int j = 0; j < times.size(); j++)
                    timeArr[j] = times.get(j);

                medications.get(i).setTimes(timeArr);
            }
        }

        return medications;
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