package projects.medicationtracker;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;
import static projects.medicationtracker.TimeFormatting.*;

public class NotificationHelper
{
    private static final String TITLE = "Medication Tracker";
    private final Context notificationContext;
    private final Medication medication;
    private final DBHelper db;
    private static PendingIntent alarmIntent;
    public static int ALARM_TYPE_RTC = 100;
    public static AlarmManager alarmManager;

    NotificationHelper(Context context, Medication med)
    {
        notificationContext = context;
        medication = med;
        db = new DBHelper(context);
    }

    public void scheduleNotification (android.app.Notification notification)
    {
//        String doseTime = LocalDateTimeToString(medication.getStartDate());
//        int id = db.getDoseId(medication.getMedId(), doseTime);
//
//        Intent notificationIntent = new Intent(notificationContext, NotificationReceiver.class);
//        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, id);
//        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);
//
//        alarmIntent = PendingIntent.getBroadcast(notificationContext, 0,
//                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        ZonedDateTime zdt = medication.getStartDate().atZone(ZoneId.systemDefault());
//        int timeInMillis = (int) zdt.toInstant().toEpochMilli();
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(Calendar.DATE, timeInMillis );
//
//        alarmManager = (AlarmManager) notificationContext.getSystemService(ALARM_SERVICE);
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),
//                1000 * 60 * medication.getMedFrequency(), alarmIntent);
    }

    public String createMedicationReminderMessage ()
    {
        String message;
        String patientName = medication.getPatientName();
        String medicationName = medication.getMedName();

        if (patientName.equals("ME!"))
        {
            if (medicationName.toLowerCase().equals("vitamin d"))
                // Inside joke with a potential tested who repeatedly asked me
                // "did you take your vitamin D?" after I started taking it.
                message = "Did you take your Vitamin D?";
            else
                message = "It's time to take your " + medicationName;
        }
        else
            message = "It's time for " + patientName + "'s " + medicationName;

        return message;
    }

    public static void cancelAlarmRTC() {
        if (alarmManager != null) {
            alarmManager.cancel(alarmIntent);
        }
    }

    public static NotificationManager getNotificationManager(Context context)
    {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
