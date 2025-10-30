package projects.medicationtracker.Dialogs;

import static projects.medicationtracker.Helpers.DBHelper.DATE_FORMAT;
import static projects.medicationtracker.Helpers.DBHelper.EXPORT_FILE_NAME;
import static projects.medicationtracker.Helpers.DBHelper.EXPORT_FREQUENCY;
import static projects.medicationtracker.Helpers.DBHelper.EXPORT_START;
import static projects.medicationtracker.Helpers.DBHelper.TIME_FORMAT;
import static projects.medicationtracker.MainActivity.preferences;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import projects.medicationtracker.Fragments.SelectDateFragment;
import projects.medicationtracker.Fragments.TimePickerFragment;
import projects.medicationtracker.Helpers.NativeDbHelper;
import projects.medicationtracker.Utils.TimeFormatting;
import projects.medicationtracker.Interfaces.IDialogCloseListener;
import projects.medicationtracker.R;

public class BackupDestinationPicker extends DialogFragment {
    private String exportDir;
    private String exportFile;
    private TextInputLayout fileNameInputLayout;
    private final String fileExtension;
    private boolean showPeriodic = false;
    private boolean createNow = false;
    private int frequency = 0;
    private LocalDate startDate;
    private LocalTime startTime;

    private boolean fileNameValid = false;
    private boolean startDateValid = false;
    private boolean startTimeValid = false;
    private boolean frequencyValid = false;

    public BackupDestinationPicker(String fileExtension, String defaultName) {
        this.fileExtension = fileExtension;
        exportFile = defaultName;
    }

    public BackupDestinationPicker(String fileExtension, boolean showPeriodic) {
        this.fileExtension = fileExtension;
        this.showPeriodic = showPeriodic;

        exportFile = "meditrak_backup";
    }

    public BackupDestinationPicker(String fileExtension) {
        LocalDateTime now = LocalDateTime.now();

        this.fileExtension = fileExtension;
        exportFile = "meditrak"
                + "_" + now.getYear()
                + "_" + now.getMonthValue()
                + "_" + now.getDayOfMonth()
                + "_" + now.getHour()
                + "_" + now.getMinute()
                + "_" + now.getSecond();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstances) {
        AlertDialog dialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final String[] directories;
        ArrayAdapter<String> adapter;
        ArrayList<String> dirs = new ArrayList<>();

        builder.setView(inflater.inflate(R.layout.dialog_backup_destination_picker, null));

        builder.setTitle(getString(R.string.export_data));
        builder.setPositiveButton(R.string.export, ((dialogInterface, i) -> {
            onExportClick();
            dismiss();
        }));
        builder.setNegativeButton(R.string.cancel, ((dialogInterface, i) -> dismiss()));

        if (preferences.getInt(EXPORT_FREQUENCY, -1) != -1 && showPeriodic) {
            builder.setNeutralButton(getString(R.string.stop), (dialogInterface, i) -> {
                onStopClick();
                dismiss();
            });
        }

        dialog = builder.create();
        dialog.show();

        MaterialAutoCompleteTextView dirSelector = dialog.findViewById(R.id.export_dir);

        SwitchCompat backupNow = dialog.findViewById(R.id.export_now);

        backupNow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            createNow = isChecked;
        });

        fileNameInputLayout = dialog.findViewById(R.id.export_file_layout);
        TextInputEditText fileName = dialog.findViewById(R.id.export_file);
        ((TextView) dialog.findViewById(R.id.file_extension)).setText("." + fileExtension);

        if (showPeriodic) {
            dialog.findViewById(R.id.export_schedule_layout).setVisibility(View.VISIBLE);
        }

        dirs.add(getString(R.string.downloads));
        dirs.add(getString(R.string.documents));

        directories = new String[] {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath()
        };

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, dirs);

        dirSelector.setAdapter(adapter);
        dirSelector.setText(dirSelector.getAdapter().getItem(0).toString(), false);

        exportDir = directories[0];

        dirSelector.setOnItemClickListener((parent, view, position, id) -> exportDir = directories[position]);

        fileName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                exportFile = editable.toString();
                fileNameInputLayout.setErrorEnabled(false);

                if (exportFile.isEmpty()) {
                    fileNameInputLayout.setError(getString(R.string.err_missing_file_name));

                    fileNameValid = false;
                } else if (!NativeDbHelper.canWriteFile(exportDir + '/' + exportFile + "." + fileExtension)) {
                    fileNameInputLayout.setError("=Access to this file was denied=");

                    fileNameValid = false;
                } else {
                    fileNameValid = true;
                }

                isValid(dialog);
            }
        });

        fileName.setText(exportFile);

        if (showPeriodic) {
            buildScheduledExportViews(dialog);
        }

        isValid(dialog);

        return dialog;
    }

    private void buildScheduledExportViews(AlertDialog dialog) {
        ArrayAdapter<String> frequencyOptions;
        ArrayList<String> timeUnits = new ArrayList<>();
        MaterialAutoCompleteTextView frequencyDropDown = dialog.findViewById(R.id.export_unit);
        TextInputLayout frequencyUnitLayout = dialog.findViewById(R.id.export_frequency_unit_layout);
        TextInputLayout dateLayout = dialog.findViewById(R.id.start_date_layout);
        TextInputEditText dateEntry = dialog.findViewById(R.id.start_date);
        TextInputLayout timeLayout = dialog.findViewById(R.id.start_time_layout);
        TextInputEditText timeEntry = dialog.findViewById(R.id.start_time);
        TextInputLayout frequencyLayout = dialog.findViewById(R.id.export_frequency_layout);
        TextInputEditText frequencyInput = dialog.findViewById(R.id.export_frequency_value);

        String exportStart = preferences.getString(EXPORT_START, "");

        timeUnits.add(getString(R.string.hours));
        timeUnits.add(getString(R.string.days));
        timeUnits.add(getString(R.string.weeks));

        frequencyOptions = new ArrayAdapter<>(
                dialog.getContext(), android.R.layout.simple_dropdown_item_1line, timeUnits
        );

        frequencyDropDown.setAdapter(frequencyOptions);
        frequencyDropDown.setText(
                frequencyDropDown.getAdapter().getItem(1).toString(),
                false
        );

        frequencyDropDown.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (frequencyInput == null || frequencyInput.getText() == null) {
                    return;
                }

                String frequency = frequencyInput.getText().toString();

                if (!frequency.isEmpty()) {
                    frequencyInput.setText(frequency);
                }
            }
        });

        timeEntry.setShowSoftInputOnFocus(false);
        timeEntry.setInputType(InputType.TYPE_NULL);

        timeEntry.setOnFocusChangeListener((view, b) -> {
            if (b) {
                DialogFragment dialogFragment = new TimePickerFragment(timeEntry);
                dialogFragment.show(getParentFragmentManager(), null);
            }
        });

        dateEntry.setShowSoftInputOnFocus(false);
        dateEntry.setInputType(InputType.TYPE_NULL);

        dateEntry.setOnFocusChangeListener((view, b) -> {
            if (b) {
                DialogFragment dialogFragment = new SelectDateFragment(dateEntry);
                dialogFragment.show(getParentFragmentManager(), null);
            }
        });

        dateEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                startDate = (LocalDate) dateEntry.getTag();
                dateLayout.setErrorEnabled(false);

                startDateValid = startDate != null && !startDate.isBefore(LocalDate.now());

                if (!startDateValid) {
                    dateLayout.setError(getString(R.string.date_must_be_future));

                    timeEntry.setText(timeEntry.getText());
                }

                isValid(dialog);
            }
        });

        timeEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                startTime = (LocalTime) timeEntry.getTag();
                timeLayout.setErrorEnabled(false);

                startTimeValid = startDateValid
                        && !LocalDateTime.of(startDate, startTime).isBefore(LocalDateTime.now());

                if (!startTimeValid) {
                    timeLayout.setError("=Time must be in the future=");
                }

                isValid(dialog);
            }
        });

        if (!exportStart.isEmpty()) {
            LocalDateTime start = TimeFormatting.stringToLocalDateTime(exportStart);
            String startDate = DateTimeFormatter.ofPattern(
                    preferences.getString(DATE_FORMAT),
                    Locale.getDefault()
            ).format(start);
            String startTime = DateTimeFormatter.ofPattern(
                    preferences.getString(TIME_FORMAT),
                    Locale.getDefault()
            ).format(start);

            dateEntry.setTag(start.toLocalDate());
            dateEntry.setText(startDate);

            timeEntry.setTag(start.toLocalTime());
            timeEntry.setText(startTime);

            timeLayout.setErrorEnabled(false);
            dateLayout.setErrorEnabled(false);

            startTimeValid = true;
            startDateValid = true;
        }

        frequencyInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int selected = timeUnits.indexOf(frequencyDropDown.getText().toString());

                frequencyLayout.setErrorEnabled(false);

                try {
                    frequency = Short.parseShort(s.toString());

                    switch (selected) {
                        case 2:
                            frequency *= 7;
                            break;
                        case 1:
                            frequency *= 24;
                    }

                    frequencyValid = true;
                } catch (Exception e) {
                    frequencyLayout.setError(getString(R.string.invalid_value));

                    frequencyValid = false;
                }

                isValid(dialog);
            }
        });

        frequency = preferences.getInt(EXPORT_FREQUENCY, -1);

        if (frequency != -1) {
            int displayFreq = frequency;
            frequencyDropDown.setText(timeUnits.get(0), false);

            if (frequency % (24 * 7) == 0) {
                displayFreq = frequency / (24 * 7);
                frequencyDropDown.setText(timeUnits.get(2), false);
            } else if (frequency % 24 == 0) {
                displayFreq = frequency / 24;
                frequencyDropDown.setText(timeUnits.get(1), false);
            }

            frequencyInput.setText(String.valueOf(displayFreq));

            frequencyValid = true;
        }
    }

    private void onExportClick() {
        if (!(getActivity() instanceof IDialogCloseListener)) {
            return;
        }

        String path = exportDir + '/' + exportFile + "." + fileExtension;

        if (showPeriodic) {
            Bundle options = new Bundle();
            String time = TimeFormatting.localDateTimeToDbString(LocalDateTime.of(startDate, startTime));

            options.putString(EXPORT_FILE_NAME, path);
            options.putInt(EXPORT_FREQUENCY, frequency);
            options.putString(EXPORT_START, time);
            options.putBoolean("CREATE_NOW", createNow);

            ((IDialogCloseListener) getActivity()).handleDialogClose(
                    IDialogCloseListener.Action.ADD,
                    options
            );

            return;
        }

        ((IDialogCloseListener) getActivity()).handleDialogClose(
            IDialogCloseListener.Action.CREATE,
            path
        );
    }

    private void onStopClick() {
        if (getActivity() instanceof IDialogCloseListener) {
            ((IDialogCloseListener) getActivity()).handleDialogClose(
                    IDialogCloseListener.Action.DELETE, null
            );
        }
    }

    private void isValid(AlertDialog dialog) {
        boolean valid = fileNameValid;

        if (showPeriodic) {
            valid = valid
                    && startDateValid
                    && startTimeValid
                    && frequencyValid;
        }

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(valid);
    }
}
