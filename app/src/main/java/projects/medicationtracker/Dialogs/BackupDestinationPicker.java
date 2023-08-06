package projects.medicationtracker.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.util.ArrayList;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.R;

public class BackupDestinationPicker extends DialogFragment {
    private String exportDir;
    private String exportFile;
    private MaterialAutoCompleteTextView dirSelector;
    private TextInputEditText fileName;

    @Override
    public Dialog onCreateDialog(Bundle savedInstances) {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

        dialog = builder.create();
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        dirs.add(getString(R.string.downloads));
        dirs.add(getString(R.string.documents));

        directories = new String[] {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath()
        };

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, dirs);

        dirSelector = dialog.findViewById(R.id.export_dir);
        fileName = dialog.findViewById(R.id.export_file);

        dirSelector.setAdapter(adapter);

        dirSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                exportDir = directories[position];

                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(evaluate());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        exportFile = "meditrak_"
                + now.getYear() + "_"
                + now.getMonthValue() + "_"
                + now.getDayOfMonth() + ".json";

        fileName.setText(exportFile);

        // TODO ADD TEXT WATCHER TO FILE NAME

        return dialog;
    }

    @Override
    public void onStart() {
        final String[] directories;
        ArrayAdapter<String> adapter;
        ArrayList<String> dirs = new ArrayList<>();

        super.onStart();

        dirs.add(getString(R.string.downloads));
        dirs.add(getString(R.string.documents));

        directories = new String[] {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath()
        };

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, dirs);

        dirSelector = getDialog().findViewById(R.id.export_dir);
        fileName = getDialog().findViewById(R.id.export_file);

        dirSelector.setAdapter(adapter);

        dirSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                exportDir = directories[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

        private void onExportClick() {
        boolean res = DbManager(DBHelper.DATABASE_NAME, exportDir + '/' + exportFile);
    }

    private boolean evaluate() {
        return  exportFile != null && exportDir != null;
    }

    public native boolean DbManager(String databaseName, String exportDirectory);
}
