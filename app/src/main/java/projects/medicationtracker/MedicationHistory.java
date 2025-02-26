package projects.medicationtracker;

import static android.view.View.GONE;
import static projects.medicationtracker.Helpers.DBHelper.DATE_FORMAT;
import static projects.medicationtracker.Helpers.DBHelper.TIME_FORMAT;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import projects.medicationtracker.Adapters.HistoryAdapter;
import projects.medicationtracker.Dialogs.BackupDestinationPicker;
import projects.medicationtracker.Dialogs.FilterDialog;
import projects.medicationtracker.Helpers.NativeDbHelper;
import projects.medicationtracker.Interfaces.IDialogCloseListener;
import projects.medicationtracker.Models.Dose;
import projects.medicationtracker.Models.FilterField;
import projects.medicationtracker.Models.Medication;

public class MedicationHistory extends AppCompatActivity implements IDialogCloseListener {
    private long medId;
    private NativeDbHelper db;
    private Medication medication;
    private HistoryAdapter historyAdapter;
    private RecyclerView recyclerView;
    private LinearLayout barrier;
    private TextView headerText;
    private String dateFormat;
    private String timeFormat;
    private FilterField<LocalDate>[] filters = new FilterField[]{};
    private TextView noRecords;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_history);
        Intent returnToMyMeds = new Intent(this, MyMedications.class);

        medId = getIntent().getLongExtra("ID", -1);
        barrier = findViewById(R.id.table_barrier);
        headerText = findViewById(R.id.schedule_label);
        barrier.setBackgroundColor(headerText.getCurrentTextColor());

        if (medId == -1) {
            finish();
            startActivity(returnToMyMeds);
        }

        db = new NativeDbHelper(this);

        medication = db.getMedicationHistory(medId);

        if (medication == null) {
            finish();
            startActivity(returnToMyMeds);
        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.history) + ": " + medication.getName());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPressed();
            }
        });

        dateFormat = MainActivity.preferences.getString(DATE_FORMAT);
        timeFormat = MainActivity.preferences.getString(TIME_FORMAT);

        noRecords = findViewById(R.id.no_records);

        if (medication.getDoses() == null || medication.getDoses().length == 0) {
            noRecords.setVisibility(View.VISIBLE);
            findViewById(R.id.export_history).setEnabled(false);
            findViewById(R.id.filter_button).setEnabled(false);
            findViewById(R.id.history_view).setVisibility(GONE);

            return;
        }

        Medication ultimateParent = getUltimateParent(medication);

        recyclerView = findViewById(R.id.history_view);

        Dose[] combinedDoses = combineDoses(ultimateParent, new Dose[]{});

        historyAdapter = new HistoryAdapter(
                dateFormat,
                timeFormat,
                ultimateParent,
                combinedDoses
        );
        recyclerView.setAdapter(historyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Handles back button or back gesture
     */
    private void handleBackPressed() {
        Intent intent = new Intent(this, MyMedications.class);
        finish();
        startActivity(intent);
    }

    /**
     * Determines which button was selected
     * @param item Selected menu option
     * @return Selected option
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MyMedications.class);
            finish();
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Recursively searches a medication's lineage for it's original parent
     * @param m Medication whose parent is sought
     * @return Oldest parent in medication's lineage
     */
    private Medication getUltimateParent(Medication m) {
        return m.getParent() == null ? m : getUltimateParent(m.getParent());
    }

    /**
     * Handles exporting medication history as a csv file
     */
    public void onExportClick(View view) {
        String defaultName = Objects.equals(medication.getPatientName(), "ME!") ?
                getString(R.string.your) : medication.getPatientName();
        LocalDateTime now = LocalDateTime.now();

        defaultName += "_" + medication.getName()
                + "_" + now.getYear()
                + "_" + now.getMonthValue()
                + "_" + now.getDayOfMonth()
                + "_" + now.getHour()
                + "_" + now.getMinute()
                + "_" + now.getSecond();

        BackupDestinationPicker backupDestinationPicker = new BackupDestinationPicker(
                "csv",
                defaultName
        );
        backupDestinationPicker.show(getSupportFragmentManager(), null);
    }

    /**
     * Opens filter dialog
     */
    public void onFilterClick(View view) {
        FilterDialog filterDialog = new FilterDialog(filters);
        filterDialog.show(getSupportFragmentManager(), null);
    }

    /**
     * Applies this.filters to medication's doses
     * @return Filtered doses
     */
    private Dose[] filterDoses() {
        Medication trueParent = getUltimateParent(medication);
        return Arrays.stream(combineDoses(trueParent, new Dose[]{})).filter(
            d -> {
                boolean meetsScheduledFilter = true;
                boolean meetsTakenFilter = true;

                for (FilterField<LocalDate> f : filters) {
                    if (f.getField().equals("SCHEDULED")) {
                        switch (f.getOption()) {
                            case GREATER_THAN:
                                meetsScheduledFilter = d.getDoseTime().toLocalDate().isAfter(f.getValue());
                                break;
                            case LESS_THAN:
                                meetsScheduledFilter = d.getDoseTime().toLocalDate().isBefore(f.getValue());
                                break;
                            case EQUALS:
                                meetsScheduledFilter = d.getDoseTime().toLocalDate().isEqual(f.getValue());
                        }
                    }

                    if (f.getField().equals("TAKEN")) {
                        switch (f.getOption()) {
                            case GREATER_THAN:
                                meetsTakenFilter = d.getTimeTaken().toLocalDate().isAfter(f.getValue());
                                break;
                            case LESS_THAN:
                                meetsTakenFilter = d.getTimeTaken().toLocalDate().isBefore(f.getValue());
                                break;
                            case EQUALS:
                                meetsTakenFilter = d.getTimeTaken().toLocalDate().isEqual(f.getValue());
                        }
                    }
                }

                return meetsTakenFilter && meetsScheduledFilter;
            }
        ).toArray(Dose[]::new);
    }

    /**
     * Handle response from a dialog
     * @param action Action performed in dialog
     * @param data Object returned by dialog
     */
    @Override
    public void handleDialogClose(Action action, Object data) {
        switch (action) {
            case CREATE: // Create CSV file
                final String[] dialogRes = (String[]) data;

                String exportDir = dialogRes[0];
                String exportFile = dialogRes[1];
                String fileExtension = dialogRes[2];

                String exportPath = exportDir + "/" + exportFile + "." + fileExtension;

                boolean exportRes =  db.exportMedicationHistory(exportDir + '/' + exportFile + "." + fileExtension, getTableData());

                String message = exportRes ? getString(R.string.successful_export, exportPath) : getString(R.string.failed_export);

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                break;
            case FILTERS_APPLIED: // Modify filters
                FilterField<LocalDate>[] filtersApplied = (FilterField<LocalDate>[]) data;
                this.filters = filtersApplied;

                Dose[] filteredDoses = filterDoses();

                historyAdapter = new HistoryAdapter(
                        dateFormat,
                        timeFormat,
                        getUltimateParent(medication),
                        filteredDoses
                );
                recyclerView.setAdapter(historyAdapter);

                if (filteredDoses.length > 0) {
                    noRecords.setVisibility(GONE);
                } else {
                    noRecords.setVisibility(View.VISIBLE);
                }

                break;
        }
    }

    /**
     * Format dosage history for export
     * @return Formatted dosage history
     */
    private Pair<String, String[]>[] getTableData() {
        Dose[] doses = filterDoses();
        Pair<String, String[]>[] tableData = new Pair[3];
        String[] scheduledTimes, takenTimes, dosages;

        scheduledTimes = new String[doses.length];
        takenTimes = new String[doses.length];
        dosages = new String[doses.length];

        for (int i = 0; i < doses.length; i++) {
            LocalDateTime scheduledDateTime = doses[i].getDoseTime();
            LocalDateTime takenDateTime = doses[i].getTimeTaken();
            Medication med = getDoseMedication(doses[i].getMedId());

            String scheduleDate = DateTimeFormatter.ofPattern(
                    dateFormat, Locale.getDefault()
            ).format(scheduledDateTime.toLocalDate());
            String scheduleTime = DateTimeFormatter.ofPattern(
                    timeFormat, Locale.getDefault()
            ).format(scheduledDateTime.toLocalTime());

            String takenDate = DateTimeFormatter.ofPattern(
                    dateFormat, Locale.getDefault()
            ).format(takenDateTime.toLocalDate());
            String takenTime = DateTimeFormatter.ofPattern(
                    timeFormat, Locale.getDefault()
            ).format(takenDateTime.toLocalTime());

            scheduledTimes[i] = scheduleDate + " " + scheduleTime;
            takenTimes[i] = takenDate + " " + takenTime;

            if (med != null) {
                dosages[i] = med.getDosage() + " " + med.getDosageUnits();
            } else {
                dosages[i] = "N/A";
            }
        }

        tableData[0] = new Pair<>(getString(R.string.scheduled), scheduledTimes);
        tableData[1] = new Pair<>(getString(R.string.taken), takenTimes);
        tableData[2] = new Pair<>(getString(R.string.dosage_hist), dosages);

        return tableData;
    }

    /**
     * Get the medication in lineage corresponding to the provided ID
     * @param medId ID of medication to find
     * @return Medication with medId or null
     */
    private Medication getDoseMedication(long medId) {
        Medication currentMed = medication;

        while (currentMed != null) {
            if (currentMed.getId() == medId) {
                return currentMed;
            }

            currentMed = currentMed.getChild();
        }

        return null;
    }

    /**
     * Combines doses from medication lineage into a single array
     * @param currentMed Medication in lineage whose doses are to be entered into the array
     * @param doses Array in which to store medications
     * @return Doses from all medications in lineage
     */
    private Dose[] combineDoses(Medication currentMed, Dose[] doses) {
        doses = Stream.concat(
                Arrays.stream(doses),
                Arrays.stream(currentMed.getDoses())
        ).toArray(Dose[]::new);

        if (currentMed.getChild() != null) {
            return combineDoses(currentMed.getChild(), doses);
        }

        return doses;
    }
}