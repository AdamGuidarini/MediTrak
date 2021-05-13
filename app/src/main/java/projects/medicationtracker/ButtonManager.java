package projects.medicationtracker;

import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;


public class ButtonManager
{
    public static void createActivityButton(String text, LinearLayout parent, Context context, Intent intent)
    {
        Button button = new Button(context);
        button.setText(text);
        parent.addView(button);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        button.setLayoutParams(layoutParams);

        button.setOnClickListener(view -> context.startActivity(intent));
    }
}
