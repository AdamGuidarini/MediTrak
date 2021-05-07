package projects.medicationtracker;

import android.widget.RadioButton;
import android.widget.RadioGroup;

public class InputValidation
{
    /**
     * Determines which button in RadioGroup is selected
     * @param group RadioGroup to check for selected button in
     * @return Selected RadioButton or null if none selected
     **************************************************************************/
    public static RadioButton checkRadioGroup(RadioGroup group)
    {
        int numChildren = group.getChildCount();
        RadioButton[] buttons = new RadioButton[numChildren];

        for (int i = 0; i < numChildren; i++)
            buttons[i] = (RadioButton) group.getChildAt(i);

        for (int i = 0; i < numChildren; i++)
        {
            if (buttons[i].isChecked())
                return buttons[i];
        }

        return null;
    }
}
