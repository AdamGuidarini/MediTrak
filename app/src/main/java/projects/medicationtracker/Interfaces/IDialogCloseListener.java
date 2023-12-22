package projects.medicationtracker.Interfaces;

import projects.medicationtracker.SimpleClasses.Dose;

public interface IDialogCloseListener {
    enum Action {
        ADD,
        EDIT,
        DELETE
    }

    public void handleDialogClose(Action action, Object obj);
}
