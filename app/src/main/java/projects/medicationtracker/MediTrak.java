package projects.medicationtracker;

import static projects.medicationtracker.Helpers.DBHelper.DATABASE_NAME;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NativeDbHelper;

public class MediTrak extends Application implements Configuration.Provider {

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build();
    }
}
