package projects.medicationtracker;

import static projects.medicationtracker.Helpers.DBHelper.DARK;
import static projects.medicationtracker.Helpers.DBHelper.DEFAULT;
import static projects.medicationtracker.Helpers.DBHelper.LIGHT;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.Objects;

import projects.medicationtracker.Fragments.ConfirmDeleteAllFragment;
import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.InputValidation;

public class Settings extends AppCompatActivity
{
    DBHelper db = new DBHelper(this);

    /**
     * Create Settings
     * @param savedInstanceState Saved instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Settings");

        Button purgeButton = findViewById(R.id.purgeButton);
        purgeButton.setBackgroundColor(Color.RED);

        setTimeBeforeDoseRestrictionSwitch();
        setEnableNotificationSwitch();
        setThemeMenu();
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

    /**
     * Prepares dose restriction switch
     */
    private void setTimeBeforeDoseRestrictionSwitch()
    {
        SwitchCompat timeBeforeDoseSwitch = findViewById(R.id.disableTimeBeforeDose);

        int timeBeforeDose = db.getTimeBeforeDose();

        timeBeforeDoseSwitch.setChecked(timeBeforeDose == -1);

        timeBeforeDoseSwitch.setOnCheckedChangeListener((compoundButton, b) ->
        {
            if (timeBeforeDoseSwitch.isChecked())
            {
                LinearLayout setHoursBeforeLayout = findViewById(R.id.timeBeforeDoseLayout);
                setHoursBeforeLayout.setVisibility(View.GONE);

                db.setTimeBeforeDose(-1);
            }
            else
            {
                setHoursBeforeDoseEditText(2, timeBeforeDoseSwitch.isChecked());
                db.setTimeBeforeDose(2);
            }
        });

        setHoursBeforeDoseEditText(timeBeforeDose, timeBeforeDoseSwitch.isChecked());
    }

    /**
     * Prepares dose restriction EditText
     */
    private void setHoursBeforeDoseEditText(int hoursBefore, boolean disabled)
    {
        if (disabled)
            return;

        LinearLayout setHoursBeforeLayout = findViewById(R.id.timeBeforeDoseLayout);
        EditText enterTimeBeforeDose = findViewById(R.id.enterTimeBeforeDose);

        enterTimeBeforeDose.setText(String.valueOf(hoursBefore));

        setHoursBeforeLayout.setVisibility(View.VISIBLE);

        enterTimeBeforeDose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable)
            {
                String text = enterTimeBeforeDose.getText().toString();

                if (!text.isEmpty())
                {
                    if (!InputValidation.isValidInt(text))
                    {
                        enterTimeBeforeDose.setError("Invalid value entered");
                        return;
                    }

                    int timeBefore = Integer.parseInt(text);

                    if (timeBefore > 0)
                        db.setTimeBeforeDose(timeBefore);
                    else
                        enterTimeBeforeDose.setError("Value must be a positive integer");
                }
            }
        });
    }

    /**
     * Enable notifications for application
     */
    private void setEnableNotificationSwitch()
    {
        SwitchCompat enableNotificationsSwitch = findViewById(R.id.enableNotificationSwitch);

        enableNotificationsSwitch.setChecked(db.getNotificationEnabled());

        enableNotificationsSwitch.setOnCheckedChangeListener(((compoundButton, b) ->
                db.setNotificationEnabled(enableNotificationsSwitch.isChecked())));
    }

    /**
     * Prepares the menu for themes
     */
    private void setThemeMenu()
    {
        MaterialAutoCompleteTextView themeSelector = findViewById(R.id.themeSelector);
        ArrayList<String> availableThemes = new ArrayList<>();
        ArrayAdapter<String> themes;
        Resources.Theme selectedTheme = getTheme();
        String savedTheme = db.getSavedTheme();

        availableThemes.add(getString(R.string.match_system_theme));
        availableThemes.add(getString(R.string.light_mode));
        availableThemes.add(getString(R.string.dark_mode));

        themes = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, availableThemes
        );

        themeSelector.setAdapter(themes);

        Toast.makeText(this, savedTheme, Toast.LENGTH_SHORT).show();

        switch (savedTheme)
        {
            case DEFAULT:
                break;
            case LIGHT:
                break;
            case DARK:
                break;
        }
    }

    /**
     * Listener for button that deletes all saved data
     */
    public void onPurgeButtonClick(View view)
    {
        ConfirmDeleteAllFragment deleteAllFragment = new ConfirmDeleteAllFragment(db);
        deleteAllFragment.show(getSupportFragmentManager(), null);
    }
}