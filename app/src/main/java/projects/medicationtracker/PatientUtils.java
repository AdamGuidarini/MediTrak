package projects.medicationtracker;

import java.util.ArrayList;
import java.util.HashSet;

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

            if (!inList && !thisPatient.equals("ME!"))
                patients.add(thisPatient);
        }

        return patients;
    }
}
