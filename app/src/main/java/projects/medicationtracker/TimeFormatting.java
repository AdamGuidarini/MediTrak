package projects.medicationtracker;

import android.icu.text.SimpleDateFormat;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class TimeFormatting
{
    /**
     * Formats time for presentation to user
     * @param hour Hour to be displayed
     * @param minute The minute to display
     * @return A string containing the time to display to the user
     **************************************************************************/
    public static String formatTimeForUser(int hour, int minute)
    {
        String chosenTime;
        String min;
        String amOrPm;

        if (hour >= 12 && hour < 24)
        {
            if (hour > 12)
                chosenTime = String.valueOf(hour - 12);
            else
                chosenTime = String.valueOf(hour);

            amOrPm = " PM";
        }
        else
        {
            if (hour < 12 && hour != 0)
                chosenTime = String.valueOf(hour);
            else
                chosenTime = "12";

            amOrPm = " AM";
        }

        if (minute < 10)
            min = "0" + minute;
        else
            min = String.valueOf(minute);

        return chosenTime + ":" + min + amOrPm;
    }

    /**
     * Formats time to store in database
     * @param hour Hour to store
     * @param minute minute to store
     * @return A containing the date and time to be stored in database
     **************************************************************************/
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

    public static String localDateToString(LocalDate localDate)
    {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.getDefault());
        return dateFormat.format(localDate);
    }

    /**
     * Sets chosen current date and time to 2 TextViews and stores datetime for
     * database in the tag of TextView that will display the date.
     * @param date Current date
     * @param time Current time
     **************************************************************************/
    public static void getCurrentTimeAndDate(TextView date, TextView time)
    {
        String dateForUser;
        String timeForUser;
        String dateTime;

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateForDb = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        Date myDate = new Date();
        dateForUser = dateFormat.format(myDate);
        timeForUser = dateFormat1.format(myDate);
        dateTime = dateForDb.format(myDate);

        date.setTag(dateTime);
        date.setText(dateForUser);
        time.setText(timeForUser);
    }

    /**
     * Converts a String to LocalDateTime
     * @param date A string containing a date
     * @return LocalDateTime with date seen in String
     **************************************************************************/
    public static LocalDateTime stringToLocalDateTime (String date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());

        LocalDateTime dateTime;

        dateTime = LocalDateTime.parse(date, formatter);

        return dateTime;
    }

    /**
     * Converts a LocalDateTime to a String, this String can be stored the database
     * @param localDateTime The time to be converted to String
     * @return String containing value of the LocalDateTime
     **************************************************************************/
    public static String LocalDateTimeToString (LocalDateTime localDateTime)
    {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(localDateTime);
    }

    /**
     * Converts a LocalTime to a String in 12-hour format
     * @param time Time to convert
     * @return A String of the time passed to it in 12-hour format
     **************************************************************************/
    public static String localTimeToString(LocalTime time)
    {
        int hour = time.getHour();
        int minute = time.getMinute();
        return formatTimeForUser(hour, minute);
    }

    /**
     * Converts time in minutes to weeks, days, and minutes
     * @param minutes Time in minutes
     * @return String showing time taken
     **************************************************************************/
    public static String freqConversion(int minutes)
    {
        boolean containsWeeks = false;
        boolean containsDays = false;
        boolean containsHours = false;

        String conversion = "";

        if (minutes >= 10080)
        {
            int weeks = 0;

            for (; minutes >= 10080; minutes -= 10080)
                weeks++;

            conversion = weeks + " week";
            if (weeks > 1) conversion += "s";

            containsWeeks = true;
        }

        if (minutes >= 1440)
        {
            if (containsWeeks)
                conversion += ", ";

            int days = 0;

            for (; minutes >= 1440; minutes -= 1440)
                days++;

            conversion += days + " day";
            if (days > 1)    conversion += "s";

            containsDays = true;
        }

        if (minutes >= 60)
        {
            if (containsDays)
                conversion += ", ";

            int hours = 0;

            for (; minutes >= 60; minutes -= 60)
                hours++;

            conversion += hours + " hour";
            if (hours > 1)  conversion += "s";

            containsHours = true;
        }

        if (minutes > 0)
        {
            if (containsHours)
                conversion += ", ";

            conversion += minutes + " minute";
            if (minutes > 1) conversion += "s";
        }

        return conversion;
    }
}
