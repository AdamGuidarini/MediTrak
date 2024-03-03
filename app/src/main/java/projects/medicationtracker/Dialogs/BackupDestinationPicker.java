package projects.medicationtracker.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NativeDbHelper;
import projects.medicationtracker.R;

public class BackupDestinationPicker extends DialogFragment {
    private String exportDir;
    private String exportFile;
    private TextInputLayout fileNameInputLayout;
    private NativeDbHelper nativeDb;

    @Override
    public Dialog onCreateDialog(Bundle savedInstances) {
        AlertDialog dialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final String[] directories;
        ArrayAdapter<String> adapter;
        ArrayList<String> dirs = new ArrayList<>();
        LocalDate now = LocalDate.now();

        builder.setView(inflater.inflate(R.layout.dialog_backup_destination_picker, null));

        builder.setTitle(getString(R.string.export_data));
        builder.setPositiveButton(R.string.export, ((dialogInterface, i) -> {
            onExportClick();
            dismiss();
        }));
        builder.setNegativeButton(R.string.cancel, ((dialogInterface, i) -> dismiss()));

        nativeDb = new NativeDbHelper(
                getContext().getDatabasePath(DBHelper.DATABASE_NAME).getAbsolutePath()
        );

        dialog = builder.create();
        dialog.show();

        MaterialAutoCompleteTextView dirSelector = dialog.findViewById(R.id.export_dir);
        fileNameInputLayout = dialog.findViewById(R.id.export_file_layout);
        TextInputEditText fileName = dialog.findViewById(R.id.export_file);

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

        exportFile = "meditrak_"
                + now.getYear() + "_"
                + now.getMonthValue() + "_"
                + now.getDayOfMonth() + ".json";

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

        return dialog;
    }

    private void onExportClick() {
        String resMessage;
        boolean res = nativeDb.dbExport(
                exportDir + '/' + exportFile,
                new String[]{DBHelper.ANDROID_METADATA, DBHelper.SETTINGS_TABLE}
        );

        resMessage = res ? getString(R.string.successful_export, exportDir + '/' + exportFile)
                : getString(R.string.failed_export);

        Toast.makeText(getContext(), resMessage, Toast.LENGTH_SHORT).show();
    }
}
