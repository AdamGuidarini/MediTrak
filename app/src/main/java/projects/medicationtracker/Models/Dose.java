package projects.medicationtracker.Models;

import androidx.annotation.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

/**
 * A simple class representing a Medication dose
 */
public class Dose {
    private long doseId;
    private boolean taken;
    private long medId;
    private LocalDateTime timeTaken;
    private LocalDateTime doseTime;

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
        Integer[] takenToks = Arrays.stream(timeTaken.split("[:\\-\\s]")).map(s -> Integer.parseInt(s)).toArray(Integer[]::new);
        Integer[] doseTimeToks = Arrays.stream(doseTime.split("[:\\-\\s]")).map(s -> Integer.parseInt(s)).toArray(Integer[]::new);

        doseId = id;
        medId = medicationId;
        taken = isTaken;
        this.timeTaken = LocalDateTime.of(takenToks[0], takenToks[1], takenToks[2], takenToks[3], takenToks[4]);
        this.doseTime = LocalDateTime.of(doseTimeToks[0], doseTimeToks[1], doseTimeToks[2], doseTimeToks[3], doseTimeToks[4]);
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

    public void setTimeTaken(LocalDateTime timeTaken) {
        this.timeTaken = timeTaken;
    }

    public LocalDateTime getDoseTime() {
        return doseTime;
    }

    public void setDoseTime(LocalDateTime doseTime) {
        this.doseTime = doseTime;
    }
}
