package projects.medicationtracker;

import static projects.medicationtracker.Helpers.DBHelper.DATE_FORMAT;
import static projects.medicationtracker.Helpers.DBHelper.TIME_FORMAT;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.Objects;

import projects.medicationtracker.Adapters.HistoryAdapter;
import projects.medicationtracker.Dialogs.BackupDestinationPicker;
import projects.medicationtracker.Dialogs.FilterDialog;
import projects.medicationtracker.Helpers.NativeDbHelper;
import projects.medicationtracker.Interfaces.IDialogCloseListener;
import projects.medicationtracker.Models.Medication;

public class MedicationHistory extends AppCompatActivity implements IDialogCloseListener {
    long medId;
    NativeDbHelper db;
    Medication medication;
    HistoryAdapter historyAdapter;
    RecyclerView recyclerView;
    MaterialButton exportCsvButton;
    MaterialButton filterButton;
    LinearLayout barrier;
    TextView headerText;

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

        recyclerView = findViewById(R.id.history_view);
        historyAdapter = new HistoryAdapter(
                MainActivity.preferences.getString(DATE_FORMAT),
                MainActivity.preferences.getString(TIME_FORMAT),
                getUltimateParent(medication)
        );
        recyclerView.setAdapter(historyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        exportCsvButton = findViewById(R.id.export_history);
        filterButton = findViewById(R.id.filter_button);

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
        BackupDestinationPicker backupDestinationPicker = new BackupDestinationPicker("csv");
        backupDestinationPicker.show(getSupportFragmentManager(), null);
    }

    public void onFilterClick(View view) {
        FilterDialog filterDialog = new FilterDialog();
        filterDialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void handleDialogClose(Action action, Object data) {
        switch (action) {
            case CREATE: // Create CSV file
                break;
            case EDIT: // Modify filters
                break;
            case DELETE: // Clear filters
                break;
        }
    }
}