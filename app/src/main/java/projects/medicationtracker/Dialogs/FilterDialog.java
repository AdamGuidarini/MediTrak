package projects.medicationtracker.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDateTime;

import kotlin.Triple;
import projects.medicationtracker.Models.Medication;
import projects.medicationtracker.R;

public class FilterDialog extends DialogFragment {

    private boolean singlePerson;

    public FilterDialog(boolean onePerson) {
        singlePerson = onePerson;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        AlertDialog filterDialog;

        builder.setView(R.layout.dialog_filter);
        builder.setTitle(R.string.filter);

        builder.setPositiveButton(
            "=Apply=",
                ((dialog, which) -> dismiss())
        );
        builder.setNegativeButton(
                R.string.cancel,
                ((dialog, which) -> dismiss())
        );
        builder.setNeutralButton(
                "=Clear=",
                ((dialog, which) -> dismiss())
        );

        filterDialog = builder.create();

        return filterDialog;
    }
}
