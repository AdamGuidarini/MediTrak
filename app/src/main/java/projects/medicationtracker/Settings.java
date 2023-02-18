package projects.medicationtracker;

import static projects.medicationtracker.Helpers.DBHelper.DARK;
import static projects.medicationtracker.Helpers.DBHelper.DEFAULT;
import static projects.medicationtracker.Helpers.DBHelper.LIGHT;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.Objects;

import projects.medicationtracker.Fragments.ConfirmDeleteAllFragment;
import projects.medicationtracker.Helpers.DBHelper;

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

        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.settings));

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
                    if (!isValidInt(text))
                    {
                        enterTimeBeforeDose.setError(getString(R.string.invalid_value));
                        return;
                    }

                    int timeBefore = Integer.parseInt(text);

                    if (timeBefore > 0)
                        db.setTimeBeforeDose(timeBefore);
                    else
                        enterTimeBeforeDose.setError(getString(R.string.must_be_positive_int));
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
        String savedTheme = db.getSavedTheme();

        themeSelector.setAdapter(createThemeMenuAdapter());

        switch (savedTheme)
        {
            case DEFAULT:
                themeSelector.setText(themeSelector.getAdapter().getItem(0).toString(), false);
                break;
            case LIGHT:
                themeSelector.setText(themeSelector.getAdapter().getItem(1).toString(), false);
                break;
            case DARK:
                themeSelector.setText(themeSelector.getAdapter().getItem(2).toString(), false);
                break;
        }

        themeSelector.setOnItemClickListener((parent, view, position, id) ->
        {
            switch (position)
            {
                case 0:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    db.saveTheme(DEFAULT);
                    break;
                case 1:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    db.saveTheme(LIGHT);
                    break;
                case 2:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    db.saveTheme(DARK);
                    break;
            }

            themeSelector.clearFocus();
        });

        themeSelector.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s)
            {
                ArrayList<String> _availableThemes = new ArrayList<>();

                _availableThemes.add(getString(R.string.match_system_theme));
                _availableThemes.add(getString(R.string.light_mode));
                _availableThemes.add(getString(R.string.dark_mode));

                themeSelector.setAdapter(createThemeMenuAdapter());
            }
        });
    }

    private ArrayAdapter<String> createThemeMenuAdapter()
    {
        ArrayList<String> availableThemes = new ArrayList<>();

        availableThemes.add(getString(R.string.match_system_theme));
        availableThemes.add(getString(R.string.light_mode));
        availableThemes.add(getString(R.string.dark_mode));

        return new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, availableThemes
        );
    }

    /**
     * Listener for button that deletes all saved data
     */
    public void onPurgeButtonClick(View view)
    {
        ConfirmDeleteAllFragment deleteAllFragment = new ConfirmDeleteAllFragment(db);
        deleteAllFragment.show(getSupportFragmentManager(), null);
    }

    /**
     * Checks if a String can be parsed to int
     * @param stringToParse The String to convert to an int
     * @return True if can be parsed, else false
     */
    public static boolean isValidInt(String stringToParse)
    {
        try
        {
            Integer.parseInt(stringToParse);
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }
}