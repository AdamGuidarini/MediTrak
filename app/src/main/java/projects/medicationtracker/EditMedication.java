package projects.medicationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import java.util.Objects;

public class EditMedication extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_medication);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Medication");
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
            finish();

        return super.onOptionsItemSelected(item);
    }

    /**
     * Return to MainActivity if back arrow is pressed
     **************************************************************************/
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }
}