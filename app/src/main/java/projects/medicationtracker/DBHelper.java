package projects.medicationtracker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper
{
    public final int NUM_TABLES = 6;

    public static final String DATABASE_NAME = "Medications.db";

    public static final String USER_TABLE = "Users";
    public static final String USER_ID = "UserID";
    public static final String USERNAME = "Username";
    public static final String PASSWORD = "Password";

    public static final String MEDICATION_TABLE = "Medication";
    public static final String MED_ID = "MedicationID";
    public static final String MED_NAME = "Name";
    public static final String MED_FREQUENCY = "DoseFrequency";
    public static final String MED_DOSAGE = "Dosage";
    public static final String MED_UNITS = "Units";

    public static final String MEDICATION_TRACKER_TABLE = "MedicationTracker";
    public static final String LAST_TAKEN = "LastTaken";
    public static final String NEXT_DOSE = "NextDose";

    public static final String MEDICATION_HISTORY = "MedicationHistory";
    public static final String TAKEN = "Taken";
    public static final String TIME_TAKEN = "DATETIME";

    public static final String MEDICATION_STATS_TABLE = "MedicationStats";
    public static final String START_DATE = "StartDate";
    public static final String END_DATE = "EndDate";
    public static final String DOSES_TAKEN = "DosesTaken";
    public static final String DOSES_MISSED = "DosesMissed";

    public static final String NOTES_TABLE = "Notes";
    public static final String NOTE_ID = "NoteID";
    public static final String NOTE = "Note";
    public static final String ENTRY_TIME = "EntryTime";


    public DBHelper(@Nullable Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        String[] queries = new String[NUM_TABLES];

        queries[0] = "CREATE TABLE " + USER_TABLE + "("
                + USER_ID + " INT,"
                + USERNAME + " TEXT,"
                + PASSWORD + " TEXT," +
                "PRIMARY KEY (" + USER_ID + ")" +
                ")";

        queries[1] = "CREATE TABLE " + MEDICATION_TABLE + "("
                + MED_ID + " INT, "
                + MED_NAME + " TEXT,"
                + MED_FREQUENCY + " INT, "
                + MED_DOSAGE + " DECIMAL(3,2), "
                + MED_UNITS + " TEXT,"
                + "PRIMARY KEY (" + MED_ID + ")"
                + ")";

        queries[2] = "CREATE TABLE " + MEDICATION_TRACKER_TABLE + "("
                + MED_ID + " INT,"
                + LAST_TAKEN + " DATETIME, "
                + NEXT_DOSE + " DATETIME,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + "),"
                + "PRIMARY KEY (" + MED_ID + ")"
                + ")";

        queries[3] = "CREATE TABLE " + MEDICATION_HISTORY + "("
                + MED_ID + " INT,"
                + TAKEN + " BOOL,"
                + TIME_TAKEN + " DATETIME,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + "),"
                + "PRIMARY KEY (" + MED_ID + ")"
                + ")";

        queries[4] = "CREATE TABLE " + MEDICATION_STATS_TABLE + "("
                + MED_ID + " INT,"
                + START_DATE + " DATETIME, "
                + END_DATE + " DATETIME, "
                + DOSES_TAKEN + " INT, "
                + DOSES_MISSED + " INT,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + "),"
                + "PRIMARY KEY (" + MED_ID + ")" +
                ")";

        queries[5] = "CREATE TABLE " + NOTES_TABLE + "("
                + NOTE_ID + " INT,"
                + MED_ID + " INT, "
                + NOTE + " TEXT, "
                + ENTRY_TIME + " DATETIME,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID +"),"
                + "PRIMARY KEY (" + NOTE_ID + ")" +
                ")";

        for (int i = 0; i < NUM_TABLES; i++)
            sqLiteDatabase.execSQL(queries[i]);
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



    public void testDB()
    {
        SQLiteDatabase db = this.getReadableDatabase();
    }
}
