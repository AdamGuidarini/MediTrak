package projects.medicationtracker;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class Medication implements Cloneable
{

    private String medName;
    private String medDosageUnits;
    private String patientName;
    private LocalDateTime startDate;
    private String alias;
    private LocalDateTime[] times;
    private long medId;
    private long medFrequency;
    private int medDosage;

    /**
     * Creates a new object of type Medication
     *
     * @param thisMed The name of this medication
     * @param patient The name of the patient taking this medication
     * @param units The units for the medication's dosage
     * @param time The times at which the medication should be taken
     * @param firstDate The first date the medication should be taken
     * @param id The id for the Medication (from database)
     * @param frequency How often the medication should be taken
     * @param dosage How much of the medication should be taken
     * @param medAlias An alias for the Medication to appear in notifications
     *************************************************************************/
    public Medication(String thisMed, String patient, String units, LocalDateTime[] time,
                      LocalDateTime firstDate, long id, long frequency, int dosage, String medAlias)
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

    /**
     * Returns Medication ID
     * @return Medication ID
     *************************************************************************/
    public long getMedId() { return medId; }

    /**
     * Returns name of Medication
     * @return Name of Medication
     *************************************************************************/
    public String getMedName() { return medName; }

    /**
     * Returns Medication frequency
     * @return Medication frequency
     *************************************************************************/
    public long getMedFrequency() { return medFrequency; }

    /**
     * Returns Medication dosage
     * @return Medication dosage
     *************************************************************************/
    public int getMedDosage() { return medDosage; }

    /**
     * Returns the Medication's dosage
     * @return Medication dosage units
     *************************************************************************/
    public String getMedDosageUnits() { return medDosageUnits; }

    /**
     * Returns the times the medication is to be taken
     * @return Medication's times
     *************************************************************************/
    public LocalDateTime[] getTimes() { return times; }

    /**
     * Returns the patient's name
     * @return Name of the patient
     *************************************************************************/
    public String getPatientName() { return patientName; }

    /**
     * Returns the Medication's start date
     * @return Start date of medication
     *************************************************************************/
    public LocalDateTime getStartDate() { return startDate; }

    /**
     * Returns the alias for this Medication
     * @return Alias of medication
     *************************************************************************/
    public String getAlias() { return alias; }

    // Setters
    /**
     * Set Medication ID
     * @param medId The Medication's ID
     *************************************************************************/
    public void setMedId(long medId) { this.medId = medId; }

    /**
     * Set the name of the Medication
     * @param medName The name of the Medication
     *************************************************************************/
    public void setMedName(String medName) { this.medName = medName; }

    /**
     * Set the Medication frequency
     * @param medFrequency Frequency of the medication
     *************************************************************************/
    public void setMedFrequency(long medFrequency) { this.medFrequency = medFrequency; }

    /**
     * Set dosage of the medication
     * @param medDosage dosage of Medication
     *************************************************************************/
    public void setMedDosage(int medDosage) { this.medDosage = medDosage; }

    /**
     * Set dosage of Medication
     * @param medDosageUnits Dosage of the Medication
     */
    public void setMedDosageUnits(String medDosageUnits) { this.medDosageUnits = medDosageUnits; }

    /**
     * Set times for Medication
     * @param times The array of times for this medication
     *************************************************************************/
    public void setTimes(LocalDateTime[] times) { this.times = times; }

    /**
     * Set name of the patient
     * @param patientName Patient's name
     *************************************************************************/
    public void setPatientName(String patientName) { this.patientName = patientName; }

    /**
     * Set start date for Medication
     * @param startDate Date of first
     *************************************************************************/
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    /**
     * Set the Medication's Alias
     * @param alias The Alias of the Medication
     *************************************************************************/
    public void setAlias(String alias) { this.alias = alias; }

    /**
     * Enables cloning of a Medication
     * @return Clone of Medication
     * @throws CloneNotSupportedException
     *************************************************************************/
    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
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
