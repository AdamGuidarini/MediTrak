package projects.medicationtracker.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.function.Consumer;
import java.util.function.Predicate;

import kotlin.jvm.internal.Lambda;

public class ConfirmationDialog extends DialogFragment {
    private String title;
    private String message;
    private Consumer<Boolean> closeAction;
    private String positiveButtonText;
    private String negativeButtonText;

    /**
     *
     * @param title Dialog title
     * @param message Dialog message
     * @param closeAction Action performed when dialog is closed
     * @param positiveButtonText Text for positive button
     * @param negativeButtonText Text for negative button
     */
    public ConfirmationDialog(
            String title,
            String message,
            Consumer<Boolean> closeAction,
            String positiveButtonText,
            String negativeButtonText
    ) {
        this.title = title;
        this.message = message;
        this.closeAction = closeAction;
        this.positiveButtonText = positiveButtonText;
        this.negativeButtonText = negativeButtonText;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setPositiveButton(
                positiveButtonText,
                (dialogInterface, i) -> onClose(true)
        );
        builder.setNegativeButton(
                negativeButtonText,
                (dialogInterface, i) -> onClose(false)
        );

        builder.setTitle(title);
        builder.setMessage(message);

        dialog = builder.create();
        dialog.show();

        return dialog;
    }

    private void onClose(boolean action) {
        closeAction.accept(action);
        dismiss();
    }
}
