package projects.medicationtracker;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

public class BootService extends IntentService
{
    public BootService() {
        super("BootService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {

    }
}
