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
import android.widget.Toast;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import static java.time.temporal.TemporalAdjusters.previous;
import static java.util.Calendar.SUNDAY;

public class MainActivity extends AppCompatActivity
{
    final private DBHelper db = new DBHelper(this);
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;

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

    public void onSettingsClick(MenuItem item)
    {
    }

    // Creates a ScrollView for each patient, a Linear layout for each day, and CheckBoxes for each
    // Medication
    public void createMedicationViews (ArrayList<Medication> medications)
    {
        LinearLayout scheduleLayout = findViewById(R.id.scheduleLayout);

        ArrayList<String> patients = new ArrayList<>();
        for (int i = 0; i < PatientUtils.numPatients(medications); i++)
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
                patientName.setText(R.string.yourMedications);
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
                    if (time.toLocalDate().isEqual(thisSunday.plusDays(day - 1)))
                    {
                        CheckBox thisMedication = new CheckBox(ll.getContext());
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        int medId = medications.get(i).getMedId();

                        // Set Checkbox label
                        String medName = medications.get(i).getMedName();
                        String dosage = medications.get(i).getMedDosage() + " " + medications.get(i).getMedDosageUnits();
                        String dosageTime = TimeFormatting.formatTime(time.getHour(), time.getMinute());

                        String thisMedicationLabel = medName + " - " + dosage + "\n" + dosageTime;
                        thisMedication.setText(thisMedicationLabel);

                        // Check database for this dosage, if not add it
                        // if it is, get the DoseId
                        int rowid;

                        if (!db.isInMedicationTracker(medications.get(i), time))
                        {
                            rowid = db.addToMedicationTracker(medications.get(i), time);
                            if ( rowid == -1)
                                Toast.makeText(this,"An error occurred when attempting to write data to database", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            rowid = db.getDoseId(medId, time.format(formatter));
                        }

                        thisMedication.setTag(rowid);

                        if (db.getTaken(rowid))
                            thisMedication.setChecked(true);

                        thisMedication.setOnCheckedChangeListener((compoundButton, b) ->
                        {
                            final int doseId = Integer.parseInt(thisMedication.getTag().toString());

                            if (LocalDateTime.now().isBefore(time.minusHours(2)))
                            {
                                thisMedication.setChecked(false);
                                Toast.makeText(this, "Cannot take medications more than 2 hours in advance", Toast.LENGTH_SHORT).show();
                                return;
                            }


                            String now = LocalDateTime.now().format(formatter);
                            db.updateMedicationStatus(doseId, now, thisMedication.isChecked());
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
}