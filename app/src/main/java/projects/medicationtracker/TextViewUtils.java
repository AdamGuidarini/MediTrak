package projects.medicationtracker;

import android.widget.LinearLayout;
import android.widget.TextView;

public class TextViewUtils
{
    public static void setTextViewParams(TextView textView, String text, LinearLayout parent)
    {
        textView.setText(text);

        setTextViewFontAndPadding(textView);

        if (parent != null)
            parent.addView(textView);
    }

    public static void setTextViewFontAndPadding(TextView textView)
    {
        textView.setPadding(20, 24, 0, 24);
        textView.setTextSize(15);
    }
}
