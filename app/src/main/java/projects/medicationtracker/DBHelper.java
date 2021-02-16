package projects.medicationtracker;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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


    public DBHelper(@Nullable Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    // Creates all tables for the database when the app runs for the first time
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        int NUM_TABLES = 5;
        String[] queries = new String[NUM_TABLES];

        // Holds all constant information on a given medication
        queries[0] = "CREATE TABLE " + MEDICATION_TABLE + "("
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
        queries[1] = "CREATE TABLE " + MEDICATION_TRACKER_TABLE + "("
                + DOSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MED_ID + " INT,"
                + DOSE_TIME + " DATETIME,"
                + TAKEN + " BOOLEAN,"
                + TIME_TAKEN + " DATETIME,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE"
                + ")";

        // Holds information on doses with a custom frequency so times for upcoming doses can be calculated
        queries[2] = "CREATE TABLE " + MEDICATION_TIMES + "("
                + TIME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MED_ID + " INT,"
                + DRUG_TIME + " TEXT,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE"
                + ")";

        // Holds statistics for a given medication
        queries[3] = "CREATE TABLE " + MEDICATION_STATS_TABLE + "("
                + MED_ID + " INT PRIMARY KEY ,"
                + START_DATE + " DATETIME, "
                + END_DATE + " DATETIME, "
                + DOSES_TAKEN + " INT, "
                + DOSES_MISSED + " INT,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE"
                + ")";

        // Stores a users notes for a medication, designed to help track how a medication is
        // affecting the patient. Facilitates tracking possible issues to bring up with prescriber
        queries[4] = "CREATE TABLE " + NOTES_TABLE + "("
                + NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MED_ID + " INT, "
                + NOTE + " TEXT, "
                + ENTRY_TIME + " DATETIME,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID +") ON DELETE CASCADE"
                + ")";

        for (int i = 0; i < NUM_TABLES; i++)
            sqLiteDatabase.execSQL(queries[i]);

    }

    // Allows the use of foreign keys
    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys = ON");
    }

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

    // Adds new medications to the database
    // Returns rowid on success, or -1 on failure
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

    // Adds new dose to MedicationTimes
    // returns rowid on success, -1 on failure
    public long addDose(long medId, String drugTime)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues doseValues = new ContentValues();

        doseValues.put(MED_ID, medId);
        doseValues.put(DRUG_TIME, drugTime);

        return db.insert(MEDICATION_TIMES, null, doseValues);
    }

    // Creates a list of all patients
    // Returns a list of all patients except app user e.i. ME!
    public ArrayList<String> getPatients()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.rawQuery("SELECT " + PATIENT_NAME + " FROM " + MEDICATION_TABLE
                + " WHERE " + PATIENT_NAME + "!= 'ME!'", null);

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

    // Returns number of medications in database
    public long numberOfRows()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        return DatabaseUtils.queryNumEntries(db, MEDICATION_TABLE);
    }

    // Returns a list of all entries in MedicationTable
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
            String startDate = meds.getString(meds.getColumnIndex(START_DATE));
            String alias = meds.getString(meds.getColumnIndex(ALIAS));

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

            Medication medication = new Medication(medName, patient, units, times, startDate, medId, frequency, dosage, alias);

            allMeds.add(medication);
            meds.moveToNext();
        }

        meds.close();

        return allMeds;
    }
}
