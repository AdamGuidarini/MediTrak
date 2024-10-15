package projects.medicationtracker;

import static projects.medicationtracker.Fragments.MedicationScheduleFragment.DAY_IN_CURRENT_WEEK;
import static projects.medicationtracker.Fragments.MedicationScheduleFragment.DAY_NUMBER;
import static projects.medicationtracker.Fragments.MedicationScheduleFragment.DAY_OF_WEEK;
import static projects.medicationtracker.Fragments.MedicationScheduleFragment.MEDICATIONS;
import static projects.medicationtracker.Helpers.DBHelper.AGREED_TO_TERMS;
import static projects.medicationtracker.Helpers.DBHelper.DARK;
import static projects.medicationtracker.Helpers.DBHelper.LIGHT;
import static projects.medicationtracker.Helpers.DBHelper.SEEN_NOTIFICATION_REQUEST;
import static projects.medicationtracker.Helpers.DBHelper.THEME;
import static projects.medicationtracker.MediTrak.DATABASE_PATH;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentContainerView;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import projects.medicationtracker.Dialogs.OpenNotificationsDialog;
import projects.medicationtracker.Dialogs.WelcomeDialog;
import projects.medicationtracker.Fragments.MedicationScheduleFragment;
import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NativeDbHelper;
import projects.medicationtracker.Helpers.NotificationHelper;
import projects.medicationtracker.Helpers.TimeFormatting;
import projects.medicationtracker.Interfaces.IDialogCloseListener;
import projects.medicationtracker.Models.Medication;
import projects.medicationtracker.Models.Notification;
import projects.medicationtracker.Views.StandardCardView;

public class MainActivity extends AppCompatActivity implements IDialogCloseListener {
    public static Bundle preferences;
    private final DBHelper db = new DBHelper(this);
    private LinearLayout scheduleLayout;
    private LocalDate aDayThisWeek;
    private NativeDbHelper nativeDb;
    private TextInputLayout namesLayout;
    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> db.seenPermissionRequest(SEEN_NOTIFICATION_REQUEST)
    );
    private ArrayList<Medication> allMeds;

    /**
     * Runs at start of activity, builds MainActivity
     *
     * @param savedInstanceState Stored instance of activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NotificationManager manager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE
        );
        StatusBarNotification[] openNotifications = manager.getActiveNotifications();

        DATABASE_PATH = getDatabasePath(DBHelper.DATABASE_NAME).getAbsolutePath();

        nativeDb = new NativeDbHelper(DATABASE_PATH);
        nativeDb.create();

        allMeds = db.getMedications();

        preferences = db.getPreferences();

        String theme = preferences.getString(THEME);

        switch (Objects.requireNonNull(theme)) {
            case DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        aDayThisWeek = LocalDate.now();
        scheduleLayout = findViewById(R.id.scheduleLayout);

        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.med_schedule));

        NotificationHelper.createNotificationChannel(this);
        prepareNotifications();

        if (!preferences.getBoolean(AGREED_TO_TERMS)) {
            WelcomeDialog welcomeDialog = new WelcomeDialog();
            welcomeDialog.setCancelable(false);
            welcomeDialog.show(getSupportFragmentManager(), null);
        }

        if (Build.VERSION.SDK_INT >= 33 && !preferences.getBoolean(SEEN_NOTIFICATION_REQUEST)
            && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        }

        namesLayout = findViewById(R.id.names_layout_main);

        createMainActivityViews();
    }

    @Override
    public void onResume() {
        super.onResume();

        NotificationManager manager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE
        );
        StatusBarNotification[] openNotifications = manager.getActiveNotifications();

        scheduleLayout.removeAllViews();
        createMainActivityViews();

        if (openNotifications.length > 0) {
            OpenNotificationsDialog notificationsDialog = new OpenNotificationsDialog(
                    openNotifications, allMeds
            );
            notificationsDialog.show(getSupportFragmentManager(), null);
        }
    }

    /**
     * Creates option menu
     *
     * @param menu Menu containing selections for user
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Launches MyMedications.java when "My Medications" option is selected
     *
     * @param item the "My Medications" menu option
     */
    public void onMyMedicationsClick(MenuItem item) {
        Intent intent = new Intent(this, MyMedications.class);
        startActivity(intent);
    }

    /**
     * Launches AddMedication.java when "Add Medication" option is selected
     *
     * @param item The "Add Medication" option
     */
    public void onAddMedicationClick(MenuItem item) {
        Intent intent = new Intent(this, AddMedication.class);
        startActivity(intent);
    }

    /**
     * Launches Settings.java when "Settings" option is selected
     *
     * @param item The "Settings" menu option
     */
    public void onSettingsClick(MenuItem item) {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    /**
     * Creates CardViews for MainActivity
     */
    public void createMainActivityViews() {
        TextView noMeds = findViewById(R.id.noMeds);
        ScrollView scheduleScrollView = findViewById(R.id.scheduleScrollView);
        MaterialAutoCompleteTextView patientNames = findViewById(R.id.patientSpinner);
        final String you = getString(R.string.you);

        // Exit if there are no patients in DB
        if (db.numberOfRows() == 0) {
            noMeds.setVisibility(View.VISIBLE);
            scheduleScrollView.setVisibility(View.GONE);
            namesLayout.setVisibility(View.GONE);
            this.findViewById(R.id.navButtonLayout).setVisibility(View.GONE);
            return;
        }

        ArrayList<Medication> medications = medicationsForThisWeek();
        ArrayList<String> names = db.getPatients();

        // Load contents into spinner, or print results for only patient
        if (db.getPatients().size() == 1) {
            namesLayout.setVisibility(View.GONE);

            createMedicationSchedule(medications, names.get(0));
        } else {
            patientNames.setVisibility(View.VISIBLE);

            if (names.contains("ME!")) {
                names.set(names.indexOf("ME!"), you);
            }

            ArrayAdapter<String> patientAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    names
            );
            patientNames.setAdapter(patientAdapter);

            patientNames.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    scheduleLayout.removeAllViews();

                    String name = s.toString();

                    if (name.equals(you)) {
                        name = "ME!";
                    }

                    createMedicationSchedule(medications, name);
                    patientNames.clearFocus();
                }
            });

            if (names.contains(you)) {
                patientNames.setText(you, false);
            } else {
                patientNames.setText(names.get((0)), false);
            }
        }
    }

    /**
     * Creates an ArrayList of Medications to be taken this week
     *
     * @return List of all Medications for this week
     */
    public ArrayList<Medication> medicationsForThisWeek() {
        ArrayList<LocalDateTime> validTimes;
        ArrayList<Medication> medications = db.getMedications();
        // Add times to custom frequency
        LocalDate thisSunday = TimeFormatting.whenIsSunday(aDayThisWeek);

        // Look at each medication
        for (int i = 0; i < medications.size(); i++) {
            LocalDateTime[] timeArr;
            ArrayList<Pair<LocalDateTime, LocalDateTime>> pausedIntervals =
                    db.getPauseResumePeriods(medications.get(i));

            // Skip as needed meds
            if (medications.get(i).getFrequency() == 0) {
                medications.get(i).setTimes(db.getMedicationDoses(medications.get(i)));

                continue;
            }

            // If a medication is taken once per day
            if (medications.get(i).getTimes().length == 1 && medications.get(i).getFrequency() == 1440) {
                // if the Medication is taken once per day just add the start of each date to
                timeArr = new LocalDateTime[7];
                LocalTime localtime = medications.get(i).getTimes()[0].toLocalTime();

                for (int j = 0; j < 7; j++)
                    timeArr[j] =
                            LocalDateTime.of(LocalDate.from(thisSunday.plusDays(j)), localtime);
            }
            // If a medication is taken multiple times per day
            else if (medications.get(i).getTimes().length > 1 && medications.get(i).getFrequency() == 1440) {
                int numberOfTimes = medications.get(i).getTimes().length;
                int index = 0;

                timeArr = new LocalDateTime[numberOfTimes * 7];
                LocalTime[] drugTimes = new LocalTime[numberOfTimes];

                for (int j = 0; j < numberOfTimes; j++)
                    drugTimes[j] = medications.get(i).getTimes()[j].toLocalTime();

                for (int j = 0; j < 7; j++) {
                    for (int y = 0; y < numberOfTimes; y++) {
                        timeArr[index] =
                                LocalDateTime.of(
                                        LocalDate.from(thisSunday.plusDays(j)), drugTimes[y]
                                );
                        index++;
                    }
                }
            }
            // If a medication has a custom frequency, take its start date and calculate times for
            // for this week
            else {
                LocalDateTime timeToCheck = medications.get(i).getStartDate();
                ArrayList<LocalDateTime> times = new ArrayList<>();
                long frequency = medications.get(i).getFrequency();

                while (timeToCheck.toLocalDate().isBefore(thisSunday))
                    timeToCheck = timeToCheck.plusMinutes(frequency);

                while (timeToCheck.toLocalDate().isBefore(thisSunday.plusDays(7))) {
                    times.add(timeToCheck);
                    timeToCheck = timeToCheck.plusMinutes(frequency);
                }

                timeArr = new LocalDateTime[times.size()];

                for (int j = 0; j < times.size(); j++)
                    timeArr[j] = times.get(j);

            }

            validTimes = new ArrayList<>(Arrays.asList(timeArr));

            validTimes.removeIf(
                    (time) ->
                    {
                        for (Pair<LocalDateTime, LocalDateTime> pausedInterval : pausedIntervals) {
                            if (pausedInterval.first == null) {
                                if (time.isBefore(pausedInterval.second)) {
                                    return true;
                                }
                            } else if (time.isAfter(pausedInterval.first) && pausedInterval.second == null) {
                                return true;
                            } else if (time.isAfter(pausedInterval.first) && time.isBefore(pausedInterval.second)) {
                                return true;
                            }
                        }

                        return false;
                    }
            );

            timeArr = new LocalDateTime[validTimes.size()];
            timeArr = validTimes.toArray(timeArr);

            medications.get(i).setTimes(timeArr);
        }

        return medications;
    }

    /**
     * Creates a schedule for the given patient's medications
     *
     * @param medications An ArrayList of Medications. Will be searched for
     *                    Medications where patientName equals name passed to method.
     * @param name        The name of the patient whose Medications should be displayed
     */
    public void createMedicationSchedule(ArrayList<Medication> medications, String name) {
        ArrayList<Medication> medicationsForThisPatient = new ArrayList<>();

        for (int i = 0; i < medications.size(); i++) {
            if (medications.get(i).getPatientName().equals(name))
                medicationsForThisPatient.add(medications.get(i));
        }

        String[] days = {
                getString(R.string.sunday),
                getString(R.string.monday),
                getString(R.string.tuesday),
                getString(R.string.wednesday),
                getString(R.string.thursday),
                getString(R.string.friday),
                getString(R.string.saturday)
        };

        for (int ii = 0; ii < 7; ii++) {
            createDayOfWeekCards(
                    days[ii],
                    ii,
                    medicationsForThisPatient,
                    scheduleLayout
            );
        }
    }

    /**
     * Creates a CardView for each day of the week containing information
     * on the medications to be taken that day
     *
     * @param dayOfWeek   String for the name of the day
     * @param day         The number representing the day of the week
     *                    - Sunday = 0
     *                    - Monday = 1
     *                    - Tuesday = 2
     *                    - Wednesday = 3
     *                    - Thursday = 4
     *                    - Friday = 5
     *                    - Saturday = 6
     * @param medications The list of medications to be taken on the given day
     * @param layout      The LinearLayout in which to place the CardView
     */
    public void createDayOfWeekCards(
            String dayOfWeek,
            int day,
            ArrayList<Medication> medications,
            LinearLayout layout
    ) {
        StandardCardView thisDayCard = new StandardCardView(this);
        FragmentContainerView fragmentContainer = new FragmentContainerView(this);
        int viewId = day == 0 ? 7 : day;

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(MEDICATIONS, medications);
        bundle.putString(DAY_OF_WEEK + "_" + viewId, dayOfWeek);
        bundle.putLong(DAY_IN_CURRENT_WEEK + "_" + viewId, aDayThisWeek.toEpochDay());
        bundle.putInt(DAY_NUMBER + "_" + viewId, day);

        thisDayCard.addView(fragmentContainer);

        fragmentContainer.setId(day == 0 ? 7 : day);
        layout.addView(thisDayCard);

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(viewId, MedicationScheduleFragment.class, bundle)
                .commit();
    }

    /**
     * Prepares pending intents for notifications, useful if app is force stopped
     * Clears all open notifications as well
     */
    private void prepareNotifications() {
        final ArrayList<Notification> notifications = nativeDb.getNotifications();

        for (Medication medication : allMeds) {
            NotificationHelper.clearPendingNotifications(medication, this);
        }

        for (Medication medication : allMeds) {
            NotificationHelper.createNotifications(medication, this);
        }

        for (final Notification n : notifications) {
            Medication med = allMeds.stream().filter(m -> m.getId() == n.getMedId()).findFirst().orElse(null);

            if (med == null) {
                Log.e("EventReceiver", "Failed to create notification for Medication: " + n.getMedId());

                continue;
            }

            NotificationHelper.scheduleNotification(this, med, n.getDoseTime(), n.getNotificationId());
        }
    }

    /**
     * Navigates one week back from the currently viewed week
     */
    public void onLeftClick(View view) {
        aDayThisWeek = aDayThisWeek.minusWeeks(1);

        scheduleLayout.removeAllViews();

        createMainActivityViews();
    }

    /**
     * Navigates to the current week
     */
    public void onTodayClick(View view) {
        aDayThisWeek = LocalDate.now();

        scheduleLayout.removeAllViews();

        createMainActivityViews();
    }

    /**
     * Navigates one week forward from the currently viewed week
     */
    public void onRightClick(View view) {
        aDayThisWeek = aDayThisWeek.plusWeeks(1);

        scheduleLayout.removeAllViews();

        createMainActivityViews();
    }

    @Override
    public void handleDialogClose(Action action, Object data) {
        if (action == Action.EDIT) {
            scheduleLayout.removeAllViews();
            createMainActivityViews();
        }
    }
}