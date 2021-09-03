package projects.medicationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class EditMedication extends AppCompatActivity
{
    final DBHelper db = new DBHelper(this);
    Medication medication;

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
        getSupportActionBar().setTitle("Edit Medication");

        CardView editMedCard = findViewById(R.id.editMedCard);
        CardCreator.setCardParams(editMedCard);

        medication = db.getMedication(getIntent().getLongExtra("medId", 0));

        new EditMedicationHelper(medication, this);


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
     * Deletes medication
     * @param item Menu item represented by a garbage can icon.
     */
    public void onDeleteMedClick(MenuItem item)
    {
        ConfirmMedicationDeleteFragment confirmMedicationDeleteFragment =
                new ConfirmMedicationDeleteFragment(db, medication);
        confirmMedicationDeleteFragment.show(getSupportFragmentManager(), null);
    }

    /**
     * Saves changes to medication.
     * @param item The save icon.
     */
    public void onSaveEditClick(MenuItem item)
    {
        if (validateUpdate())
        {
            Intent intent = new Intent(this, MyMedications.class);

            changeMedicationValues();

            db.updateMedication(medication);

            finish();
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Please complete the required fields",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validates input.
     * @return True if submitted, false if not.
     */
    public boolean validateUpdate()
    {
        boolean inputIsValid = true;
        LinearLayout timesOfDay = findViewById(R.id.editTimesInDay);
        RadioGroup patientGroup = findViewById(R.id.editPatientGroup);
        RadioButton multiplePerDay = findViewById(R.id.editMultiplePerDay);
        RadioButton dailyButton = findViewById(R.id.editDailyButton);
        EditText patientName = findViewById(R.id.editPatientNameEditText);
        EditText medName = findViewById(R.id.editMedicationName);
        EditText dosage = findViewById(R.id.editMedDosageEnter);
        EditText dosageUnits = findViewById(R.id.editEnterMedUnits);
        Toast fillOutFields = Toast.makeText(this, "Please set all requested times", Toast.LENGTH_SHORT);

        if (patientGroup.getChildAt(1).isSelected() && patientName.getText().toString().equals(""))
        {
            inputIsValid = false;
            patientName.setError("Enter a name or select \"Me\"");
        }

        if (medName.getText().toString().equals(""))
        {
            inputIsValid = false;
            medName.setError("Enter a name for this medication");
        }

        if (dosage.getText().toString().equals(""))
        {
            inputIsValid = false;
            dosage.setError("Enter medication dosage");
        }

        if (dosageUnits.getText().toString().equals(""))
        {
            inputIsValid = false;
            dosageUnits.setError("Enter a unit for this medication's dosage");
        }

        if (multiplePerDay.isChecked())
        {
            EditText timesTaken = findViewById(R.id.editNumTimesTaken);

            if (timesTaken.getText().toString().equals(""))
            {
                inputIsValid = false;
                timesTaken.setError("Enter number of times per day to take medication");
            }
            else
            {
                int childCount = timesOfDay.getChildCount();

                for (int i = 0; i < childCount; i++)
                {
                    TextView tv = (TextView) timesOfDay.getChildAt(i);

                    if (tv.getText().toString().equals("Tap to set time"))
                    {
                        inputIsValid = false;
                        fillOutFields.show();
                    }
                }
            }
        }
        else if (dailyButton.isChecked())
        {
            TextView dailyTime = findViewById(R.id.editTimeTaken1);

            if (dailyTime.getText().toString().equals(getResources().getString(R.string.atThisTime)))
            {
                inputIsValid = false;
                fillOutFields.show();
            }
        }
        else
        {
            EditText numberOfTimes = findViewById(R.id.editEnterFrequency);

            if (numberOfTimes.getText().toString().equals(""))
            {
                inputIsValid = false;
                numberOfTimes.setError("Enter a frequency.");
            }
            else
            {
                TextView selectDate = findViewById(R.id.editStartDate);
                TextView selectTime = findViewById(R.id.editStartTime);

                if (selectDate.getText().toString().equals(getResources()
                        .getString(R.string.tap_to_set_date)))
                {
                    inputIsValid = false;
                    fillOutFields.show();
                }
                else if (selectTime.getText().toString().equals(getResources()
                        .getString(R.string.tap_to_set_time)))
                {
                    inputIsValid = false;
                    fillOutFields.show();
                }
            }
        }

        return inputIsValid;
    }

    private void changeMedicationValues()
    {
        RadioButton otherPatient = findViewById(R.id.otherButtonEdit);
        RadioButton multiplePerDay = findViewById(R.id.editMultiplePerDay);
        RadioButton dailyButton = findViewById(R.id.editDailyButton);
        EditText patientName = findViewById(R.id.editPatientNameEditText);
        EditText medicationName = findViewById(R.id.editMedicationName);
        EditText alias = findViewById(R.id.editAlias);
        EditText dosage = findViewById(R.id.editMedDosageEnter);
        EditText dosageUnits = findViewById(R.id.editEnterMedUnits);

        if (otherPatient.isChecked())
            medication.setPatientName(patientName.getText().toString());
        else
            medication.setPatientName("ME!");

        medication.setMedName(medicationName.getText().toString());
        medication.setAlias(alias.getText().toString());
        medication.setMedDosage(Integer.parseInt(dosage.getText().toString()));
        medication.setMedDosageUnits(dosageUnits.getText().toString());

        if (multiplePerDay.isChecked())
        {
            LinearLayout timeLayout = findViewById(R.id.editTimesInDay);
            LocalTime[] times = new LocalTime[timeLayout.getChildCount()];

            for (int i = 0; i < timeLayout.getChildCount(); i++)
            {
                TextView tv = (TextView) timeLayout.getChildAt(i);

                int[] hourAndMin = (int[]) tv.getTag();

                times[i] = LocalTime.parse(TimeFormatting.formatTimeForDB(hourAndMin[0], hourAndMin[1]));
            }

            LocalDateTime dateTimes[] = new LocalDateTime[times.length];

            for (int i = 0; i < times.length; i++)
            {
                dateTimes[i] = LocalDateTime.of(LocalDate.now(), times[i]);
            }

            medication.setMedFrequency(1440);
            medication.setTimes(dateTimes);
        }
        else if (dailyButton.isChecked())
        {
            TextView dailyTime = findViewById(R.id.editTimeTaken1);
            int[] hourAndMin = (int[]) dailyTime.getTag();
            LocalTime time =
                    LocalTime.parse(TimeFormatting.formatTimeForDB(hourAndMin[0], hourAndMin[1]));

            LocalDateTime dateTime[] = {LocalDateTime.of(LocalDate.now(), time)};

            medication.setMedFrequency(1440);
            medication.setTimes(dateTime);
        }
        else
        {
            EditText takenEvery = findViewById(R.id.editNumTimesTaken);
            Spinner frequencySpinner = findViewById(R.id.editFrequencySpinner);
            TextView startDate = findViewById(R.id.editStartDate);
            TextView startTime = findViewById(R.id.editStartTime);

            int every = Integer.parseInt(takenEvery.getText().toString());

            switch (frequencySpinner.getSelectedItemPosition())
            {
                case 2:
                    every *= 7;
                case 1:
                    every *= 24;
                case 0:
                    every *= 60;
            }

            medication.setMedFrequency(every);
            medication.setTimes(new LocalDateTime[0]);
        }
    }
}
