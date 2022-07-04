package projects.medicationtracker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public class PatientUtils
{
    /**
     * Creates an ArrayList of Medications to be taken this week
     * @param db The DBHelper to draw data from
     * @return List of all Medications for this week
     **************************************************************************/
    public static ArrayList<Medication> medicationsForThisWeek(DBHelper db, LocalDate dateInSoughtWeek)
    {
        ArrayList<Medication> medications = db.getMedications();

        // Add times to custom frequency
        LocalDate thisSunday = TimeFormatting.whenIsSunday(dateInSoughtWeek);

        // Look at each medication
        for (int i = 0; i < medications.size(); i++)
        {
            LocalDateTime[] timeArr;

            // If a medication is taken once per day
            if (medications.get(i).getTimes().length == 1 && medications.get(i).getMedFrequency() == 1440)
            {
                // if the Medication is taken once per day just add the start of each date to
                timeArr = new LocalDateTime[7];
                LocalTime localtime = medications.get(i).getTimes()[0].toLocalTime();

                for (int j = 0; j < 7; j++)
                    timeArr[j] = LocalDateTime.of(LocalDate.from(thisSunday.plusDays(j)), localtime);

                medications.get(i).setTimes(timeArr);
            }
            // If a medication is taken multiple times per day
            else if (medications.get(i).getTimes().length > 1 && medications.get(i).getMedFrequency() == 1440)
            {
                int numberOfTimes = medications.get(i).getTimes().length;
                int index = 0;

                timeArr = new LocalDateTime[numberOfTimes * 7];
                LocalTime[] drugTimes = new LocalTime[numberOfTimes];

                for (int j = 0; j < numberOfTimes; j++)
                    drugTimes[j] = medications.get(i).getTimes()[j].toLocalTime();

                for (int j = 0; j < 7; j++)
                {
                    for (int y = 0; y < numberOfTimes; y++)
                    {
                        timeArr[index] = LocalDateTime.of(LocalDate.from(thisSunday.plusDays(j)), drugTimes[y]);
                        index++;
                    }
                }

                medications.get(i).setTimes(timeArr);
            }
            // If a medication has a custom frequency, take its start date and calculate times for
            // for this week
            else
            {
                LocalDateTime timeToCheck = medications.get(i).getStartDate();
                ArrayList<LocalDateTime> times = new ArrayList<>();
                long frequency = medications.get(i).getMedFrequency();

                while (timeToCheck.toLocalDate().isBefore(thisSunday))
                    timeToCheck = timeToCheck.plusMinutes(frequency);

                while (timeToCheck.toLocalDate().isBefore(thisSunday.plusDays(7)))
                {
                    times.add(timeToCheck);
                    timeToCheck = timeToCheck.plusMinutes(frequency);
                }

                timeArr = new LocalDateTime[times.size()];

                for (int j = 0; j < times.size(); j++)
                    timeArr[j] = times.get(j);

                medications.get(i).setTimes(timeArr);
            }
        }

        return medications;
    }
}
