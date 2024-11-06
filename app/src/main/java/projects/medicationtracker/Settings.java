package projects.medicationtracker;

import static projects.medicationtracker.Helpers.DBHelper.DARK;
import static projects.medicationtracker.Helpers.DBHelper.DATE_FORMAT;
import static projects.medicationtracker.Helpers.DBHelper.DEFAULT;
import static projects.medicationtracker.Helpers.DBHelper.EXPORT_FILE_NAME;
import static projects.medicationtracker.Helpers.DBHelper.LIGHT;
import static projects.medicationtracker.Helpers.DBHelper.THEME;
import static projects.medicationtracker.Helpers.DBHelper.TIME_FORMAT;
import static projects.medicationtracker.MainActivity.preferences;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

import projects.medicationtracker.Dialogs.BackupDestinationPicker;
import projects.medicationtracker.Fragments.ConfirmDeleteAllFragment;
import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NativeDbHelper;
import projects.medicationtracker.Interfaces.IDialogCloseListener;

public class Settings extends AppCompatActivity implements IDialogCloseListener {
    private final DBHelper db = new DBHelper(this);
    private ActivityResultLauncher<Intent> chooseFileLauncher;
    private final ActivityResultLauncher<String> permissionRequester = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
            }
    );
    private NativeDbHelper nativeDb;

    /**
     * Create Settings
     *
     * @param savedInstanceState Saved instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.settings));

        Button deleteAllButton = findViewById(R.id.deleteAllButton);
        deleteAllButton.setBackgroundColor(Color.parseColor("#DD2222"));

        Button enableNotificationsButton = findViewById(R.id.enableNotifications);
        SwitchCompat notificationToggle = findViewById(R.id.enableNotificationSwitch);

        nativeDb = new NativeDbHelper(this);

        if (Build.VERSION.SDK_INT >= 33) {
            enableNotificationsButton.setVisibility(View.VISIBLE);
            notificationToggle.setVisibility(View.GONE);
        } else {
            enableNotificationsButton.setVisibility(View.GONE);
            notificationToggle.setVisibility(View.VISIBLE);
        }

        setTimeBeforeDoseRestrictionSwitch();
        setEnableNotificationSwitch();
        setThemeMenu();
        setDateFormatMenu();
        setTimeFormatMenu();
        setLanguageMenu();

        chooseFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() != null && result.getData().getData() != null && result.getData().getData().getPath() != null) {
                        Uri uri = result.getData().getData();

                        ContentResolver contentResolver = getContentResolver();
                        InputStream inputStream;
                        int size;
                        String contents;
                        boolean success = false;
                        int length;
                        byte[] bytes;

                        try {
                            try {
                                inputStream = contentResolver.openInputStream(uri);
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }

                            try {
                                assert inputStream != null;
                                size = inputStream.available();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            DataInputStream dis = new DataInputStream(inputStream);
                            bytes = new byte[size];

                            try {
                                length = dis.read(bytes, 0, size);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            contents = new String(bytes, 0, length);

                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            success = nativeDb.dbImport(contents);
                        } catch (Exception e) {
                            Log.e("Import Error", "Error occurred when reading file");
                        } finally {
                            if (success) {
                                Toast.makeText(
                                        this,
                                        getString(R.string.import_success),
                                        Toast.LENGTH_SHORT
                                ).show();
                            } else {
                                Toast.makeText(
                                        this,
                                        getString(R.string.failed_import),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                    } else {
                        Toast.makeText(
                                this,
                                getString(R.string.could_not_retrieve_file),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    /**
     * Determines which button was selected
     *
     * @param item Selected menu option
     * @return Selected option
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Return to MainActivity if back arrow is pressed
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    /**
     * Prepares dose restriction switch
     */
    private void setTimeBeforeDoseRestrictionSwitch() {
        SwitchCompat timeBeforeDoseSwitch = findViewById(R.id.disableTimeBeforeDose);

        int timeBeforeDose = db.getTimeBeforeDose();

        timeBeforeDoseSwitch.setChecked(timeBeforeDose == -1);

        timeBeforeDoseSwitch.setOnCheckedChangeListener((compoundButton, b) ->
        {
            if (timeBeforeDoseSwitch.isChecked()) {
                LinearLayout setHoursBeforeLayout = findViewById(R.id.timeBeforeDoseLayout);
                setHoursBeforeLayout.setVisibility(View.GONE);

                db.setTimeBeforeDose(-1);
            } else {
                setHoursBeforeDoseEditText(2, timeBeforeDoseSwitch.isChecked());
                db.setTimeBeforeDose(2);
            }
        });

        setHoursBeforeDoseEditText(timeBeforeDose, timeBeforeDoseSwitch.isChecked());
    }

    /**
     * Prepares dose restriction EditText
     */
    private void setHoursBeforeDoseEditText(int hoursBefore, boolean disabled) {
        if (disabled)
            return;

        LinearLayout setHoursBeforeLayout = findViewById(R.id.timeBeforeDoseLayout);
        TextInputEditText enterTimeBeforeDose = findViewById(R.id.enterTimeBeforeDose);

        enterTimeBeforeDose.setText(String.valueOf(hoursBefore));

        setHoursBeforeLayout.setVisibility(View.VISIBLE);

        enterTimeBeforeDose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = enterTimeBeforeDose.getText().toString();

                if (!text.isEmpty()) {
                    if (!isValidInt(text)) {
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
     * Prepares the menu for themes
     */
    private void setThemeMenu() {
        MaterialAutoCompleteTextView themeSelector = findViewById(R.id.themeSelector);
        String savedTheme = preferences.getString(THEME, DEFAULT);

        themeSelector.setAdapter(createThemeMenuAdapter());

        switch (savedTheme) {
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
            switch (position) {
                case 0:
                    AppCompatDelegate.setDefaultNightMode(
                            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    );
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

        themeSelector.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                themeSelector.setAdapter(createThemeMenuAdapter());
            }
        });
    }

    /**
     * Builds date format selector menu
     */
    private void setDateFormatMenu() {
        MaterialAutoCompleteTextView dateSelector = findViewById(R.id.date_format_selector);
        String dateFormat = preferences.getString(DATE_FORMAT, DBHelper.DateFormats.MM_DD_YYYY);

        dateSelector.setAdapter(createDateFormatMenuAdapter());

        switch (dateFormat) {
            case DBHelper.DateFormats.MM_DD_YYYY:
                dateSelector.setText(dateSelector.getAdapter().getItem(0).toString(), false);
                break;
            case DBHelper.DateFormats.DD_MM_YYYY:
                dateSelector.setText(dateSelector.getAdapter().getItem(1).toString(), false);
                break;
        }

        dateSelector.setOnItemClickListener((parent, view, position, id) -> {
            String timeFormat = preferences.getString(TIME_FORMAT, DBHelper.TimeFormats._12_HOUR);

            switch (position) {
                case 0:
                    if (!dateFormat.equals(DBHelper.DateFormats.MM_DD_YYYY)) {
                        db.setDateTimeFormat(DBHelper.DateFormats.MM_DD_YYYY, timeFormat);
                    }
                    break;
                case 1:
                    if (!dateFormat.equals(DBHelper.DateFormats.DD_MM_YYYY)) {
                        db.setDateTimeFormat(DBHelper.DateFormats.DD_MM_YYYY, timeFormat);
                    }
            }

            dateSelector.clearFocus();
            preferences = db.getPreferences();
        });
    }

    /**
     * Builds time format selector menu
     */
    private void setTimeFormatMenu() {
        MaterialAutoCompleteTextView timeSelector = findViewById(R.id.time_format_selector);
        String timeFormat = preferences.getString(TIME_FORMAT, DBHelper.TimeFormats._12_HOUR);

        timeSelector.setAdapter(createTimeFormatMenuAdapter());

        switch (timeFormat) {
            case DBHelper.TimeFormats._12_HOUR:
                timeSelector.setText(timeSelector.getAdapter().getItem(0).toString(), false);
                break;
            case DBHelper.TimeFormats._24_HOUR:
                timeSelector.setText(timeSelector.getAdapter().getItem(1).toString(), false);
                break;
        }

        timeSelector.setOnItemClickListener((parent, view, position, id) -> {
            String dateFormat = preferences.getString(DATE_FORMAT, DBHelper.DateFormats.DD_MM_YYYY);

            switch (position) {
                case 0:
                    if (!timeFormat.equals(DBHelper.TimeFormats._12_HOUR)) {
                        db.setDateTimeFormat(dateFormat, DBHelper.TimeFormats._12_HOUR);
                    }
                    break;
                case 1:
                    if (!timeFormat.equals(DBHelper.TimeFormats._24_HOUR)) {
                        db.setDateTimeFormat(dateFormat, DBHelper.TimeFormats._24_HOUR);
                    }
            }

            timeSelector.clearFocus();
            preferences = db.getPreferences();
        });
    }

    /**
     * Creates menu allow language change
     */
    private void setLanguageMenu() {
        MaterialAutoCompleteTextView langSelector = findViewById(R.id.language_selector);
        String[] langOpts = {"Deutsch", "English", "Español", "Italiano", "Türkçe"};
        String[] langCodes = {"de", "en", "es", "it", "tr"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                langOpts
        );

        Configuration config = getResources().getConfiguration();

        langSelector.setAdapter(adapter);

        String langCode = config.getLocales().get(0).getLanguage();

        switch (langCode) {
            case "de":
                langSelector.setText(langOpts[0], false);
                break;
            case "en":
            default:
                langSelector.setText(langOpts[1], false);
                break;
            case "es":
                langSelector.setText(langOpts[2], false);
                break;
            case "it":
                langSelector.setText(langOpts[3], false);
                break;
            case "tr":
                langSelector.setText(langOpts[4], false);
        }

        langSelector.setOnItemClickListener((parent, view, position, id) -> {
            LocaleListCompat locale = LocaleListCompat.forLanguageTags(langCodes[position]);
            AppCompatDelegate.setApplicationLocales(locale);

            langSelector.clearFocus();
        });

        langSelector.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setThemeMenu();
                setTimeFormatMenu();
                setDateFormatMenu();

                langSelector.setAdapter(
                        new ArrayAdapter<>(
                                langSelector.getContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                langOpts
                        )
                );
            }
        });
    }

    /**
     * Creates array adapter for themes selector
     *
     * @return ArrayAdapter of theme options
     */
    private ArrayAdapter<String> createThemeMenuAdapter() {
        ArrayList<String> availableThemes = new ArrayList<>();

        availableThemes.add(getString(R.string.match_system_theme));
        availableThemes.add(getString(R.string.light_mode));
        availableThemes.add(getString(R.string.dark_mode));

        return new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, availableThemes
        );
    }

    /**
     * Creates ArrayAdapter for date format options
     *
     * @return ArrayAdapter of date format options
     */
    private ArrayAdapter<String> createDateFormatMenuAdapter() {
        ArrayList<String> formats = new ArrayList<>();

        formats.add(getString(R.string.date_format_mm_dd_yyyy));
        formats.add(getString(R.string.date_format_dd_mm_yyyy));

        return new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, formats
        );
    }

    /**
     * Creates ArrayAdapter for time format options
     *
     * @return ArrayAdapter of time format options
     */
    private ArrayAdapter<String> createTimeFormatMenuAdapter() {
        ArrayList<String> formats = new ArrayList<>();

        formats.add(getString(R.string.hour_12));
        formats.add(getString(R.string.hour_24));

        return new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, formats
        );
    }

    /**
     * Listener for export data button
     */
    public void onExportClick(View view) {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            permissionRequester.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        BackupDestinationPicker picker = new BackupDestinationPicker("json");
        picker.show(getSupportFragmentManager(), null);
    }

    public void onImportClick(View view) {
        String type = Build.VERSION.SDK_INT >= 30 ? "application/json" : "*/*";
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType(type);

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            permissionRequester.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        chooseFileLauncher.launch(i);
    }

    /**
     * Listener for button that deletes all saved data
     */
    public void onPurgeButtonClick(View view) {
        ConfirmDeleteAllFragment deleteAllFragment = new ConfirmDeleteAllFragment(db);
        deleteAllFragment.show(getSupportFragmentManager(), null);
    }

    /**
     * Checks if a String can be parsed to int
     *
     * @param stringToParse The String to convert to an int
     * @return True if can be parsed, else false
     */
    public static boolean isValidInt(String stringToParse) {
        try {
            Integer.parseInt(stringToParse);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public void OnEnableNotificationsClick(View view) {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            permissionRequester.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        } else {
            Toast.makeText(this, getString(R.string.notifications_already_enabled), Toast.LENGTH_SHORT).show();
        }
    }

    public void onScheduleExportClick(View view) {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            permissionRequester.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        BackupDestinationPicker picker = new BackupDestinationPicker("json", true);
        picker.show(getSupportFragmentManager(), null);
    }

    /**
     * Enable notifications for application
     */
    private void setEnableNotificationSwitch() {
        SwitchCompat enableNotificationsSwitch = findViewById(R.id.enableNotificationSwitch);

        enableNotificationsSwitch.setChecked(db.getNotificationEnabled());

        enableNotificationsSwitch.setOnCheckedChangeListener(((compoundButton, b) ->
                db.setNotificationEnabled(enableNotificationsSwitch.isChecked())));
    }

    @Override
    public void handleDialogClose(Action action, Object data) {
        final String[] dialogRes = (String[]) data;

        String exportDir = dialogRes[0];
        String exportFile = dialogRes[1];
        String fileExtension = dialogRes[2];
        boolean res;
        String resMessage;

        if (action == Action.ADD) {
            preferences.putString(EXPORT_FILE_NAME, );
        }

        res = nativeDb.dbExport(exportDir + '/' + exportFile + "." + fileExtension);

        resMessage = res ? getString(R.string.successful_export, exportDir + '/' + exportFile)
                : getString(R.string.failed_export);

        Toast.makeText(this, resMessage, Toast.LENGTH_SHORT).show();
    }
}