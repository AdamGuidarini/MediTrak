package projects.medicationtracker.SimpleClasses;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;

public class Medication implements Cloneable, Parcelable
{

    private String medName;
    private String medDosageUnits;
    private String patientName;
    private LocalDateTime startDate;
    private String alias;
    private LocalDateTime[] times;
    private long medId;
    private long medFrequency;
    private float medDosage;
    private boolean active;

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
     */
    public Medication(String thisMed, String patient, String units, LocalDateTime[] time,
                      LocalDateTime firstDate, long id, long frequency, float dosage, String medAlias)
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

    public Medication()
    {
        medName = "";
        patientName = "";
        medDosageUnits = "";
        times = new LocalDateTime[0];
        medId = -1;
        medFrequency = 0;
        startDate = LocalDateTime.now();
        medDosage = 0;
        alias = "";
    }

    protected Medication(Parcel in) {
        medName = in.readString();
        medDosageUnits = in.readString();
        patientName = in.readString();
        alias = in.readString();
        medId = in.readLong();
        medFrequency = in.readLong();
        medDosage = in.readInt();
    }

    public static final Creator<Medication> CREATOR = new Creator<Medication>() {
        @Override
        public Medication createFromParcel(Parcel in) {
            return new Medication(in);
        }

        @Override
        public Medication[] newArray(int size) {
            return new Medication[size];
        }
    };

    /**
     * Returns Medication ID
     * @return Medication ID
     */
    public long getId() { return medId; }

    /**
     * Returns name of Medication
     * @return Name of Medication
     */
    public String getName() { return medName; }

    /**
     * Returns Medication frequency
     * @return Medication frequency
     */
    public long getFrequency() { return medFrequency; }

    /**
     * Returns Medication dosage
     * @return Medication dosage
     */
    public float getDosage() { return medDosage; }

    /**
     * Returns the Medication's dosage
     * @return Medication dosage units
     */
    public String getDosageUnits() { return medDosageUnits; }

    /**
     * Returns the times the medication is to be taken
     * @return Medication's times
     */
    public LocalDateTime[] getTimes() { return times; }

    /**
     * Returns the patient's name
     * @return Name of the patient
     */
    public String getPatientName() { return patientName; }

    /**
     * Returns the Medication's start date
     * @return Start date of medication
     */
    public LocalDateTime getStartDate() { return startDate; }

    /**
     * Returns the alias for this Medication
     * @return Alias of medication
     */
    public String getAlias() { return alias; }

    /**
     * Returns active status of medication
     */
    public boolean isActive() { return active; }

    // Setters
    /**
     * Set Medication ID
     * @param medId The Medication's ID
     */
    public void setId(long medId) { this.medId = medId; }

    /**
     * Set the name of the Medication
     * @param medName The name of the Medication
     */
    public void setName(String medName) { this.medName = medName; }

    /**
     * Set the Medication frequency
     * @param medFrequency Frequency of the medication
     */
    public void setFrequency(long medFrequency) { this.medFrequency = medFrequency; }

    /**
     * Set dosage of the medication
     * @param medDosage dosage of Medication
     */
    public void setDosage(float medDosage) { this.medDosage = medDosage; }

    /**
     * Set dosage of Medication
     * @param medDosageUnits Dosage of the Medication
     */
    public void setDosageUnits(String medDosageUnits) { this.medDosageUnits = medDosageUnits; }

    /**
     * Set times for Medication
     * @param times The array of times for this medication
     */
    public void setTimes(LocalDateTime[] times) { this.times = times; }

    /**
     * Set name of the patient
     * @param patientName Patient's name
     */
    public void setPatientName(String patientName) { this.patientName = patientName; }

    /**
     * Set start date for Medication
     * @param startDate Date of first
     */
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    /**
     * Set the Medication's Alias
     * @param alias The Alias of the Medication
     */
    public void setAlias(String alias) { this.alias = alias; }

    /**
     * Sets a medication to active or inactive
     */
    public void setActiveStatus(boolean activeStatus) { active = activeStatus; }

    /**
     * Enables cloning of a Medication
     * @return Clone of Medication
     * @throws CloneNotSupportedException
     */
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(medName);
        parcel.writeString(medDosageUnits);
        parcel.writeString(patientName);
        parcel.writeString(alias);
        parcel.writeLong(medId);
        parcel.writeLong(medFrequency);
        parcel.writeFloat(medDosage);
    }
}
