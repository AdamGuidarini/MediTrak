package projects.medicationtracker.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import projects.medicationtracker.Interfaces.IDialogCloseListener;
import projects.medicationtracker.R;

public class AdjustRemainingDosesDialog extends DialogFragment {
    private Integer amountAdjustment = 0;
    private int currentAmount;

    public AdjustRemainingDosesDialog(int amount) {
        currentAmount = amount;
    }

    /**
     * Sets up dialog
     * @param savedInstances The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return instance of dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstances) {
        AlertDialog dialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_adjust_remaining_doses, null));
        builder.setTitle("=Adjust Quantity=");

        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> adjustDose());
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dismiss());
        builder.setNeutralButton(R.string.clear, (dialogInterface, i) -> {
            amountAdjustment = null;

            adjustDose();
        });

        dialog = builder.create();
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        TextInputLayout amountLayout = dialog.findViewById(R.id.amount_layout);
        TextInputEditText amountChanged = dialog.findViewById(R.id.amount);
        TextView newTotal = dialog.findViewById(R.id.new_amount);

        amountChanged.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                amountLayout.setErrorEnabled(false);
                newTotal.setText(null);

                if (intIsParsable(editable.toString())) {
                    amountAdjustment = Integer.parseInt(editable.toString());
                } else {
                    amountLayout.setError(getString(R.string.err_value_too_large));
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

                    return;
                }

                if (amountAdjustment < 0 && (amountAdjustment + currentAmount < 0)) {
                    amountLayout.setError("=cannot reduce by more than the current quantity=");
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

                    return;
                }

                newTotal.setText(
                        getString(R.string.new_quantity, (amountAdjustment + currentAmount))
                );

                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            }
        });

        return dialog;
    }

    /**
     * Determines if a string can be parsed to int
     *
     * @param intToParse String to try to convert
     * @return True if the string can be converted, else false
     */
    private boolean intIsParsable(String intToParse) {
        try {
            Integer.parseInt(intToParse);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Signal parent Activity to update dose
     */
    void adjustDose() {
        if (!(getActivity() instanceof IDialogCloseListener)) {
            return;
        }

        IDialogCloseListener dialogCloseListener = (IDialogCloseListener) getActivity();

        dialogCloseListener.handleDialogClose(IDialogCloseListener.Action.EDIT, amountAdjustment);

        dismiss();
    }
}
