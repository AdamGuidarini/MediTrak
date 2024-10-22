package projects.medicationtracker.Models;

import android.util.Log;

import androidx.annotation.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import projects.medicationtracker.Helpers.DBHelper;
import projects.medicationtracker.Helpers.TimeFormatting;

/**
 * A simple class representing a Medication dose
 */
public class Dose {
    private long doseId;
    private boolean taken;
    private long medId;
    private LocalDateTime timeTaken;
    private LocalDateTime doseTime;
    private int overrideDoseAmount = -1;
    private String overrideDoseUnit = "";

    /**
     * Class constructor
     * @param id ID of dose (-1 if no ID)
     * @param medicationId ID of associated medication
     * @param isTaken Whether or not medication is taken
     * @param timeTaken Time when medication was taken or null
     */
    public Dose(long id, long medicationId, boolean isTaken, @Nullable LocalDateTime timeTaken, @Nullable LocalDateTime doseTime) {
        doseId = id;
        medId = medicationId;
        taken = isTaken;
        this.timeTaken = timeTaken;
        this.doseTime = doseTime;
    }

    public Dose(long id, long medicationId, boolean isTaken, @Nullable String timeTaken, @Nullable String doseTime) {
        final String dateFormat = DBHelper.DateFormats.DB_DATE_FORMAT;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat, Locale.getDefault());

        // Some times seem to be 1 character short, this protects against that
        if (timeTaken.length() < dateFormat.length()) {
          timeTaken += "0";
        }

        if (doseTime.length() < dateFormat.length()) {
            doseTime += "0";
        }

        doseId = id;
        medId = medicationId;
        taken = isTaken;
        this.timeTaken = timeTaken.length() == dateFormat.length() ? LocalDateTime.parse(timeTaken, formatter) : null;
        this.doseTime = doseTime.length() == dateFormat.length() ? LocalDateTime.parse(doseTime, formatter) : null;
    }

    public Dose(long id, long medicationId, boolean isTaken, @Nullable LocalDateTime timeTaken, @Nullable LocalDateTime doseTime, int overrideAmount, String overrideUnit) {
        doseId = id;
        medId = medicationId;
        taken = isTaken;
        this.timeTaken = timeTaken;
        this.doseTime = doseTime;
        overrideDoseAmount = overrideAmount;
        overrideDoseUnit = overrideUnit;
    }

    public Dose(long id, long medicationId, boolean isTaken, String timeTaken, String doseTime, int overrideAmount, String overrideUnit) {
        final String dateFormat = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat, Locale.getDefault());

        // Some times seem to be 1 character short, this protects against that
        if (timeTaken.length() < dateFormat.length()) {
            timeTaken += "0";
        }

        if (doseTime.length() < dateFormat.length()) {
            doseTime += "0";
        }

        doseId = id;
        medId = medicationId;
        taken = isTaken;
        this.timeTaken = LocalDateTime.parse(timeTaken, formatter);
        this.doseTime = LocalDateTime.parse(doseTime, formatter);
        overrideDoseAmount = overrideAmount;
        overrideDoseUnit = overrideUnit;
    }

    public Dose() {
        doseId = -1;
        medId = -1;
        taken = false;
        timeTaken = null;
        doseTime = null;
    }

    /**
     * Dose ID getter
     */
    public long getDoseId() { return doseId; }

    /**
     * Dose ID setter
     * @param doseId ID of dose
     */
    public void setDoseId(long doseId) { this.doseId = doseId; }

    /**
     * Whether or not dose is taken
     */
    public boolean isTaken() { return taken; }

    /**
     * Toggle medication taken or not
     * @param taken taken status of dose
     */
    public void setTaken(boolean taken) { this.taken = taken; }

    /**
     * Retrieves ID of associated medication
     * @return medication id
     */
    public long getMedId() { return medId; }

    /**
     * Set dose's medication ID
     * @param medId ID of medication
     */
    public void setMedId(long medId) { this.medId = medId; }

    /**
     * Get time dose was taken
     * @return Time medication was taken
     */
    public LocalDateTime getTimeTaken() { return timeTaken; }

    public String getTimeTakenText() {
        return TimeFormatting.localDateTimeToDbString(timeTaken);
    }

    public void setTimeTaken(String timeTaken) {
        final String dateFormat = DBHelper.DateFormats.DB_DATE_FORMAT;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat, Locale.getDefault());

        this.timeTaken = LocalDateTime.parse(timeTaken, formatter);
    }

    public void setTimeTaken(LocalDateTime timeTaken) {
        this.timeTaken = timeTaken;
    }

    public LocalDateTime getDoseTime() {
        return doseTime;
    }

    public String getDoseTimeText() {
        return TimeFormatting.localDateTimeToDbString(doseTime);
    }

    public void setDoseTime(String doseTime) {
        final String dateFormat = DBHelper.DateFormats.DB_DATE_FORMAT;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat, Locale.getDefault());

        this.doseTime = LocalDateTime.parse(doseTime, formatter);
    }

    public void setDoseTime(LocalDateTime doseTime) {
        this.doseTime = doseTime;
    }

    public int getOverrideDoseAmount() {
        return overrideDoseAmount;
    }

    public void setOverrideDoseAmount(int overrideDoseValue) {
        this.overrideDoseAmount = overrideDoseValue;
    }

    public void setOverrideDoseUnit(String overrideUnit) {
        this.overrideDoseUnit = overrideUnit;
    }

    public String getOverrideDoseUnit() {
        return overrideDoseUnit;
    }
}
