package projects.medicationtracker.Helpers;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TimeFormatting {

    /**
     * Formats time to store in database
     *
     * @param hour   Hour to store
     * @param minute minute to store
     * @return A containing the date and time to be stored in database
     */
    public static String formatTimeForDB(int hour, int minute) {
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

    /**
     * Converts a String to LocalDateTime
     *
     * @param date A string containing a date
     * @return LocalDateTime with date seen in String
     */
    public static LocalDateTime stringToLocalDateTime(String date) {
        final String pattern = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault());

        // Handle bug where seconds < 10 were sorted as 1 digit
        if (date.length() < pattern.length()) {
            date += "0";
        }

        LocalDateTime dateTime;

        dateTime = LocalDateTime.parse(date, formatter);

        return dateTime;
    }

    /**
     * Converts a LocalDateTime to a String, this String can be stored the database
     *
     * @param localDateTime The time to be converted to String
     * @return String containing value of the LocalDateTime
     */
    public static String localDateTimeToString(LocalDateTime localDateTime) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(localDateTime);
    }

    /**
     * Converts time in minutes to weeks, days, and minutes
     *
     * @param minutes Time in minutes
     * @return String showing time taken
     */
    public static String freqConversion(long minutes) {
        boolean containsWeeks = false;
        boolean containsDays = false;
        boolean containsHours = false;

        String conversion = "";

        if (minutes >= 10080) {
            int weeks = 0;

            for (; minutes >= 10080; minutes -= 10080)
                weeks++;

            conversion = weeks + " week";
            if (weeks > 1) conversion += "s";

            containsWeeks = true;
        }

        if (minutes >= 1440) {
            if (containsWeeks)
                conversion += ", ";

            int days = 0;

            for (; minutes >= 1440; minutes -= 1440)
                days++;

            conversion += days + " day";
            if (days > 1) conversion += "s";

            containsDays = true;
        }

        if (minutes >= 60) {
            if (containsDays)
                conversion += ", ";

            int hours = 0;

            for (; minutes >= 60; minutes -= 60)
                hours++;

            conversion += hours + " hour";
            if (hours > 1) conversion += "s";

            containsHours = true;
        }

        if (minutes > 0) {
            if (containsHours)
                conversion += ", ";

            conversion += minutes + " minute";
            if (minutes > 1) conversion += "s";
        }

        return conversion;
    }

    public static LocalDate whenIsSunday(LocalDate now) {
        LocalDate thisSunday;
        if (now == null)
            now = LocalDate.now(Clock.systemDefaultZone());

        if (now.getDayOfWeek() == DayOfWeek.SUNDAY)
            thisSunday = now.minusDays(0);
        else if (now.getDayOfWeek() == DayOfWeek.MONDAY)
            thisSunday = now.minusDays(1);
        else if (now.getDayOfWeek() == DayOfWeek.TUESDAY)
            thisSunday = now.minusDays(2);
        else if (now.getDayOfWeek() == DayOfWeek.WEDNESDAY)
            thisSunday = now.minusDays(3);
        else if (now.getDayOfWeek() == DayOfWeek.THURSDAY)
            thisSunday = now.minusDays(4);
        else if (now.getDayOfWeek() == DayOfWeek.FRIDAY)
            thisSunday = now.minusDays(5);
        else
            thisSunday = now.minusDays(6);

        return thisSunday;
    }
}
