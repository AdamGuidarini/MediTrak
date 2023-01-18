package projects.medicationtracker.Helpers;

import android.view.ViewGroup;
import android.widget.TextView;

public class TextViewUtils
{
    /**
     * Sets predetermined parameters for a given TextView
     * enabling a consistent style across the app
     * @param textView The TextView to edit
     * @param text Text to display
     * @param parent The view to will hold the TextView
     */
    public static void setTextViewParams(TextView textView, String text, ViewGroup parent)
    {
        textView.setText(text);

        setTextViewFontAndPadding(textView);

        if (parent != null)
            parent.addView(textView);
    }

    /**
     * Sets predetermined padding to TextView
     * @param textView The TextView whose padding to set
     */
    public static void setTextViewFontAndPadding(TextView textView)
    {
        textView.setPadding(20, 24, 0, 24);
        textView.setTextSize(15);
    }
}
