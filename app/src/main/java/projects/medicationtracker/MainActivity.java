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
import android.widget.CompoundButton;
import android.widget.FrameLayout;
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


import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
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
        LocalDateTime thisSunday = LocalDateTime.now().with(previous(DayOfWeek.of(SUNDAY)));
        LocalDateTime timeToCheck;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Look at each medication
        for (int i = 0; i < medications.size(); i++)
        {
            ArrayList<LocalDateTime> times = new ArrayList<>();

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
                for (int ii = 0; ii < timeArr.length; ii++)
                    timeArr[ii] = times.get(ii);


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

            // Split thisPatientMedications into an array of ArrayLists based on day of week
            ArrayList<Medication>[] weekOfMedsForThisPatient = splitMedsByDay(thisPatientsMedications);

            String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

            // Create CardViews
            for (int j = 0; j < weekOfMedsForThisPatient.length; j++)
                createDayOfWeekCards(days[j], thisPatientsMedications, horizontalLayout);
        }
    }

    // Create a CardView for the given day of the week
    public void createDayOfWeekCards (String dayOfWeek, ArrayList<Medication> medications, LinearLayout layout)
    {
        CardView thisDayCard = new CardView(layout.getContext());
        TextView dayLabel = new TextView(thisDayCard.getContext());
        LinearLayout ll = new LinearLayout(thisDayCard.getContext());

        LinearLayout.LayoutParams  llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ll.setLayoutParams(llParams);
        ll.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        thisDayCard.setLayoutParams(layoutParams);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) thisDayCard.getLayoutParams();
        marginLayoutParams.setMargins( 15, 40, 15, 40);
        thisDayCard.requestLayout();



        // Add day to top of card
        dayLabel.setText(dayOfWeek);
        ll.addView(dayLabel);

        // Add medications
        thisDayCard.addView(ll);
        for (int i = 0; i < medications.size(); i++)
        {
            CheckBox thisMedication = new CheckBox(ll.getContext());
            String medName = medications.get(i).getMedName();
            String dosage = medications.get(i).getMedDosage() + " " + medications.get(i).getMedDosageUnits();
            String thisMedicationLabel = medName + "\n" + dosage;

            thisMedication.setText(thisMedicationLabel);
            thisMedication.setOnCheckedChangeListener((compoundButton, b) ->
            {
                // TODO find a way to efficiently add data to MedicationTacker table
                // TODO create method to change status of medication in MedicationTracker table
            });

            ll.addView(thisMedication);
        }

        layout.addView(thisDayCard);
    }

    // Creates an array of ArrayLists that splits each medication
    public ArrayList<Medication>[] splitMedsByDay (ArrayList<Medication> medicationsForThisPatient)
    {
        ArrayList<Medication>[] medsByDay = new ArrayList[7];

        // Get Sunday of current week
        final LocalDateTime sunday = LocalDateTime.now().with(previous(DayOfWeek.of(SUNDAY)));

        int i = 0;
        for (LocalDateTime time = sunday; time.isBefore(sunday.plusDays(7)); time = time.plusHours(24))
        {
            for (int j = 0; j < medicationsForThisPatient.size(); j++)
            {
                Medication medication = medicationsForThisPatient.get(j);

                for (int x = 0; x < medicationsForThisPatient.get(j).getTimes().length; x++)
                {
                    LocalDateTime drugTime = medicationsForThisPatient.get(j).getTimes()[x];
                    ArrayList<LocalDateTime> timeArrayList = new ArrayList<>();

                    if (drugTime != null)
                    {
                        if (drugTime.toLocalDate().equals("0000-00-00"))
                        {
                            timeArrayList.add(LocalDateTime.of(time.toLocalDate(), drugTime.toLocalTime()));
                        } else if (drugTime.isAfter(time) && !drugTime.isBefore(time.plusHours(24)))
                        {
                            timeArrayList.add(drugTime);

                            LocalDateTime[] timeArray = new LocalDateTime[timeArrayList.size()];
                            for (int y = 0; y < timeArrayList.size(); y++)
                                timeArray[y] = timeArrayList.get(y);

                            Medication thisMedForThisDay = new Medication(medication.getMedName(),
                                    medication.getPatientName(), medication.getMedDosageUnits(),
                                    timeArray, medication.getStartDate(),
                                    medication.getMedId(), medication.getMedFrequency(),
                                    medication.getMedDosage(), medication.getAlias());
                            try
                            {
                                medsByDay[i].add(thisMedForThisDay);
                            }
                            catch (java.lang.NullPointerException e)
                            {
                                e.getCause();
                            }
                        }
                    }
                }
            }
            i++;
        }
        return medsByDay;
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