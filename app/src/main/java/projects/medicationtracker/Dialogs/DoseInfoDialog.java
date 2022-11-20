package projects.medicationtracker.Dialogs;

import androidx.fragment.app.DialogFragment;

public class DoseInfoDialog extends DialogFragment
{
    private final long doseId;

    DoseInfoDialog(long doseId)
    {
        this.doseId = doseId;
    }

}
