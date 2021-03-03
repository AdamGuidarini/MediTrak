package projects.medicationtracker;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class Notifications extends Activity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);
    }
}
