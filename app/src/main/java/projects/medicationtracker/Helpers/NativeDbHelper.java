package projects.medicationtracker.Helpers;

import android.util.Pair;

import projects.medicationtracker.SimpleClasses.Medication;

public class NativeDbHelper {
    static {
        System.loadLibrary("medicationtracker");
    }

    private final String dbPath;

    public NativeDbHelper(String databasePath) {
        dbPath = databasePath;
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
     * @param version new database version number
     */
    public void upgrade(int version) {
        dbUpgrade(dbPath, version);
    }

    /**
     * Inserts a new record into table
     * @param table Table in which to insert record
     * @param values values to add
     * @return Row id of new record
     */
    public long insert(String table, Pair<String, String>[] values) {
        return insert(dbPath, table, values);
    }

    /**
     * Update a row in a database
     * @param table Table in which to update row
     * @param values Values to update
     * @param where Where clause values
     * @return true if success
     */
    public boolean update(String table, Pair<String, String>[] values, Pair<String, String>[] where) {
        return update(dbPath, table, values, where);
    }

    /**
     * Update a row in a database
     * @param table Table in which to update row
     * @param where Where clause values
     */
    public void delete(String table, Pair<String, String>[] where) {
        delete(dbPath, table, where);
    }

    /**
     * Exports a database
     * @param exportPath Path where exported file will be created
     * @param ignoredTables Tables to ignore in database
     * @return true if success
     */
    public boolean dbExport(String exportPath, String[] ignoredTables) {
        return dbExporter(dbPath, exportPath, ignoredTables);
    }

    /**
     * Imports a database
     * @param fileContents Contents of import file
     * @param ignoredTables Tables to ignore while importing
     * @return true if import succeeded
     */
    public boolean dbImport(String fileContents, String[] ignoredTables) {
        return dbImporter(dbPath, fileContents, ignoredTables);
    }

    /**
     * Retrieves medication and all past doses
     * @param medId ID of medication
     * @return Medication with all doses - Includes any parents/children
     */
    public Medication getMedicationHistory(long medId) {
        return getMedHistory(dbPath, medId);
    }

    private  native void dbCreate(String dbPath);
    private native void dbUpgrade(String dbPath, int version);
    private native long insert(String dbPath, String table, Pair<String, String>[] values);
    private native boolean update(String dbPath, String table, Pair<String, String>[] values, Pair<String, String >[] where);
    private native long delete(String dbPath, String table, Pair<String, String>[] values);
    private native boolean dbExporter(String databaseName, String exportDirectory, String[] ignoredTables);
    private native boolean dbImporter(String dbPath, String fileContents, String[] ignoredTables);
    private native Medication getMedHistory(String dbPath, long medId);
}
