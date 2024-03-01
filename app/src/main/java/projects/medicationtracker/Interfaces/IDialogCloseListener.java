package projects.medicationtracker.Interfaces;

/**
 * Interface for handling response from dialogs. Should be implement be activity calling dialog.
 */
public interface IDialogCloseListener {
    /**
     * Actions a dialog can return
     */
    enum Action {
        ADD,
        EDIT,
        DELETE
    }

    /**
     * Method for handling dialog response.
     * @param action Action performed in dialog
     * @param data Object returned by dialog
     */
    void handleDialogClose(Action action, Object data);
}
