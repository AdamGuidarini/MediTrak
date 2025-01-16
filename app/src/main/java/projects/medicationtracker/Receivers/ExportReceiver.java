package projects.medicationtracker.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NativeDbHelper;

public class ExportReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NativeDbHelper db = new NativeDbHelper(context);
        Bundle prefs = db.getSettings();



        System.out.println("I got a message!");
    }
}
