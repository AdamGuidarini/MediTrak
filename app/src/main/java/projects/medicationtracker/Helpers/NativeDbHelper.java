package projects.medicationtracker.Helpers;

import static projects.medicationtracker.Helpers.DBHelper.AGREED_TO_TERMS;
import static projects.medicationtracker.Helpers.DBHelper.DATABASE_NAME;
import static projects.medicationtracker.Helpers.DBHelper.DATE_FORMAT;
import static projects.medicationtracker.Helpers.DBHelper.EXPORT_FILE_NAME;
import static projects.medicationtracker.Helpers.DBHelper.EXPORT_FREQUENCY;
import static projects.medicationtracker.Helpers.DBHelper.EXPORT_START;
import static projects.medicationtracker.Helpers.DBHelper.SEEN_NOTIFICATION_REQUEST;
import static projects.medicationtracker.Helpers.DBHelper.THEME;
import static projects.medicationtracker.Helpers.DBHelper.TIME_FORMAT;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import projects.medicationtracker.Models.Dose;
import projects.medicationtracker.Models.Medication;
import projects.medicationtracker.Models.Notification;

public class NativeDbHelper {
    static {
        System.loadLibrary("medicationtracker");
    }

    private final String dbPath;
    private final String[] ignoredTables = {
        DBHelper.ANDROID_METADATA, DBHelper.SETTINGS_TABLE, DBHelper.NOTIFICATIONS
    };


    public NativeDbHelper(Context context) {
        dbPath = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
        dbCreate(dbPath);
    }

    /**
     * Creates database
     */
    public void create() {
        dbCreate(dbPath);
    }

    /**
     * Upgrades existing database
     *
     * @param version new database version number
     */
    public void upgrade(int version) {
        dbUpgrade(dbPath, version);
    }

    /**
     * Inserts a new record into table
     *
     * @param table  Table in which to insert record
     * @param values values to add
     * @return Row id of new record
     */
    public long insert(String table, Pair<String, String>[] values) {
        return insert(dbPath, table, values);
    }

    /**
     * Update a row in a database
     *
     * @param table  Table in which to update row
     * @param values Values to update
     * @param where  Where clause values
     * @return true if success
     */
    public boolean update(String table, Pair<String, String>[] values, Pair<String, String>[] where) {
        return update(dbPath, table, values, where);
    }

    /**
     * Update a row in a database
     *
     * @param table Table in which to update row
     * @param where Where clause values
     */
    public void delete(String table, Pair<String, String>[] where) {
        delete(dbPath, table, where);
    }

    /**
     * Exports a database
     *
     * @param exportPath    Path where exported file will be created
     * @return true if success
     */
    public boolean dbExport(String exportPath) {
        return dbExporter(dbPath, exportPath, ignoredTables);
    }

    /**
     * Imports a database
     *
     * @param fileContents  Contents of import file
     * @return true if import succeeded
     */
    public boolean dbImport(String fileContents) {
        return dbImporter(dbPath, fileContents, ignoredTables);
    }

    /**
     * Retrieves medication and all past doses
     *
     * @param medId ID of medication
     * @return Medication with all doses - Includes any parents/children
     */
    public Medication getMedicationHistory(long medId) {
        return getMedHistory(dbPath, medId, Medication.class);
    }

    /**
     * Exports history to a csv file
     *
     * @param filePath Where the file will be stored
     * @param data     Medication history data
     * @return true on success, false on failure
     */
    public boolean exportMedicationHistory(String filePath, Pair<String, String[]>[] data) {
        return exportMedHistory(dbPath, filePath, data);
    }

    public Dose findDose(long medicationId, LocalDateTime doseTime) {
        return findDose(
                dbPath,
                medicationId,
                TimeFormatting.localDateTimeToDbString(doseTime),
                new Medication()
        );
    }

    public Dose getDoseById(long doseId) {
        return getDoseById(dbPath, doseId, new Medication());
    }

    public boolean updateDose(Dose dose) {
        return updateDose(dbPath, dose);
    }

    public long stashNotification(Notification notification) {
        return stashNotification(dbPath, notification);
    }

    public void deleteNotification(long notificationId) {
        deleteNotification(dbPath, notificationId);
    }

    public void deleteNotificationByMedicationId(long medicationId) {
        deleteNotificationsByMedId(dbPath, medicationId);
    }

    public ArrayList<Notification> getNotifications() {
        return new ArrayList<>(Arrays.asList(getNotifications(dbPath, Notification.class)));
    }

    public long addDose(long medId, LocalDateTime scheduledTime, LocalDateTime timeTaken, boolean taken) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                DBHelper.DateFormats.DB_DATE_FORMAT, Locale.getDefault()
        );

        return addDose(
                dbPath,
                medId,
                formatter.format(scheduledTime),
                formatter.format(timeTaken),
                taken
        );
    }

    public void updateSettings(Bundle preferences) {
        Pair<String, String>[] settings = new Pair[]{
                new Pair<>(THEME, preferences.getString(THEME)),
                new Pair<>(AGREED_TO_TERMS, preferences.getString(AGREED_TO_TERMS)),
                new Pair<>(SEEN_NOTIFICATION_REQUEST, preferences.getString(SEEN_NOTIFICATION_REQUEST)),
                new Pair<>(DATE_FORMAT, preferences.getString(DATE_FORMAT)),
                new Pair<>(TIME_FORMAT, preferences.getString(TIME_FORMAT)),
                new Pair<>(EXPORT_FREQUENCY, preferences.getString(EXPORT_FREQUENCY)),
                new Pair<>(EXPORT_START, preferences.getString(EXPORT_START)),
                new Pair<>(EXPORT_FILE_NAME, preferences.getString(EXPORT_FILE_NAME))
        };

        updateSettings(dbPath, settings);
    }

    /**
     * Native methods
     */
    private native void dbCreate(String dbPath);

    private native void dbUpgrade(String dbPath, int version);

    private native long insert(String dbPath, String table, Pair<String, String>[] values);

    private native boolean update(String dbPath, String table, Pair<String, String>[] values, Pair<String, String>[] where);

    private native long delete(String dbPath, String table, Pair<String, String>[] values);

    private native boolean dbExporter(String databaseName, String exportDirectory, String[] ignoredTables);

    private native boolean dbImporter(String dbPath, String fileContents, String[] ignoredTables);

    private native Medication getMedHistory(String dbPath, long medId, Class<Medication> medicationClass);

    private native boolean exportMedHistory(String dbPath, String exportPath, Pair<String, String[]>[] data);

    private native Dose findDose(String dbPath, long medicationId, String doseTime, Medication med);

    private native Dose getDoseById(String dbPath, long doseId, Medication med);

    private native boolean updateDose(String dbPath, Dose dose);

    private native long stashNotification(String dbPath, Notification notification);

    private native void deleteNotification(String dbPath, long notificationId);
    private native void deleteNotificationsByMedId(String dbPath, long medicationid);

    private native Notification[] getNotifications(String dbPath, Class<Notification> notificationClass);

    private native long addDose(String dbPath, long medId, String scheduledTime, String timeTaken, boolean taken);
    private native void updateSettings(String dbPath, Pair<String, String>[] settings);
}
