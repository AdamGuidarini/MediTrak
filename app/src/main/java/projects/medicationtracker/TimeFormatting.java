package projects.medicationtracker;

public class TimeFormatting
{
    public static String formatTime(int hour, int minute)
    {
        String chosenTime = "At: ";
        String min;
        String amOrPm;

        if (hour >= 12)
        {
            chosenTime += String.valueOf(hour - 12);
            amOrPm = " PM";
        }
        else
        {
            if (hour > 0)
                chosenTime += String.valueOf(hour);
            else if (hour == 0)
                chosenTime += "12";
            amOrPm = " AM";
        }

        if (minute < 10)
            min = "0" + minute;
        else
            min = String.valueOf(minute);

        return chosenTime + ":" + min + amOrPm;
    }

    // Returns a string with the hour and formatted for the database
    public static String formatTimeForDB(int hour, int minute)
    {
        String time;

        if (hour < 10)
            time = "0" + hour;
        else
            time = String.valueOf(hour);

        if (minute < 10)
            time += ":0" + minute;
        else
            time += ":" + minute;

        return time + ":00";
    }
}
