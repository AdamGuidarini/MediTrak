package projects.medicationtracker.Interfaces;

import android.content.DialogInterface;

public interface IDialogCloseListener
{
    enum Action {
        DELETE,
        ADD
    }

    public void handleDialogClose(Action action, Long doseId);
}
