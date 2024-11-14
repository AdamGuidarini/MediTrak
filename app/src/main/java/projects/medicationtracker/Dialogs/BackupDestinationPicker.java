package projects.medicationtracker.Dialogs;

import static projects.medicationtracker.Helpers.DBHelper.EXPORT_FILE_NAME;
import static projects.medicationtracker.Helpers.DBHelper.EXPORT_FREQUENCY;
import static projects.medicationtracker.Helpers.DBHelper.EXPORT_START;
import static projects.medicationtracker.MainActivity.preferences;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import projects.medicationtracker.Interfaces.IDialogCloseListener;
import projects.medicationtracker.R;

public class BackupDestinationPicker extends DialogFragment {
    private String exportDir;
    private String exportFile;
    private TextInputLayout fileNameInputLayout;
    private final String fileExtension;
    private boolean showPeriodic = false;
    private boolean createNow;
    private int frequency;
    private LocalDateTime startDate;

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
        ArrayAdapter<String> frequencyOptions;
        ArrayList<String> dirs = new ArrayList<>();
        ArrayList<String> timeUnits = new ArrayList<>();

        builder.setView(inflater.inflate(R.layout.dialog_backup_destination_picker, null));

        builder.setTitle(getString(R.string.export_data));
        builder.setPositiveButton(R.string.export, ((dialogInterface, i) -> {
            onExportClick();
            dismiss();
        }));
        builder.setNegativeButton(R.string.cancel, ((dialogInterface, i) -> dismiss()));

        if (preferences.getInt(EXPORT_FREQUENCY, -1) != -1) {
            builder.setNegativeButton("=STOP=", (dialogInterface, i) -> {
                onStopClick();
                dismiss();
            });
        }

        dialog = builder.create();
        dialog.show();

        MaterialAutoCompleteTextView frequencyDropDown = dialog.findViewById(R.id.export_unit);
        MaterialAutoCompleteTextView dirSelector = dialog.findViewById(R.id.export_dir);

        TextInputLayout frequencyUnitLayout = dialog.findViewById(R.id.export_frequency_unit_layout);
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

        fileName.setText(exportFile);

        fileName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                Optional<String> fileNameChecker;
                exportFile = editable.toString();

                fileNameChecker = Optional.of(exportFile)
                        .filter(f -> f.contains("."))
                        .map(f -> f.substring(exportFile.lastIndexOf(".") + 1));

                if (exportFile.isEmpty()) {
                    fileNameInputLayout.setError(getString(R.string.err_missing_file_name));
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

                    return;
                } else if (fileNameChecker.isPresent() && !fileNameChecker.get().equals("json")) {
                    fileNameInputLayout.setError(getString(R.string.err_file_must_be_json));
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

                    return;
                }

                fileNameInputLayout.setErrorEnabled(false);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            }
        });

        timeUnits.add(getString(R.string.minutes));
        timeUnits.add(getString(R.string.hours));
        timeUnits.add(getString(R.string.days));
        timeUnits.add(getString(R.string.weeks));

        frequencyOptions = new ArrayAdapter<>(
                dialog.getContext(), android.R.layout.simple_dropdown_item_1line, timeUnits
        );

        frequencyDropDown.setAdapter(frequencyOptions);

        frequencyDropDown.setOnItemClickListener((adapterView, view, i, l) -> {
            frequencyUnitLayout.setErrorEnabled(false);

            switch (i) {
                case 3:
                    frequency *= 7;
                case 2:
                    frequency *= 24;
                case 1:
                    frequency *= 60;
            }
        });

        return dialog;
    }

    private void onExportClick() {
        if (!(getActivity() instanceof IDialogCloseListener)) {
            return;
        }

        String path = exportDir + '/' + exportFile + "." + fileExtension;

        if (showPeriodic) {
            Bundle options = new Bundle();

            options.putString(EXPORT_FILE_NAME, path);
            options.putInt(EXPORT_FREQUENCY, frequency);
            options.putString(EXPORT_START, startDate.toString());
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
}
