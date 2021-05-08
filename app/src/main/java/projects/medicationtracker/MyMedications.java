package projects.medicationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Spinner;

import java.util.Objects;

public class MyMedications extends AppCompatActivity
{
    DBHelper db = new DBHelper(this);

    /**
     * Creates MyMedications
     * @param savedInstanceState Saved instances
     **************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_medications);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Objects.requireNonNull(getSupportActionBar()).setTitle("My Medications");

        Spinner nameSpinner = findViewById(R.id.nameSpinner);
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