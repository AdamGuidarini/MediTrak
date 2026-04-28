package projects.medicationtracker;

import static projects.medicationtracker.Helpers.DBHelper.DATE_FORMAT;
import static projects.medicationtracker.Helpers.DBHelper.TIME_FORMAT;
import static projects.medicationtracker.MainActivity.preferences;
import static projects.medicationtracker.MediTrak.formatter;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import kotlin.Pair;
import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NativeDbHelper;
import projects.medicationtracker.Models.Medication;
import projects.medicationtracker.Views.StandardCardView;

public class MyMedications extends BaseActivity {
    public static final String MEDICATION_ID_ARG = "MediTrakCore/MedicationId";
    NativeDbHelper db;
    DBHelper javaDb;

    private MaterialButton activeButton;
    private MaterialButton inactiveButton;
    private LinearLayout activeLayout;
    private LinearLayout inactiveLayout;
    private ScrollView scrollMyMeds;
    private TextView noMeds;

    /**
     * Creates MyMedications
     *
     * @param savedInstanceState Saved instances
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_medications);

        db = new NativeDbHelper(this);
        javaDb = new DBHelper(this);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.my_medications));

        String you = getString(R.string.you);
        ArrayList<Medication> allMeds = db.getAllMedications();

        noMeds = findViewById(R.id.noMyMeds);
        scrollMyMeds = findViewById(R.id.scrollMyMeds);

        if (allMeds.isEmpty())
            return;
        else {
            noMeds.setVisibility(GONE);
            scrollMyMeds.setVisibility(VISIBLE);
        }

        final TextInputLayout namesLayout = findViewById(R.id.names_layout);
        final MaterialAutoCompleteTextView namesSelector = findViewById(R.id.nameSpinner);
        final LinearLayout activeToggleLayout = findViewById(R.id.active_toggle_layout);

        activeLayout = findViewById(R.id.med_layout);
        inactiveLayout = findViewById(R.id.med_layout_inactive);

        activeButton = findViewById(R.id.active_button);
        inactiveButton = findViewById(R.id.inactive_button);

        List<String> patientNames = allMeds.stream()
                .map(Medication::getPatientName)
                .distinct()
                .collect(Collectors.toList());

        ArrayList<Pair<String, ArrayList<Medication>>> patientMedPairs = new ArrayList<>();

        for (String patient : patientNames) {
            ArrayList<Medication> meds = allMeds.stream()
                    .filter(m -> m.getPatientName().equals(patient) && m.getChild() == null)
                    .collect(Collectors.toCollection(ArrayList::new));

            if (!meds.isEmpty()) {
                patientMedPairs.add(new Pair<>(patient, meds));
            }
        }

        if (patientMedPairs.size() == 1) {
            populateViews(allMeds);
        } else if (patientMedPairs.size() > 1) {
            String[] patients = patientMedPairs.stream().map(Pair::getFirst).map(p ->
                    Objects.equals(p, "ME!") ? getString(R.string.you) : p).toArray(String[]::new
            );

            if (patientMedPairs.stream().allMatch(m -> m.getFirst().equals("ME!"))) {
                patientMedPairs = patientMedPairs.stream().map(m -> {
                    if (m.getFirst().equals("ME!")) {
                        m = new Pair<>(you, m.getSecond());
                    }

                    return m;
                }).collect(Collectors.toCollection(ArrayList::new));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, patients);
            namesSelector.setAdapter(adapter);

            namesLayout.setVisibility(VISIBLE);

            final ArrayList<Pair<String, ArrayList<Medication>>> allMedsClone = (ArrayList<Pair<String, ArrayList<Medication>>>) patientMedPairs.clone();

            namesSelector.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    activeLayout.removeAllViews();
                    inactiveLayout.removeAllViews();
                    activeToggleLayout.setVisibility(GONE);

                    String selected = s.toString();
                    final String patient = selected.equals(you) ? "ME!" : selected;

                    ArrayList<Medication> patientMeds = allMedsClone.stream()
                            .filter(m -> m.getFirst().equals(patient))
                            .map(Pair::getSecond)
                            .collect(Collectors.toList())
                            .stream()
                            .findFirst()
                            .orElse(null);

                    if (patientMeds == null) {
                        return;
                    }

                    populateViews(patientMeds);

                    namesSelector.clearFocus();

                    if (inactiveLayout.getChildCount() > 0) {
                        activeToggleLayout.setVisibility(VISIBLE);
                    }

                    setActive(activeLayout.getChildCount() > 0);
                }
            });

            if (Arrays.asList(patients).contains(you)) {
                namesSelector.setText(you, false);
            } else {
                namesSelector.setText(adapter.getItem(0), false);
            }
        }

        activeButton.setOnClickListener(
                (view) -> {
                    setActive(true);
                    activeLayout.setVisibility(VISIBLE);
                    inactiveLayout.setVisibility(GONE);
                }
        );

        inactiveButton.setOnClickListener(
                (view) -> {
                    setActive(false);
                    activeLayout.setVisibility(GONE);
                    inactiveLayout.setVisibility(VISIBLE);
                }
        );

        setActive(true);

        if (inactiveLayout.getChildCount() > 0) {
            activeToggleLayout.setVisibility(VISIBLE);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPressed();
            }
        });
    }

    /**
     * Determines which button was selected
     *
     * @param item Selected menu option
     * @return Selected option
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Return to MainActivity if back arrow is pressed
     */
    public void handleBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    /**
     * Creates a CardView containing all information on a Medication
     *
     * @param medication The Medication whose details will be displayed.
     * @param baseLayout The LinearLayout in which to place the card
     */
    private void createMyMedCard(Medication medication, LinearLayout baseLayout) {
        StandardCardView thisMedCard = new StandardCardView(this);
        View thisMedLayout = LayoutInflater.from(this).inflate(
                R.layout.fragment_my_medications,
                thisMedCard,
                false
        );

        baseLayout.addView(thisMedCard);
        thisMedCard.addView(thisMedLayout);
        bindMedicationCard(thisMedLayout, medication);
    }

    private void bindMedicationCard(View cardView, Medication medication) {
        LocalTime[] times = javaDb.getMedicationTimes(medication.getId());
        LocalDateTime[] dateTimes = new LocalDateTime[times.length];

        LinearLayout barrier = cardView.findViewById(R.id.barrier);
        LinearLayout barrier1 = cardView.findViewById(R.id.barrier1);
        LinearLayout barrier2 = cardView.findViewById(R.id.barrier2);
        LinearLayout barrier3 = cardView.findViewById(R.id.barrier3);
        LinearLayout barrier4 = cardView.findViewById(R.id.barrier4);

        MaterialTextView name = cardView.findViewById(R.id.myMedCardMedicationName);
        MaterialTextView dosage = cardView.findViewById(R.id.dosage_amount);
        MaterialTextView doseUnit = cardView.findViewById(R.id.dosage_unit);
        MaterialTextView alias = cardView.findViewById(R.id.myMedCardAlias);
        MaterialTextView frequency = cardView.findViewById(R.id.myMedCardFrequency);
        MaterialTextView remainingDose = cardView.findViewById(R.id.remainingDoses);
        MaterialTextView takenSince = cardView.findViewById(R.id.myMedCardTakenSince);
        MaterialTextView endDate = cardView.findViewById(R.id.endDate);
        MaterialTextView instructions = cardView.findViewById(R.id.instructions);
        MaterialButton notesButton = cardView.findViewById(R.id.myMedsNotes);
        MaterialButton editButton = cardView.findViewById(R.id.myMedsEdit);
        MaterialButton historyButton = cardView.findViewById(R.id.history_button);

        for (int i = 0; i < times.length; i++) {
            dateTimes[i] = LocalDateTime.of(medication.getStartDate().toLocalDate(), times[i]);
        }

        medication.setTimes(dateTimes);

        name.setText(medication.getName());
        dosage.setText(formatter.format(medication.getDosage()));
        doseUnit.setText(medication.getDosageUnits());

        String label = medication.generateFrequencyLabel(
                this,
                preferences.getString(DATE_FORMAT),
                preferences.getString(TIME_FORMAT)
        );
        frequency.setText(label);

        String doseLimit = medication.getRemainingDosesCount() > -1
                ? String.valueOf(medication.getRemainingDosesCount())
                : "N/A";
        remainingDose.setText(doseLimit);

        alias.setText(medication.getAlias().isEmpty() ? "N/A" : medication.getAlias());

        LocalDateTime start = medication.getParent() == null
                ? medication.getStartDate()
                : medication.getParent().getStartDate();
        String beginning = DateTimeFormatter.ofPattern(
                preferences.getString(DATE_FORMAT),
                Locale.getDefault()
        ).format(start);
        takenSince.setText(beginning);

        LocalDateTime end = medication.getEndDate();
        if (end == null || end.toLocalDate().isEqual(LocalDate.of(9999, 12, 31))) {
            endDate.setText("N/A");
        } else {
            String endSt = DateTimeFormatter.ofPattern(
                    preferences.getString(DATE_FORMAT),
                    Locale.getDefault()
            ).format(end);
            endDate.setText(endSt);
        }

        if (medication.getInstructions() == null || medication.getInstructions().isEmpty()) {
            instructions.setText("N/A");
        } else {
            instructions.setText(medication.getInstructions());
        }

        notesButton.setOnClickListener(view -> {
            Intent notesIntent = new Intent(this, MedicationNotes.class);
            notesIntent.putExtra("medId", medication.getId());
            finish();
            startActivity(notesIntent);
        });

        editButton.setOnClickListener(view -> {
            Intent editMedIntent = new Intent(this, AddMedication.class);
            editMedIntent.putExtra("medId", medication.getId());
            finish();
            startActivity(editMedIntent);
        });

        historyButton.setOnClickListener(view -> {
            Intent historyIntent = new Intent(this, MedicationHistory.class);
            historyIntent.putExtra("ID", medication.getId());
            finish();
            startActivity(historyIntent);
        });

        int separatorColor = name.getCurrentTextColor();
        barrier.setBackgroundColor(separatorColor);
        barrier1.setBackgroundColor(separatorColor);
        barrier2.setBackgroundColor(separatorColor);
        barrier3.setBackgroundColor(separatorColor);
        barrier4.setBackgroundColor(separatorColor);
    }

    private void setActive(boolean isActive) {
        int primaryColor = MaterialColors.getColor(
                this, androidx.appcompat.R.attr.colorPrimary, Color.BLACK
        );
        int onPrimaryColor = MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorOnPrimary, Color.WHITE
        );

        MaterialButton selected = isActive ? activeButton : inactiveButton;
        selected.setStrokeWidth(0);
        selected.setBackgroundTintList(ColorStateList.valueOf(primaryColor));
        selected.setTextColor(onPrimaryColor);

        MaterialButton unselected = isActive ? inactiveButton : activeButton;
        unselected.setStrokeWidth(3);
        unselected.setStrokeColor(ColorStateList.valueOf(primaryColor));
        unselected.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        unselected.setTextColor(primaryColor);

        if (isActive && activeLayout.getChildCount() == 0 && inactiveLayout.getChildCount() > 0) {
            scrollMyMeds.setVisibility(GONE);
            noMeds.setVisibility(VISIBLE);
        } else {
            scrollMyMeds.setVisibility(VISIBLE);
            noMeds.setVisibility(GONE);
        }
    }

    private void populateViews(ArrayList<Medication> patientMeds) {
        patientMeds.stream()
                .filter(med -> med.getChild() == null)
                .forEach(med -> createMyMedCard(
                        med,
                        med.isActive() ? activeLayout : inactiveLayout
                ));
    }
}