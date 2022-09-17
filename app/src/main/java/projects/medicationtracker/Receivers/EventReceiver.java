package projects.medicationtracker.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.NotificationHelper;
import projects.medicationtracker.SimpleClasses.Medication;

public class EventReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        final DBHelper db = new DBHelper(context);
        ArrayList<Medication> medications = db.getMedications();

        for (final Medication medication : medications)
        {
            prepareNotification(context, db, medication);
        }
    }

    /**
     * Prepares pending notifications on boot
     * @param context Notification context
     * @param db DBHelper containing medication data
     * @param medication Medication in need of a notification
     */
    private void prepareNotification(Context context, DBHelper db, Medication medication)
    {
        LocalTime[] times = db.getMedicationTimes(medication.getMedId());
        LocalDate startDate = medication.getStartDate().toLocalDate();
        long[] timeIds = db.getMedicationTimeIds(medication);

        for (int i = 0; i < times.length; i++)
        {
            long notificationId = times.length > 1 ? timeIds[i] * -1 : medication.getMedId();

            NotificationHelper.scheduleNotification(
                    context,
                    medication,
                    LocalDateTime.of(startDate, times[i]),
                    notificationId
            );
        }
    }
}
