package projects.medicationtracker;

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

import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_history);

        medId = getIntent().getLongExtra("ID", -1);
        barrier = findViewById(R.id.table_barrier);
        headerText = findViewById(R.id.schedule_label);

        if (medId == -1) {
            Intent returnToMyMeds = new Intent(this, MyMedications.class);
            finish();
            startActivity(returnToMyMeds);
        }

        db = new NativeDbHelper(MainActivity.DATABASE_DIR);

        medication = db.getMedicationHistory(medId);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.history);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPressed();
            }
        });

        dateFormat = MainActivity.preferences.getString(DATE_FORMAT);
        timeFormat = MainActivity.preferences.getString(TIME_FORMAT);

        Medication ultimateParent = getUltimateParent(medication);

        recyclerView = findViewById(R.id.history_view);
        historyAdapter = new HistoryAdapter(
                dateFormat,
                timeFormat,
                ultimateParent,
                combineDoses(ultimateParent, new Dose[]{})
        );
        recyclerView.setAdapter(historyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        barrier.setBackgroundColor(headerText.getCurrentTextColor());
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

    private Medication getUltimateParent(Medication m) {
        return m.getParent() == null ? m : getUltimateParent(m.getParent());
    }

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

    public void onFilterClick(View view) {
        FilterDialog filterDialog = new FilterDialog();
        filterDialog.show(getSupportFragmentManager(), null);
    }
    
    private void onFiltersChanged() {
        Medication trueParent = getUltimateParent(medication);
        Dose[] dosesArr = Arrays.stream(combineDoses(trueParent, new Dose[]{})).filter(
            d -> {
                for (FilterField<LocalDate> f : this.filters) {

                    if (f.getField().equals("SCHEDULED")) {
                        switch (f.getOption()) {
                            case GREATER_THAN:
                                return d.getDoseTime().toLocalDate().isAfter(f.getValue());
                            case LESS_THAN:
                                return d.getDoseTime().toLocalDate().isBefore(f.getValue());
                            case EQUALS:
                                return d.getDoseTime().toLocalDate().isEqual(f.getValue());
                        }
                    }

                    if (f.getField().equals("TAKEN")) {
                        switch (f.getOption()) {
                            case GREATER_THAN:
                                return d.getTimeTaken().toLocalDate().isAfter(f.getValue());
                            case LESS_THAN:
                                return d.getTimeTaken().toLocalDate().isBefore(f.getValue());
                            case EQUALS:
                                return d.getTimeTaken().toLocalDate().isEqual(f.getValue());
                        }
                    }
                }

                return true;
            }
        ).toArray(Dose[]::new);

        historyAdapter = new HistoryAdapter(
                dateFormat,
                timeFormat,
                trueParent,
                dosesArr
        );
        recyclerView.setAdapter(historyAdapter);
    }

    @Override
    public void handleDialogClose(Action action, Object data) {
        FilterField<LocalDate>[] filtersApplied = (FilterField<LocalDate>[]) data;

        switch (action) {
            case CREATE: // Create CSV file
                final String[] dialogRes = (String[]) data;

                String exportDir = dialogRes[0];
                String exportFile = dialogRes[1];
                String fileExtension = dialogRes[2];

                boolean exportRes =  db.exportMedicationHistory(exportDir + '/' + exportFile + "." + fileExtension, getTableData());

                String message = exportRes ? getString(R.string.successful_export, data) : getString(R.string.failed_export);

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                break;
            case FILTERS_APPLIED: // Modify filters
                this.filters = filtersApplied;
                onFiltersChanged();

                break;
        }
    }

    private Pair<String, String[]>[] getTableData() {
        ArrayList<Dose> doses = new ArrayList<>();
        Medication currentMed = getUltimateParent(medication);
        Pair<String, String[]>[] tableData = new Pair[3];
        String[] scheduledTimes, takenTimes, dosages;

        while (currentMed != null) {
            doses.addAll(Arrays.asList(currentMed.getDoses()));

            currentMed = currentMed.getParent();
        }

        scheduledTimes = new String[doses.size()];
        takenTimes = new String[doses.size()];
        dosages = new String[doses.size()];

        for (int i = 0; i < doses.size(); i++) {
            LocalDateTime scheduledDateTime = doses.get(i).getDoseTime();
            LocalDateTime takenDateTime = doses.get(i).getTimeTaken();
            Medication med = getDoseMedication(doses.get(i).getMedId());

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