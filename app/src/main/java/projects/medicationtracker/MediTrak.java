package projects.medicationtracker;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NativeDbHelper;

public class MediTrak extends Application implements Configuration.Provider {

    public static String DATABASE_PATH;

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build();
    }
}
