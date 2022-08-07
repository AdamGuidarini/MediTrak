package projects.medicationtracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
{
    private final DBHelper db = new DBHelper(this);
    private LinearLayout scheduleLayout;
    private LocalDate aDayThisWeek;

    /**
     * Runs at start of activity, builds MainActivity
     *
     * @param savedInstanceState Stored instance of activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aDayThisWeek = LocalDate.now();

        scheduleLayout = findViewById(R.id.scheduleLayout);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Medication Schedule");

        NotificationHelper.createNotificationChannel(this);
        prepareNotifications();

        createMainActivityViews();
    }

    /**
     * Creates option menu
     *
     * @param menu Menu containing selections for user
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Instructions for restarting activity once returned to from other activity
     * Clears all elements and reprints new ones.
     */
    @Override
    protected void onRestart()
    {
        super.onRestart();
        super.recreate();
    }

    /**
    *  Launches MyMedications.java when "My Medications" option is selected
    *
    *  @param item the "My Medications" menu option
    */
    public void onMyMedicationsClick(MenuItem item)
    {
        Intent intent = new Intent(this, MyMedications.class);
        startActivity(intent);
    }

    /**
     *  Launches AddMedication.java when "Add Medication" option is selected
     *
     *  @param item The "Add Medication" option
     */
    public void onAddMedicationClick(MenuItem item)
    {
        Intent intent = new Intent(this, AddMedication.class);
        startActivity(intent);
    }

    /**
     *  Launches Settings.java when "Settings" option is selected
     *
     *  @param item The "Settings" menu option
     */
    public void onSettingsClick(MenuItem item)
    {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    /**
     * Creates CardViews for MainActivity
     */
    public void createMainActivityViews()
    {
        TextView noMeds = findViewById(R.id.noMeds);
        ScrollView scheduleScrollView = findViewById(R.id.scheduleScrollView);
        Spinner patientNames = findViewById(R.id.patientSpinner);

        // Exit if there are no patients in DB
        if (db.numberOfRows() == 0)
        {
            noMeds.setVisibility(View.VISIBLE);
            scheduleScrollView.setVisibility(View.GONE);
            patientNames.setVisibility(View.GONE);
            this.findViewById(R.id.navButtonLayout).setVisibility(View.GONE);
            return;
        }

        ArrayList<Medication> medications = medicationsForThisWeek();
        ArrayList<String> names = db.getPatients();

        // Load contents into spinner, or print results for only patient
        if (db.getPatients().size() == 1)
        {
            patientNames.setVisibility(View.GONE);

            createMedicationSchedule(medications, names.get(0), db);
        }
        else
        {
            patientNames.setVisibility(View.VISIBLE);

            if (names.contains("ME!"))
                names.set(names.indexOf("ME!"), "You");

            ArrayAdapter<String> patientAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    names
            );
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

                    createMedicationSchedule(medications, name, db);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
        }
    }

    /**
     * Creates an ArrayList of Medications to be taken this week
     * @return List of all Medications for this week
     **************************************************************************/
    public ArrayList<Medication> medicationsForThisWeek()
    {
        ArrayList<Medication> medications = db.getMedications();

        // Add times to custom frequency
        LocalDate thisSunday = TimeFormatting.whenIsSunday(aDayThisWeek);

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
                LocalDateTime timeToCheck = medications.get(i).getStartDate();
                ArrayList<LocalDateTime> times = new ArrayList<>();
                long frequency = medications.get(i).getMedFrequency();

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

    /**
     * Creates a schedule for the given patient's medications
     *
     * @param medications An ArrayList of Medications. Will be searched for
     *                    Medications where patientName equals name passed to method.
     * @param name The name of the patient whose Medications should be displayed
     * @param db The database from which to pull data
     */
    public void createMedicationSchedule(ArrayList<Medication> medications, String name, DBHelper db)
    {
        ArrayList<Medication> medicationsForThisPatient = new ArrayList<>();

        for (int i = 0; i < medications.size(); i++)
        {
            if (medications.get(i).getPatientName().equals(name))
                medicationsForThisPatient.add(medications.get(i));
        }

        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        for (int ii = 0; ii < 7; ii++)
            createDayOfWeekCards(days[ii], ii, medicationsForThisPatient, scheduleLayout, db, scheduleLayout.getContext());
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
     */
    public void createDayOfWeekCards (String dayOfWeek, int day, ArrayList<Medication> medications, LinearLayout layout, DBHelper db, Context context)
    {
        CardView thisDayCard = new CardView(context);
        TextView dayLabel = new TextView(context);
        LinearLayout ll = new LinearLayout(context);

        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ll.setLayoutParams(llParams);
        ll.setOrientation(LinearLayout.VERTICAL);

        CardCreator.setCardParams(thisDayCard);

        LocalDate thisSunday = TimeFormatting.whenIsSunday(aDayThisWeek);

        // Add day to top of card
        TextViewUtils.setTextViewFontAndPadding(dayLabel);

        String dayLabelString = dayOfWeek + " " + TimeFormatting.localDateToString(thisSunday.plusDays(day));

        dayLabel.setText(dayLabelString);
        ll.addView(dayLabel);

        // Add medications
        thisDayCard.addView(ll);

        for (Medication medication : medications)
        {
            for (LocalDateTime time : medication.getTimes())
            {
                if (time.toLocalDate().isEqual(thisSunday.plusDays(day)))
                {
                    CheckBox thisMedication = new CheckBox(ll.getContext());
                    long medId = medication.getMedId();
                    Pair<Long, LocalDateTime> tag;

                    // Set Checkbox label
                    String medName = medication.getMedName();
                    String dosage = medication.getMedDosage() + " " + medication.getMedDosageUnits();
                    String dosageTime = TimeFormatting.formatTimeForUser(time.getHour(), time.getMinute());

                    String thisMedicationLabel = medName + " - " + dosage + " - " + dosageTime;
                    thisMedication.setText(thisMedicationLabel);

                    // Check database for this dosage, if not add it
                    // if it is, get the DoseId
                    long rowid = 0;

                    if (!db.isInMedicationTracker(medication, time))
                    {
                        LocalDateTime startDate = medication.getStartDate();
                        if (!time.isBefore(startDate))
                        {
                            rowid = db.addToMedicationTracker(medication, time);

                            if (rowid == -1)
                                Toast.makeText(context, "An error occurred when attempting to write data to database", Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        rowid = db.getDoseId(medId, TimeFormatting.localDateTimeToString(time));
                    }

                    tag = Pair.create(rowid, time);

                    if (rowid > 0)
                    {
                        thisMedication.setTag(tag);

                        if (db.getTaken(rowid))
                            thisMedication.setChecked(true);

                        thisMedication.setOnCheckedChangeListener((compoundButton, b) ->
                        {
                            Pair<Long, LocalDateTime> tvTag = (Pair<Long, LocalDateTime>) thisMedication.getTag();
                            final Long doseId = tvTag.first;
                            int timeBeforeDose = db.getTimeBeforeDose();

                            if (LocalDateTime.now().isBefore(time.minusHours(timeBeforeDose)) && timeBeforeDose != -1)
                            {
                                thisMedication.setChecked(false);
                                Toast.makeText(context,
                                        "Cannot take medications more than "
                                                + timeBeforeDose + " hours in advance",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }


                            String now = TimeFormatting.localDateTimeToString(LocalDateTime.now());
                            db.updateDoseStatus(doseId, now, thisMedication.isChecked());
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

            TextViewUtils.setTextViewParams(textView, noMed, ll);
        }
        else
            sortMedicationCheckBoxes(ll);

        layout.addView(thisDayCard);
    }

    /**
     * Sorts CheckBoxes in medication schedule.
     * @param parentLayout Layout containing CheckBoxes to sort.
     */
    public void sortMedicationCheckBoxes(LinearLayout parentLayout)
    {
        int count = parentLayout.getChildCount();
        short firstCheckboxIndex = 1;

        for (int i = firstCheckboxIndex; i < count; i++)
        {
            for (int j = firstCheckboxIndex + 1; j < (count - i); j++)
            {
                CheckBox child1 = (CheckBox) parentLayout.getChildAt(j - 1);
                CheckBox child2 = (CheckBox) parentLayout.getChildAt(j);

                Pair<Long, LocalDateTime> child1Pair = (Pair<Long, LocalDateTime>) child1.getTag();
                Pair<Long, LocalDateTime> child2Pair = (Pair<Long, LocalDateTime>) child2.getTag();

                LocalDateTime child1Time = child1Pair.second;
                LocalDateTime child2Time = child2Pair.second;

                if (child1Time != null && child1Time.isAfter(child2Time))
                {
                    CheckBox temp = new CheckBox(parentLayout.getContext());
                    temp.setText(child1.getText());
                    temp.setTag(child1.getTag());

                    child1.setText(child2.getText());
                    child1.setTag(child2.getTag());

                    child2.setText(temp.getText());
                    child2.setTag(temp.getTag());
                }
            }
        }
    }

    /**
     * Prepares pending intents for notifications, useful if app is force stopped
     */
    private void prepareNotifications()
    {
        ArrayList<Medication> medications = db.getMedications();

        for (Medication medication : medications)
        {
            long[] medicationTimeIds = db.getMedicationTimeIds(medication);

            if (medication.getMedFrequency() == 1440)
            {
                NotificationHelper.deletePendingNotification(medication.getMedId(), this);

            }
            else
            {
                for (int i = 0; i < medicationTimeIds.length; i++)
                {
                    NotificationHelper.deletePendingNotification(medicationTimeIds[i], this);
                }
            }
        }

        for (Medication medication : medications)
        {
            long[] medicationTimeIds = db.getMedicationTimeIds(medication);

            if (medication.getMedFrequency() == 1440)
            {
                NotificationHelper.scheduleNotification(
                        getApplicationContext(),
                        medication,
                        LocalDateTime.of(LocalDate.now(), medication.getTimes()[0].toLocalTime()),
                        medication.getMedId()
                );
            }
            else
            {
                for (int i = 0; i < medicationTimeIds.length; i++)
                {
                    NotificationHelper.scheduleNotification(
                            getApplicationContext(),
                            medication,
                            LocalDateTime.of(LocalDate.now(), medication.getTimes()[i].toLocalTime()),
                            medicationTimeIds[i] * -1
                    );
                }
            }
        }
    }

    public void onLeftClick(View view)
    {
        aDayThisWeek = aDayThisWeek.minusWeeks(1);

        scheduleLayout.removeAllViews();

        createMainActivityViews();
    }

    public void onTodayClick(View view)
    {
        aDayThisWeek = LocalDate.now();

        scheduleLayout.removeAllViews();

        createMainActivityViews();
    }

    public void onRightClick(View view)
    {
        aDayThisWeek = aDayThisWeek.plusWeeks(1);

        scheduleLayout.removeAllViews();

        createMainActivityViews();
    }
}