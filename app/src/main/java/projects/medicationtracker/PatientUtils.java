package projects.medicationtracker;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;

import static java.time.temporal.TemporalAdjusters.previous;

public class PatientUtils
{
    // Returns number of patients in database
    public static int numPatients (ArrayList<Medication> medications)
    {
        HashSet<String> patients = new HashSet<>();

        // Iterate through each Medication instance and patientName to the HashSet
        for (int i = 0; i < medications.size(); i++)
            patients.add(medications.get(i).getPatientName());

        return patients.size();
    }

    public static ArrayList<String> getPatientNames (ArrayList<Medication> medications)
    {
        ArrayList<String> patients = new ArrayList<>();

        for (int i = 0; i < medications.size(); i++)
        {
            String thisPatient = medications.get(i).getPatientName();
            boolean inList = patients.contains(thisPatient);

            if (!inList)
                patients.add(thisPatient);
        }

        if (patients.contains("ME!"))
        {
            for (int j = 0; j < patients.size(); j++)
            {
                if (patients.get(j).equals("ME!"))
                    patients.set(j, "You");
            }
        }

        return patients;
    }

    // Creates a list of Medications to be taken in the current week
    public static ArrayList<Medication> medicationsForThisWeek(DBHelper db)
    {
        ArrayList<Medication> medications = db.getMedications();

        // Add times to custom frequency
        LocalDate thisSunday = LocalDate.now().with(previous(DayOfWeek.SUNDAY));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
                int frequency = medications.get(i).getMedFrequency();

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
