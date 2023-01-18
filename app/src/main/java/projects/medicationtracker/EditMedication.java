package projects.medicationtracker;

import static projects.medicationtracker.Fragments.AddEditFormFragment.MED_ID;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import projects.medicationtracker.Dialogs.PauseResumeDialog;
import projects.medicationtracker.Fragments.AddEditFormFragment;
import projects.medicationtracker.Fragments.ConfirmMedicationDeleteFragment;
import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.SimpleClasses.Medication;

public class EditMedication extends AppCompatActivity
{
    private final DBHelper db = new DBHelper(this);
    private Medication medication;
    private MenuItem pauseButton;
    private MenuItem resumeButton;

    /**
     * Instructions on how to build the EditMedications activity
     * @param savedInstanceState Saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_medication);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.edit_medication));

        medication = db.getMedication(getIntent().getLongExtra("medId", 0));

        Bundle medBundle = new Bundle();
        medBundle.putLong(MED_ID, getIntent().getLongExtra("medId", 0));

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.editFragmentView, AddEditFormFragment.class, medBundle)
                .commit();
    }

    /**
     * Creates options menu for activity
     * @param menu Displayed menu
     * @return True if can be created, else false
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.edit_meds_menu, menu);

        pauseButton = menu.findItem(R.id.pause_button);
        resumeButton = menu.findItem(R.id.resume_button);

        if (db.isMedicationActive(medication)) {
            pauseButton.setVisible(true);
        } else {
            resumeButton.setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Determines which button was selected
     * @param item Selected menu option
     * @return Selected option
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            Intent intent = new Intent(this, MyMedications.class);
            finish();
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Return to MainActivity if back arrow is pressed.
     */
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MyMedications.class);
        finish();
        startActivity(intent);
    }

    /**
     * Resumes or pauses a medication
     */
    public void onPauseResumeClick(MenuItem item)
    {
        PauseResumeDialog dialog = new PauseResumeDialog(medication, pauseButton, resumeButton);
        dialog.show(getSupportFragmentManager(), null);
    }

    /**
     * Deletes medication
     * @param item Menu item represented by a garbage can icon.
     */
    public void onDeleteMedClick(MenuItem item)
    {
        ConfirmMedicationDeleteFragment confirmMedicationDeleteFragment =
                new ConfirmMedicationDeleteFragment(db, medication);
        confirmMedicationDeleteFragment.show(getSupportFragmentManager(), null);
    }
}
