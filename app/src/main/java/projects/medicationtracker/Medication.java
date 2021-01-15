package projects.medicationtracker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Medication
{

    private String medName;
    private String medDosageUnits;
    private String patientName;
    private String startDate;
    private LocalDateTime[] times;
    private int medId;
    private int medFrequency;
    private int medDosage;

    public Medication(String thisMed, String patient, String units, LocalDateTime[] time, String firstDate,
                      int id, int frequency, int dosage)
    {
        medName = thisMed;
        patientName = patient;
        medDosageUnits = units;
        times = time;
        medId = id;
        medFrequency = frequency;
        startDate = firstDate;
        medDosage = dosage;
    }

    // Getters
    public int getMedId()
    {
        return medId;
    }
    public String getMedName()
    {
        return medName;
    }
    public int getMedFrequency()
    {
        return medFrequency;
    }
    public int getMedDosage()
    {
        return medDosage;
    }
    public String getMedDosageUnits()
    {
        return medDosageUnits;
    }
    public LocalDateTime[] getTimes()
    {
        return times;
    }
    public String getPatientName()
    {
        return patientName;
    }
    public String getStartDate()
    {
        return startDate;
    }

    // Setters
    public void setMedId(int medId)
    {
        this.medId = medId;
    }
    public void setMedName(String medName)
    {
        this.medName = medName;
    }
    public void setMedFrequency(int medFrequency)
    {
        this.medFrequency = medFrequency;
    }
    public void setMedDosage(int medDosage)
    {
        this.medDosage = medDosage;
    }
    public void setMedDosageUnits(String medDosageUnits)
    {
        this.medDosageUnits = medDosageUnits;
    }
    public void setTimes(LocalDateTime[] times)
    {
        this.times = times;
    }
    public void setPatientName(String patientName)
    {
        this.patientName = patientName;
    }
    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }
}
