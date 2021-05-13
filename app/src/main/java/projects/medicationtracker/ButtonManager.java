package projects.medicationtracker;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;


public class ButtonManager
{
    public static void createActivityButton(String text, LinearLayout parent, Context context)
    {
        Button button = new Button(context);
        button.setText(text);
        parent.addView(button);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        button.setLayoutParams(layoutParams);

        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                button.setError("Button does nothing yet");
            }
        });
    }
}
