package projects.medicationtracker;

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;

public class Medication
{
    private int medId;
    private String medName;
    private int medFrequency;
    private int medDosage;
    private String medDosageUnits;

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


}
