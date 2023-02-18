package projects.medicationtracker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import projects.medicationtracker.Fragments.AddEditFormFragment;

public class AddMedication extends AppCompatActivity
{
    /**
     * Builds AddMedication Activity
     * @param savedInstanceState Previous state
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.add_medication));

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.addFragmentView, AddEditFormFragment.class, null)
                .commit();
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
            finish();

        return super.onOptionsItemSelected(item);
    }

    /**
     * Return to MainActivity if back arrow is pressed
     */
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }
}