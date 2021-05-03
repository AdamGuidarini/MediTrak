package projects.medicationtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import static java.time.temporal.TemporalAdjusters.previous;
import static java.util.Calendar.SUNDAY;

public class MainActivity extends AppCompatActivity
{
    private final DBHelper db = new DBHelper(this);

    /**
     * Runs at start of activity, builds MainActivity
     *
     * @param savedInstanceState Stored instance of activity
     **************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Medication Schedule");

        LinearLayout scheduleLayout = findViewById(R.id.scheduleLayout);
        TextView noMeds = findViewById(R.id.noMeds);
        ScrollView scheduleScrollView = findViewById(R.id.scheduleScrollView);
        Spinner patientNames = findViewById(R.id.patientSpinner);

        if (db.numberOfRows() == 0)
        {
            noMeds.setVisibility(View.VISIBLE);
            scheduleScrollView.setVisibility(View.GONE);
            return;
        }

        ArrayList<Medication> medications = PatientUtils.medicationsForThisWeek(db);

        // Load contents into spinner
        if (PatientUtils.numPatients(medications) <= 1)
            patientNames.setVisibility(View.GONE);
        else
        {
            patientNames.setVisibility(View.VISIBLE);

            ArrayList<String> names = PatientUtils.getPatientNames(medications);
            ArrayAdapter<String> patientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
            patientNames.setAdapter(patientAdapter);

            // Select "You" by default
            if (names.contains("You"))
            {
                for (int i = 0; i < names.size(); i++)
                {
                    if (names.get(i).equals("You"))
                        patientNames.setSelection(i);
                }
            }

            patientNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
                {
                    scheduleLayout.removeAllViews();

                    String name = adapterView.getSelectedItem().toString();

                    if (name.equals("You"))
                        name = "ME!";

                    createMedicationSchedule(medications, name);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
        }
    }

    /**
     * Creates option menu
     *
     * @param menu Menu containing selections for user
     **************************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Instructions for restarting activity once returned to from other activity
     **************************************************************************/
    @Override
    protected void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    /**
    *  Launches MyMedications.java when "My Medications" option is selected
    *
    *  @param item the "My Medications" menu option
    **************************************************************************/
    public void onMyMedicationsClick(MenuItem item)
    {
    }

    /**
     *  Launches AddMedication.java when "Add Medication" option is selected
     *
     *  @param item The "Add Medication" option
     **************************************************************************/
    public void onAddMedicationClick(MenuItem item)
    {
        Intent intent = new Intent(this, AddMedication.class);
        startActivity(intent);
    }

    /**
     *  Launches Settings.java when "Settings" option is selected
     *
     *  @param item The "Settings" menu option
     **************************************************************************/
    public void onSettingsClick(MenuItem item)
    {
    }

    /**
     * Creates a schedule for the given patient's medications
     *
     * @param medications An ArrayList of Medications. Will be searched for
     *        Medications where patientName equals name passed to method.
     *
     * @param name The name of the patient whose Medications should be displayed
    **************************************************************************/
    public void createMedicationSchedule (ArrayList<Medication> medications, String name)
    {
        LinearLayout scheduleLayout = findViewById(R.id.scheduleLayout);

        ArrayList<Medication> medicationsForThisPatient = new ArrayList<>();

        for (int i = 0; i < medications.size(); i++)
        {
            if (medications.get(i).getPatientName().equals(name))
                medicationsForThisPatient.add(medications.get(i));
        }

        String[] days = {" Sunday", " Monday", " Tuesday", " Wednesday", " Thursday", " Friday", " Saturday"};

        for (int ii = 0; ii < 7; ii++)
            createDayOfWeekCards(days[ii], ii, medicationsForThisPatient, scheduleLayout);
    }

    /**
     * Creates a CardView for each day of the week containing information
     * on the medications to be taken that day
     *
     * @param dayOfWeek The day of the week represented by the CardView.
     * @param day The number representing the day of the week
     *            - Sunday = 0
     *            - Monday = 1
     *            - Tuesday = 2
     *            - Wednesday = 3
     *            - Thursday = 4
     *            - Friday = 5
     *            - Saturday = 6
     * @param medications The list of medications to be taken on the given day
     * @param layout The LinearLayout in which to place the CardView
     **************************************************************************/
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
}