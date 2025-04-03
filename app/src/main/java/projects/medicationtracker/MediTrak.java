package projects.medicationtracker;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import java.text.DecimalFormat;

public class MediTrak extends Application implements Configuration.Provider {

    public final static DecimalFormat formatter = new DecimalFormat("0.###");

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build();
    }
}
