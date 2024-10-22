package projects.medicationtracker.Dialogs;

import static projects.medicationtracker.Helpers.DBHelper.DATE_FORMAT;
import static projects.medicationtracker.MainActivity.preferences;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
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
    private String[] opts;
    private FilterField<LocalDate>[] filters;

    public FilterDialog(FilterField<LocalDate>[] existingFilters) {
        filters = existingFilters;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
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

        if (filters != null) {
            for (FilterField<LocalDate> f : filters) {
                if (f.getField().equals("TAKEN")) {
                    setExistingFilter(takenBeforeAfter, takenFilterSelector, f);
                } else if (f.getField().equals("SCHEDULED")) {
                    setExistingFilter(scheduledBeforeAfter, scheduledDateSelector, f);
                }
            }
        }

        LinearLayout barrier = filterDialog.findViewById(R.id.barrier);
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

        tv.setText(tv.getAdapter().getItem(0).toString(), false);
    }

    private void applyFilters() {
        ArrayList<FilterField<LocalDate>> filters = new ArrayList<>();
        FilterField<LocalDate>[] filts;

        if (!scheduledDateSelector.getText().toString().isEmpty()) {
            FilterField<LocalDate> filter = new FilterField();

            filter.setField("SCHEDULED");
            filter.setValue((LocalDate) scheduledDateSelector.getTag());

            if (scheduledBeforeAfter.getText().toString().equals(opts[0])) {
                filter.setOption(FilterField.FilterOptions.GREATER_THAN);
            } else if (scheduledBeforeAfter.getText().toString().equals(opts[1])) {
                filter.setOption(FilterField.FilterOptions.LESS_THAN);
            } else {
                filter.setOption(FilterField.FilterOptions.EQUALS);
            }

            filters.add(filter);
        }

        if (!takenFilterSelector.getText().toString().isEmpty()) {
            FilterField<LocalDate> filter = new FilterField<LocalDate>();

            filter.setField("TAKEN");
            filter.setValue((LocalDate) takenFilterSelector.getTag());

            if (takenBeforeAfter.getText().toString().equals(opts[0])) {
                filter.setOption(FilterField.FilterOptions.GREATER_THAN);
            } else if (takenBeforeAfter.getText().toString().equals(opts[1])) {
                filter.setOption(FilterField.FilterOptions.LESS_THAN);
            } else {
                filter.setOption(FilterField.FilterOptions.EQUALS);
            }

            filters.add(filter);
        }

        if (getActivity() instanceof IDialogCloseListener) {
            filts = new FilterField[filters.size()];

            ((IDialogCloseListener) getActivity()).handleDialogClose(
                    IDialogCloseListener.Action.FILTERS_APPLIED,
                    filters.toArray(filts)
            );
        }

        Objects.requireNonNull(getDialog()).dismiss();
    }

    private void filtersCleared() {
        if (getActivity() instanceof IDialogCloseListener) {
            ((IDialogCloseListener) getActivity()).handleDialogClose(
                    IDialogCloseListener.Action.FILTERS_APPLIED, new FilterField[] {}
            );

            scheduledDateSelector.setText("");
            takenFilterSelector.setText("");
        }
    }

    private void setExistingFilter(
            MaterialAutoCompleteTextView autoCompleteTextView,
            TextInputEditText editText,
            FilterField<LocalDate> filterField
    ) {
        String opt = "";
        LocalDate date = filterField.getValue();

        switch (filterField.getOption()) {
            case GREATER_THAN:
                opt = getString(R.string.after);
                break;
            case LESS_THAN:
                opt = getString(R.string.before);
                break;
            case EQUALS:
                opt = getString(R.string.on);
        }

        autoCompleteTextView.setText(opt, false);
        editText.setTag(date);

        String formattedDate = DateTimeFormatter.ofPattern(
                preferences.getString(DATE_FORMAT),
                Locale.getDefault()
        ).format(date);

        editText.setText(formattedDate);
    }
}
