package projects.medicationtracker;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.time.LocalTime;

public class EditMedicationHelper
{
    final DBHelper db;
    final Medication medication;
    final Activity activity;
    final LocalTime[] medicationTimes;

    public EditMedicationHelper(Medication medication, Activity activity)
    {
        this.medication = medication;
        this.activity = activity;
        this.db = new DBHelper(activity.getBaseContext());
        this.medicationTimes = db.getMedicationTimes(medication.getMedId());

        setPatientButtons();
        setMedicationName();
        setAlias();
        setDosage();
        setFrequencyButton();
    }

    private void setPatientButtons()
    {
        // Set radio button
        if (medication.getPatientName().equals("ME!"))
        {
            RadioButton meButton = activity.findViewById(R.id.meButtonEdit);
            meButton.setChecked(true);
        }
        else
        {
            RadioButton otherButton = activity.findViewById(R.id.otherButtonEdit);
            otherButton.setChecked(true);

            EditText enterPatientName = activity.findViewById(R.id.editPatientNameEditText);
            enterPatientName.setText(medication.getPatientName());
            enterPatientName.setVisibility(View.VISIBLE);
        }
    }

    private void setMedicationName()
    {
        EditText enterMedicationName = activity.findViewById(R.id.editMedicationName);
        enterMedicationName.setText(medication.getMedName());
    }

    private void setAlias()
    {
        EditText enterAlias = activity.findViewById(R.id.editAlias);
        enterAlias.setText(medication.getAlias());
    }

    private void setDosage()
    {
        EditText enterDosage = activity.findViewById(R.id.editMedDosageEnter);
        enterDosage.setText(String.valueOf(medication.getMedDosage()));

        EditText enterDosageUnits = activity.findViewById(R.id.editEnterMedUnits);
        enterDosageUnits.setText(medication.getMedDosageUnits());
    }

    private void setFrequencyButton()
    {
        RadioButton button;

        if (medication.getMedFrequency() != 1440)
            button = activity.findViewById(R.id.editCustomFreqButton);
        else if (medicationTimes.length > 1)
            button = activity.findViewById(R.id.editMultiplePerDay);
        else
            button = activity.findViewById(R.id.editDailyButton);
        
        button.setChecked(true);
    }
}
