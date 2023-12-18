package projects.medicationtracker.Interfaces;

import projects.medicationtracker.SimpleClasses.Dose;

public interface IDialogCloseListener {
    enum Action {
        DELETE,
        ADD
    }

    public void handleDialogClose(Action action, Dose dose);
}
