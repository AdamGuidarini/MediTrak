package projects.medicationtracker;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NativeDbHelper;

public class MediTrak extends Application implements Configuration.Provider {

    public static String DATABASE_PATH;

    @Override
    public void onCreate() {
        super.onCreate();

        DATABASE_PATH = getDatabasePath(DBHelper.DATABASE_NAME).getAbsolutePath();
        NativeDbHelper nativeDb = new NativeDbHelper(DATABASE_PATH);
        nativeDb.create();
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build();
    }
}
