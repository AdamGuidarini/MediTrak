package projects.medicationtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper
{
    private final int NUM_TABLES = 4;

    private static final String DATABASE_NAME = "Medications.db";

    private static final String MEDICATION_TABLE = "Medication";
    private static final String MED_ID = "MedicationID";
    private static final String MED_NAME = "MedName";
    private static final String PATIENT_NAME = "PatientName";
    private static final String MED_DOSAGE = "Dosage";
    private static final String MED_UNITS = "Units";

    private static final String MEDICATION_TRACKER_TABLE = "MedicationTracker";
    private static final String DOSE_TIME = "DoseTime";
    private static final String TAKEN = "Taken";
    private static final String TIME_TAKEN = "TimeTaken";
    private static final String DOSE_ID = "DoseID";

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

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        String[] queries = new String[NUM_TABLES];

        queries[0] = "CREATE TABLE " + MEDICATION_TABLE + "("
                + MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MED_NAME + " TEXT,"
                + PATIENT_NAME + " Text,"
                + MED_DOSAGE + " DECIMAL(3,2),"
                + MED_UNITS + " TEXT"
                + ")";

        queries[1] = "CREATE TABLE " + MEDICATION_TRACKER_TABLE + "("
                + DOSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MED_ID + " INT,"
                + DOSE_TIME + " DATETIME,"
                + TAKEN + " BOOLEAN,"
                + TIME_TAKEN + " DATETIME,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE"
                + ")";

        queries[2] = "CREATE TABLE " + MEDICATION_STATS_TABLE + "("
                + MED_ID + " INT PRIMARY KEY ,"
                + START_DATE + " DATETIME, "
                + END_DATE + " DATETIME, "
                + DOSES_TAKEN + " INT, "
                + DOSES_MISSED + " INT,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE"
                + ")";

        queries[3] = "CREATE TABLE " + NOTES_TABLE + "("
                + NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MED_ID + " INT, "
                + NOTE + " TEXT, "
                + ENTRY_TIME + " DATETIME,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID +") ON DELETE CASCADE"
                + ")";

        for (int i = 0; i < NUM_TABLES; i++)
            sqLiteDatabase.execSQL(queries[i]);
    }

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
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'", null);
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

    public boolean addMedication(String medName, String patientName, String dosage, String units, ArrayList<String> times)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db1 = this.getReadableDatabase();
        ContentValues medTableValues = new ContentValues();
        ContentValues medTrackerValues = new ContentValues();

        medTableValues.put(MED_NAME, medName);
        medTableValues.put(PATIENT_NAME, patientName);
        medTableValues.put(MED_DOSAGE, dosage);
        medTableValues.put(MED_UNITS, units);

        long rowid = db.insert(MEDICATION_TABLE, null, medTableValues);

        if (rowid == -1)
            return false;

        medTrackerValues.put(MED_ID, rowid);
        medTrackerValues.put(DOSE_TIME, 0);
        medTrackerValues.put(TAKEN, 0);
        medTrackerValues.put(TIME_TAKEN, 0);

        if (db.insert(MEDICATION_TRACKER_TABLE, null, medTrackerValues) == -1)
            return false;

        return true;
    }



    public void testDB()
    {
        SQLiteDatabase db = this.getReadableDatabase();
    }
}
