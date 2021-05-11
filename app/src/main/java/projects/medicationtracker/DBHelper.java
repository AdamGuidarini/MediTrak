package projects.medicationtracker;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "Medications.db";

    private static final String MEDICATION_TABLE = "Medication";
    private static final String MED_ID = "MedicationID";
    private static final String MED_NAME = "MedName";
    private static final String PATIENT_NAME = "PatientName";
    private static final String MED_DOSAGE = "Dosage";
    private static final String MED_UNITS = "Units";
    private static final String ALIAS = "Alias";

    private static final String MEDICATION_TRACKER_TABLE = "MedicationTracker";
    private static final String DOSE_TIME = "DoseTime";
    private static final String TAKEN = "Taken";
    private static final String TIME_TAKEN = "TimeTaken";
    private static final String DOSE_ID = "DoseID";
    private static final String MED_FREQUENCY = "DrugFrequency";

    private static final String MEDICATION_TIMES = "MedicationTimes";
    private static final String TIME_ID = "TimeID";
    private static final String DRUG_TIME = "DrugTime";

    private static final String MEDICATION_STATS_TABLE = "MedicationStats";
    private static final String START_DATE = "StartDate";
    private static final String END_DATE = "EndDate";
    private static final String DOSES_TAKEN = "DosesTaken";
    private static final String DOSES_MISSED = "DosesMissed";

    private static final String NOTES_TABLE = "Notes";
    private static final String NOTE_ID = "NoteID";
    private static final String NOTE = "Note";
    private static final String ENTRY_TIME = "EntryTime";


    public DBHelper(@Nullable Context context) { super(context, DATABASE_NAME, null, 1);}

    /**
     * Creates DBHelper object, adds tables to DB if the don't exist
     * @param sqLiteDatabase Database instance
     **************************************************************************/
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        final int NUM_TABLES = 5;
        String[] queries = new String[NUM_TABLES];

        // Holds all constant information on a given medication
        queries[0] = "CREATE TABLE IF NOT EXISTS" + MEDICATION_TABLE + "("
                + MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MED_NAME + " TEXT,"
                + PATIENT_NAME + " Text,"
                + MED_DOSAGE + " DECIMAL(3,2),"
                + MED_UNITS + " TEXT,"
                + START_DATE + " DATETIME,"
                + MED_FREQUENCY + " INT,"
                + ALIAS + " TEXT"
                + ")";

        // Holds data on past doses, as well as doses for current week
        queries[1] = "CREATE TABLE IF NOT EXISTS" + MEDICATION_TRACKER_TABLE + "("
                + DOSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MED_ID + " INT,"
                + DOSE_TIME + " DATETIME,"
                + TAKEN + " BOOLEAN,"
                + TIME_TAKEN + " DATETIME,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE"
                + ")";

        // Holds information on doses with a custom frequency so times for upcoming doses can be calculated
        queries[2] = "CREATE TABLE IF NOT EXISTS" + MEDICATION_TIMES + "("
                + TIME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MED_ID + " INT,"
                + DRUG_TIME + " TEXT,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE"
                + ")";

        // Holds statistics for a given medication
        queries[3] = "CREATE TABLE IF NOT EXISTS" + MEDICATION_STATS_TABLE + "("
                + MED_ID + " INT PRIMARY KEY ,"
                + START_DATE + " DATETIME, "
                + END_DATE + " DATETIME, "
                + DOSES_TAKEN + " INT, "
                + DOSES_MISSED + " INT,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE"
                + ")";

        // Stores a users notes for a medication, designed to help track how a medication is
        // affecting the patient. Facilitates tracking possible issues to bring up with prescriber
        queries[4] = "CREATE TABLE IF NOT EXISTS" + NOTES_TABLE + "("
                + NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MED_ID + " INT, "
                + NOTE + " TEXT, "
                + ENTRY_TIME + " DATETIME,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID +") ON DELETE CASCADE"
                + ")";

        for (int i = 0; i < NUM_TABLES; i++)
            sqLiteDatabase.execSQL(queries[i]);

    }

    /**
     * Instructions for creation of new DBHelper object
     * Currently enables foreign keys
     * @param db A database instance
     **************************************************************************/
    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys = ON");
    }

    /**
     * Instructions to perform on database upgrade
     * @param sqLiteDatabase Database instance
     * @param i Unused by required param
     * @param i1 Unused but required param
     **************************************************************************/
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
        // Remove tables
        @SuppressLint("Recycle") Cursor cursor = sqLiteDatabase.rawQuery(
                "SELECT name FROM sqlite_master WHERE type = 'table'", null);
        List<String> tables = new ArrayList<>();

        while (cursor.moveToNext())
            tables.add(cursor.getString(0));

        for (String table : tables)
        {
            String dropQuery = "DROP TABLE IF EXISTS " + table;
            sqLiteDatabase.execSQL(dropQuery);
        }

        onCreate(sqLiteDatabase);
    }

    /**
     * Adds new Medication to database
     * @param medName Name of Medication
     * @param patientName Name of patient
     * @param dosage Number of units to take
     * @param units Dosage unit
     * @param startDate Date of first dose
     * @param frequency How often to take Medication
     * @param alias Alias for Medication, appears in notifications
     * @return rowid on success, -1 on failure
     **************************************************************************/
    public long addMedication(String medName, String patientName, String dosage, String units,
                              String startDate, int frequency, String alias)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues medTableValues = new ContentValues();

        medTableValues.put(MED_NAME, medName);
        medTableValues.put(PATIENT_NAME, patientName);
        medTableValues.put(MED_DOSAGE, dosage);
        medTableValues.put(MED_UNITS, units);
        medTableValues.put(START_DATE, startDate);
        medTableValues.put(MED_FREQUENCY, frequency);
        medTableValues.put(ALIAS, alias);

        return db.insert(MEDICATION_TABLE, null, medTableValues);
    }

    /**
     * Adds dose to MedicationTracker table
     * @param medId ID of Medication
     * @param drugTime Time to take Medication
     * @return rowid on success, -1 on failure
     **************************************************************************/
    public long addDose(long medId, String drugTime)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues doseValues = new ContentValues();

        doseValues.put(MED_ID, medId);
        doseValues.put(DRUG_TIME, drugTime);

        return db.insert(MEDICATION_TIMES, null, doseValues);
    }

    /**
     * Creates an ArrayList of all patients
     * @return ArrayList of all patients, except
     **************************************************************************/
    public ArrayList<String> getPatients()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.rawQuery("SELECT DISTINCT " + PATIENT_NAME + " FROM " + MEDICATION_TABLE, null);

        ArrayList<String> patients = new ArrayList<>();

        result.moveToFirst();

        while (!result.isAfterLast())
        {
            patients.add(result.getString(result.getColumnIndex(PATIENT_NAME)));
            result.moveToNext();
        }

        result.close();

        return patients;
    }

    /**
     * Returns an ArrayList of all Medications for given patient
     * @param patient The name of the patient whose Medications are sought
     * @return An ArrayList of all Medications for given patient
     */
    public ArrayList<Medication> getMedicationsForPatient(String patient)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Medication> medications = new ArrayList<>();

        String query = "SELECT * FROM " + MEDICATION_TABLE + " WHERE " + PATIENT_NAME + " = " + patient;

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            int medId = Integer.parseInt(cursor.getString(cursor.getColumnIndex(MED_ID)));
            int dosage = Integer.parseInt(cursor.getString(cursor.getColumnIndex(MED_DOSAGE)));
            int freq = Integer.parseInt(cursor.getString(cursor.getColumnIndex(MED_FREQUENCY)));
            String medName = cursor.getString(cursor.getColumnIndex(MED_NAME));
            String units = cursor.getString(cursor.getColumnIndex(MED_UNITS));
            String startDateStr = cursor.getString(cursor.getColumnIndex(START_DATE));
            String alias = cursor.getString(cursor.getColumnIndex(ALIAS));

            LocalDateTime startDate = TimeFormatting.stringToLocalDateTime(startDateStr);

            if (alias == null)
                alias = "";

            LocalDateTime times[];

            String query2 = "Select " + DRUG_TIME + " FROM " + MEDICATION_TABLE + " WHERE "
                    + MED_ID + " = " + medId;

            Cursor cursor1 = db.rawQuery(query2, null);

            int count = cursor1.getCount();
            if (count > 0)
            {
                // Build list of times for a Medication
                times = new LocalDateTime[count];
                for (int i = 0; i < count; i++)
                {
                    LocalTime lt = LocalTime.parse(cursor.getString(cursor.getColumnIndex(DRUG_TIME)));

                    times[i] = LocalDateTime.of(LocalDate.MIN, lt);

                    cursor.moveToNext();
                }
            }
            else
            {
                times = new LocalDateTime[1];

                times[0] = LocalDateTime.MIN;
            }

            medications.add(new Medication(medName, patient, units, times,
                    startDate, medId, freq, dosage, alias));

            cursor1.close();
            cursor.moveToNext();
        }

        cursor.close();

        return medications;
    }

    /**
     * Get number of rows in MedicationTable
     * @return Number of rows in MedicationTable
     **************************************************************************/
    public long numberOfRows()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        return DatabaseUtils.queryNumEntries(db, MEDICATION_TABLE);
    }

    /**
     * Create ArrayList of all Medications in MedicationTable
     * @return All Medications in MedicationTable
     **************************************************************************/
    public ArrayList<Medication> getMedications()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Medication> allMeds = new ArrayList<>();

        String query = "SELECT * FROM " + MEDICATION_TABLE + " ORDER BY " + PATIENT_NAME;

        Cursor meds = db.rawQuery(query, null);
        meds.moveToFirst();

        // Iterates through cursors to create instances of Medication object
        while (!meds.isAfterLast())
        {
            int medId = Integer.parseInt(meds.getString(meds.getColumnIndex(MED_ID)));
            int dosage = Integer.parseInt(meds.getString(meds.getColumnIndex(MED_DOSAGE)));
            int frequency = Integer.parseInt(meds.getString(meds.getColumnIndex(MED_FREQUENCY)));
            String medName = meds.getString(meds.getColumnIndex(MED_NAME));
            String patient = meds.getString(meds.getColumnIndex(PATIENT_NAME));
            String units = meds.getString(meds.getColumnIndex(MED_UNITS));
            String date1 = meds.getString(meds.getColumnIndex(START_DATE));
            String alias = meds.getString(meds.getColumnIndex(ALIAS));

            LocalDateTime startDate = TimeFormatting.stringToLocalDateTime(date1);

            if (alias == null)
                alias = "";

            LocalDateTime[] times;

            Cursor cursor = db.rawQuery("SELECT " + DRUG_TIME + " FROM " + MEDICATION_TIMES
                    + " WHERE " + MED_ID + "=" + medId, null);
            cursor.moveToFirst();

            int count = cursor.getCount();

            if (count != 0)
            {
                // Build list of times for a Medication
                times = new LocalDateTime[count];
                for (int i = 0; i < count; i++)
                {
                    LocalTime lt = LocalTime.parse(cursor.getString(cursor.getColumnIndex(DRUG_TIME)));

                    times[i] = LocalDateTime.of(LocalDate.MIN, lt);

                    cursor.moveToNext();
                }
            }
            else
            {
                times = new LocalDateTime[1];

                times[0] = LocalDateTime.MIN;
            }

            cursor.close();

            Medication medication = new Medication(medName, patient, units, times,
                    startDate, medId, frequency, dosage, alias);

            allMeds.add(medication);
            meds.moveToNext();
        }

        meds.close();

        return allMeds;
    }

    /**
     * Determine of dose is in MedicationTracker
     * @param medication Medication to check
     * @param time Time of dose
     * @return True if present, false if not present
     **************************************************************************/
    public boolean isInMedicationTracker (Medication medication, LocalDateTime time)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String dateTime = TimeFormatting.LocalDateTimeToString(time);

        String query = "SELECT * FROM " + MEDICATION_TRACKER_TABLE + " WHERE " + MED_ID + " = " +
                medication.getMedId() + " AND " + DOSE_TIME + " = \"" + dateTime + "\"";

        int count = 0;

        try
        {
            Cursor cursor = db.rawQuery(query, null);
            count = cursor.getCount();
            cursor.close();
        }
        catch (SQLException e)
        {
            e.getCause();
        }

        return count > 0;
    }

    /**
     * Add dose to MedicationTracker table
     * @param medication Medication whose dose will be added
     * @param time Time of dose
     * @return rowid of added dose on success, -1 on failure
     **************************************************************************/
    public long addToMedicationTracker (Medication medication, LocalDateTime time)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues medTrackerValues = new ContentValues();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTime = time.format(formatter);

        medTrackerValues.put(MED_ID, medication.getMedId());
        medTrackerValues.put(DOSE_TIME, dateTime);
        medTrackerValues.put(TAKEN, false);

        return db.insert(MEDICATION_TRACKER_TABLE, null, medTrackerValues);
    }

    /**
     * Get ID of dose
     * @param medId ID of Medication
     * @param doseTime Time of dose
     * @return rowid of match found in MedicationTracker table
     **************************************************************************/
    public long getDoseId (int medId, String doseTime)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        long rowId;
        String query = "SELECT " + DOSE_ID + " FROM " + MEDICATION_TRACKER_TABLE + " WHERE "
                + MED_ID + "=" + medId + " AND " + DOSE_TIME + "=\"" + doseTime + "\"";

        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        rowId = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DOSE_ID)));
        cursor.close();

        return rowId;
    }

    /**
     * Status of entry in MedicationTracker
     * @param doseId ID of dose in table
     * @return Status of dose
     **************************************************************************/
    public boolean getTaken (long doseId)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + TAKEN + " FROM " + MEDICATION_TRACKER_TABLE
                + " WHERE " + DOSE_ID + " = " + doseId;

        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();
        int taken = Integer.parseInt(cursor.getString(cursor.getColumnIndex(TAKEN)));

        cursor.close();

        return taken == 1;
    }

    /**
     * Updates status of dose
     * @param id ID of Medication
     * @param timeTaken Time of dose
     * @param status Status of whether dose has been taken or not
     **************************************************************************/
    public void updateDoseStatus(long id, String timeTaken, boolean status)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues newValues = new ContentValues();
        int bool = status ? 1 : 0;

        newValues.put(TAKEN, bool);
        newValues.put(TIME_TAKEN, timeTaken);

        db.update(MEDICATION_TRACKER_TABLE, newValues, DOSE_ID + "=?", new String[]{String.valueOf(id)});
    }

    /**
     * Deletes all entries from database
     **************************************************************************/
    public boolean purge()
    {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(MEDICATION_TABLE, "1", null) != 0;
    }
}
