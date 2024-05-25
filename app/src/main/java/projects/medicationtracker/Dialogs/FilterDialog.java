package projects.medicationtracker.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

import projects.medicationtracker.Fragments.SelectDateFragment;
import projects.medicationtracker.Interfaces.IDialogCloseListener;
import projects.medicationtracker.Models.FilterField;
import projects.medicationtracker.R;

public class FilterDialog extends DialogFragment {

    private TextInputEditText scheduledDateSelector;
    private MaterialAutoCompleteTextView scheduledBeforeAfter;
    private TextInputEditText takenFilterSelector;
    private MaterialAutoCompleteTextView takenBeforeAfter;
    private LinearLayout barrier;
    private String[] opts;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        AlertDialog filterDialog;

        builder.setView(inflater.inflate(R.layout.dialog_filter, null));
        builder.setTitle(R.string.filter);

        builder.setPositiveButton(R.string.apply, ((dialog, which) -> applyFilters()));
        builder.setNegativeButton(R.string.cancel, ((dialog, which) -> dismiss()));
        builder.setNeutralButton(R.string.clear, ((dialog, which) -> filtersCleared()));

        filterDialog = builder.create();
        filterDialog.show();

        opts = new String[]{getString(R.string.after), getString(R.string.before), getString(R.string.on)};

        scheduledDateSelector = filterDialog.findViewById(R.id.scheduled_filter);
        scheduledBeforeAfter = filterDialog.findViewById(R.id.sched_before_after);

        takenFilterSelector = filterDialog.findViewById(R.id.taken_filter);
        takenBeforeAfter = filterDialog.findViewById(R.id.taken_before_after);

        setDateSelectListeners(scheduledDateSelector);
        setDateSelectListeners(takenFilterSelector);

        setTimeFilterArrayAdapters(takenBeforeAfter);
        setTimeFilterArrayAdapters(scheduledBeforeAfter);

        barrier = filterDialog.findViewById(R.id.barrier);

        barrier.setBackgroundColor(scheduledDateSelector.getCurrentTextColor());

        return filterDialog;
    }

    private void setDateSelectListeners(TextInputEditText editText) {
        editText.setShowSoftInputOnFocus(false);

        editText.setOnFocusChangeListener((v, b) -> {
            if (b) {
                DialogFragment dateSelector = new SelectDateFragment(editText);
                dateSelector.show(getParentFragmentManager(), null);
            }
        });
    }

    private void setTimeFilterArrayAdapters(MaterialAutoCompleteTextView tv) {
        tv.setShowSoftInputOnFocus(false);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            getActivity(),
            android.R.layout.simple_dropdown_item_1line,
            opts
        );

        tv.setAdapter(adapter);

        tv.setText(R.string.after);
    }

    private void applyFilters() {
        ArrayList<FilterField<LocalDate>> filters = new ArrayList<>();

        if (!scheduledDateSelector.getText().toString().isEmpty()) {
            FilterField<LocalDate> filter = new FilterField<>();

            filter.field = "SCHEDULED";
            filter.value = (LocalDate) scheduledDateSelector.getTag();

            if (scheduledBeforeAfter.getText().toString().equals(opts[0])) {
                filter.option = FilterField.FilterOptions.AFTER;
            } else if (scheduledBeforeAfter.getText().toString().equals(opts[1])) {
                filter.option = FilterField.FilterOptions.BEFORE;
            } else {
                filter.option = FilterField.FilterOptions.EQUALS;
            }

            filters.add(filter);
        }

        if (!takenFilterSelector.getText().toString().isEmpty()) {
            FilterField<LocalDate> filter = new FilterField<>();

            filter.field = "SCHEDULED";
            filter.value = (LocalDate) takenFilterSelector.getTag();

            if (takenBeforeAfter.getText().toString().equals(opts[0])) {
                filter.option = FilterField.FilterOptions.AFTER;
            } else if (takenBeforeAfter.getText().toString().equals(opts[1])) {
                filter.option = FilterField.FilterOptions.BEFORE;
            } else {
                filter.option = FilterField.FilterOptions.EQUALS;
            }

            filters.add(filter);
        }

        if (getActivity() instanceof IDialogCloseListener) {
            ((IDialogCloseListener) getActivity()).handleDialogClose(
                    IDialogCloseListener.Action.FILTERS_APPLIED,
                    filters
            );
        }

        Objects.requireNonNull(getDialog()).dismiss();
    }

    private void filtersCleared() {
        if (getActivity() instanceof IDialogCloseListener) {
            ((IDialogCloseListener) getActivity()).handleDialogClose(
                    IDialogCloseListener.Action.FILTERS_APPLIED, null
            );

            scheduledDateSelector.setText("");
            takenFilterSelector.setText("");
        }
    }
}
