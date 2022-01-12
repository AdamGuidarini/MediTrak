package projects.medicationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.Objects;

public class Settings extends AppCompatActivity
{
    DBHelper db = new DBHelper(this);

    /**
     * Create Settings
     * @param savedInstanceState Saved instance
     **************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Settings");

        Button purgeButton = findViewById(R.id.purgeButton);
        purgeButton.setBackgroundColor(Color.RED);
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

    public void onPurgeButtonClick(View view)
    {
        ConfirmDeleteAllFragment deleteAllFragment = new ConfirmDeleteAllFragment(db);
        deleteAllFragment.show(getSupportFragmentManager(), null);
    }
}