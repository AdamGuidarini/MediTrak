package projects.medicationtracker.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import projects.medicationtracker.R;

public class OpenNotificationsDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        AlertDialog filterDialog;

        builder.setView(inflater.inflate(R.layout.dialog_open_notifications, null));
        builder.setTitle(R.string.open_notifications);

        builder.setPositiveButton(R.string.apply, ((dialog, which) -> dismiss()));
        builder.setNegativeButton(R.string.cancel, ((dialog, which) -> dismiss()));
        builder.setNeutralButton(R.string.clear, ((dialog, which) -> dismiss()));

        filterDialog = builder.create();
        filterDialog.show();

        return builder.create();
    }
}
