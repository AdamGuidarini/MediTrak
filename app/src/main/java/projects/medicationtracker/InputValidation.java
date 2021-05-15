package projects.medicationtracker;

import android.widget.EditText;
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

    /**
     * Checks a String for invalid chars and sets a warning if found
     * @param editText EditText to check
     * @param unWantedChars characters to check for
     * @param warning Warning to display if wrong character is found
     * @return False if bad character is found, true if not found
     **************************************************************************/
    public static boolean checkEditText(EditText editText, char[] unWantedChars, String warning)
    {
        String text = editText.getText().toString();

        for (int i = 0; i < text.length(); i++)
        {
            for (char unWantedChar : unWantedChars)
            {
                if (text.charAt(i) == unWantedChar)
                {
                    editText.setError(warning);
                    return false;
                }
            }
        }

        return true;
    }
}
