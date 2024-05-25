package projects.medicationtracker.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import projects.medicationtracker.R;

public class FilterDialog extends DialogFragment {

    private MaterialAutoCompleteTextView personSelect;
    private TextInputEditText scheduledDateSelector;
    private MaterialAutoCompleteTextView scheduledBeforeAfter;
    private TextInputEditText takenFilterSelector;
    private MaterialAutoCompleteTextView takenBeforeAfter;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        AlertDialog filterDialog;

        builder.setView(inflater.inflate(R.layout.dialog_filter, null));
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
        filterDialog.show();

        scheduledDateSelector = filterDialog.findViewById(R.id.scheduled_filter);
        scheduledBeforeAfter = filterDialog.findViewById(R.id.sched_before_after);

        takenFilterSelector = filterDialog.findViewById(R.id.taken_filter);
        takenBeforeAfter = filterDialog.findViewById(R.id.taken_before_after);

        setDateSelectListeners(scheduledDateSelector);
        setDateSelectListeners(takenFilterSelector);

        return filterDialog;
    }

    private void setDateSelectListeners(TextInputEditText editText) {
        editText.setShowSoftInputOnFocus(false);
    }
}
