package projects.medicationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import java.util.Objects;

public class EditMedication extends AppCompatActivity
{
    final DBHelper db = new DBHelper(this);
    LinearLayout editMedLayout;
    Medication medication;

    /**
     * Instructions on how to build the EditMedications activity
     * @param savedInstanceState Saved instance state.
     **************************************************************************/
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
        editMedLayout = findViewById(R.id.editMedLayout);

        CardCreator.createEditMedCard(editMedLayout, medication);
    }

    /**
     * Creates options menu for activity
     * @param menu Displayed menu
     * @return True if can be created, else false
     **************************************************************************/
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
     **************************************************************************/
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
     **************************************************************************/
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
     **************************************************************************/
    public void onDeleteMedClick(MenuItem item)
    {
        ConfirmMedicationDeleteFragment confirmMedicationDeleteFragment = new ConfirmMedicationDeleteFragment(db, medication);
        confirmMedicationDeleteFragment.show(getSupportFragmentManager(), null);
    }
}