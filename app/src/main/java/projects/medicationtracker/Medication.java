package projects.medicationtracker;

import java.time.LocalDateTime;

public class Medication implements Cloneable
{

    private String medName;
    private String medDosageUnits;
    private String patientName;
    private LocalDateTime startDate;
    private String alias;
    private LocalDateTime[] times;
    private int medId;
    private int medFrequency;
    private int medDosage;

    public Medication(String thisMed, String patient, String units, LocalDateTime[] time,
                      LocalDateTime firstDate, int id, int frequency, int dosage, String medAlias)
    {
        medName = thisMed;
        patientName = patient;
        medDosageUnits = units;
        times = time;
        medId = id;
        medFrequency = frequency;
        startDate = firstDate;
        medDosage = dosage;
        alias = medAlias;
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
    public LocalDateTime getStartDate()
    {
        return startDate;
    }
    public String getAlias()
    {
        return alias;
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
    public void setStartDate(LocalDateTime startDate)
    {
        this.startDate = startDate;
    }
    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Medication clone = null;
        try
        {
            clone = (Medication) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
        return clone;
    }
}
