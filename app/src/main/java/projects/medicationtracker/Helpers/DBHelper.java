package projects.medicationtracker.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

import projects.medicationtracker.SimpleClasses.Medication;
import projects.medicationtracker.SimpleClasses.Note;

public class DBHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "Medications.db";
    private final static int DATABASE_VERSION = 4;

    private static final String MEDICATION_TABLE = "Medication";
    private static final String MED_ID = "MedicationID";
    private static final String MED_NAME = "MedName";
    private static final String PATIENT_NAME = "PatientName";
    private static final String MED_DOSAGE = "Dosage";
    private static final String MED_UNITS = "Units";
    private static final String ALIAS = "Alias";
    private static final String ACTIVE = "Active";

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

    private static final String NOTES_TABLE = "Notes";
    private static final String NOTE_ID = "NoteID";
    private static final String NOTE = "Note";
    private static final String ENTRY_TIME = "EntryTime";

    private static final String SETTINGS_TABLE = "Settings";
    private static final String TIME_BEFORE_DOSE = "TimeBeforeDose";
    private static final String ENABLE_NOTIFICATIONS = "EnableNotifications";
    private static final String THEME = "Theme";
    public static final String DEFAULT = "default";
    public static final String LIGHT = "light";
    public static final String DARK = "dark";

    private static final String ACTIVITY_CHANGE_TABLE = "ActivityChanges";
    private static final String CHANGE_EVENT_ID = "ChangeId";
    private static final String CHANGE_DATE = "ChangeDate";
    private static final String PAUSED = "Paused";

    public DBHelper(@Nullable Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION);}

    /**
     * Creates DBHelper object, adds tables to DB if the don't exist
     * @param sqLiteDatabase Database instance
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        // Holds all constant information on a given medication
        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + MEDICATION_TABLE + "("
                + MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MED_NAME + " TEXT,"
                + PATIENT_NAME + " Text,"
                + MED_DOSAGE + " DECIMAL(3,2),"
                + MED_UNITS + " TEXT,"
                + START_DATE + " DATETIME,"
                + MED_FREQUENCY + " INT,"
                + ALIAS + " TEXT,"
                + ACTIVE + " BOOLEAN DEFAULT " + 1
                + ")"
        );

        // Holds data on past doses, as well as doses for current week
        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + MEDICATION_TRACKER_TABLE + "("
                + DOSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MED_ID + " INT,"
                + DOSE_TIME + " DATETIME,"
                + TAKEN + " BOOLEAN,"
                + TIME_TAKEN + " DATETIME,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE"
                + ")"
        );

        // Holds information on doses with a custom frequency so times for upcoming doses can be calculated
        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + MEDICATION_TIMES + "("
                + TIME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MED_ID + " INT,"
                + DRUG_TIME + " TEXT,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE"
                + ")"
        );

        // Stores a users notes for a medication, designed to help track how a medication is
        // affecting the patient. Facilitates tracking possible issues to bring up with prescriber
        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + NOTES_TABLE + "("
                + NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MED_ID + " INT, "
                + NOTE + " TEXT, "
                + ENTRY_TIME + " DATETIME,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID +") ON DELETE CASCADE"
                + ")"
        );

        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + SETTINGS_TABLE + "("
                + TIME_BEFORE_DOSE + " INT DEFAULT 2, "
                + ENABLE_NOTIFICATIONS + " BOOLEAN DEFAULT 1, "
                + THEME + " TEXT DEFAULT '" + DEFAULT + "')"
        );

        sqLiteDatabase.execSQL("INSERT INTO " + SETTINGS_TABLE + "("
                + ENABLE_NOTIFICATIONS + ", " + TIME_BEFORE_DOSE + ")"
                + "VALUES (1, 2)");

        sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + ACTIVITY_CHANGE_TABLE + "("
                + CHANGE_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MED_ID + " INT,"
                + CHANGE_DATE + " DATETIME,"
                + PAUSED + " BOOLEAN,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID +") ON DELETE CASCADE"
                + ")"
        );
    }

    /**
     * Instructions for creation of new DBHelper object
     * Currently enables foreign keys
     * @param db A database instance
     */
    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys = ON");
    }

    /**
     * Instructions to perform on database upgrade
     * @param sqLiteDatabase Database instance
     * @param i Old version of database
     * @param i1 New Version of database
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
        onCreate(sqLiteDatabase);

        if (i < 2)
        {
            sqLiteDatabase.execSQL("ALTER TABLE " + MEDICATION_TABLE + " ADD COLUMN " + ACTIVE + " BOOLEAN DEFAULT 1;");
        }

        if (i < 3)
        {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MEDICATION_STATS_TABLE);
        }

        if (i < 4)
        {
            sqLiteDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS " + ACTIVITY_CHANGE_TABLE + "("
                + CHANGE_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MED_ID + " INT,"
                + CHANGE_DATE + " DATETIME,"
                + PAUSED + " BOOLEAN,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID +") ON DELETE CASCADE"
                + ")"
            );
        }
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
     */
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
     */
    public long addDoseTime(long medId, String drugTime)
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
     */
    public ArrayList<String> getPatients()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.rawQuery(
                "SELECT DISTINCT " + PATIENT_NAME + " FROM " + MEDICATION_TABLE, null
        );

        ArrayList<String> patients = new ArrayList<>();

        result.moveToFirst();

        while (!result.isAfterLast())
        {
            patients.add(result.getString(result.getColumnIndexOrThrow(PATIENT_NAME)));
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

        String query = "SELECT * FROM " + MEDICATION_TABLE + " WHERE " + PATIENT_NAME + " = \"" + patient + "\"";

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            int medId = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(MED_ID)));
            int dosage = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(MED_DOSAGE)));
            int freq = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(MED_FREQUENCY)));
            String medName = cursor.getString(cursor.getColumnIndexOrThrow(MED_NAME));
            String units = cursor.getString(cursor.getColumnIndexOrThrow(MED_UNITS));
            String startDateStr = cursor.getString(cursor.getColumnIndexOrThrow(START_DATE));
            String alias = cursor.getString(cursor.getColumnIndexOrThrow(ALIAS));

            LocalDateTime startDate = TimeFormatting.stringToLocalDateTime(startDateStr);

            if (alias == null)
                alias = "";

            LocalDateTime[] times;

            String query2 = "Select " + DRUG_TIME + " FROM " + MEDICATION_TIMES + " WHERE "
                    + MED_ID + " = " + medId;

            Cursor cursor1 = db.rawQuery(query2, null);
            cursor1.moveToFirst();

            int count = cursor1.getCount();
            if (count != 0)
            {
                // Build list of times for a Medication
                times = new LocalDateTime[count];
                ArrayList<LocalDateTime> timesArr = new ArrayList<>();

                for (int i = 0; i < count; i++)
                {
                    LocalTime lt = LocalTime.parse(cursor1.getString(cursor1.getColumnIndexOrThrow(DRUG_TIME)));

                    timesArr.add(LocalDateTime.of(LocalDate.MIN, lt));

                    cursor1.moveToNext();
                }

                Collections.sort(timesArr);

                for (int i = 0; i < count; i++)
                    times[i] = timesArr.get(i);

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
     */
    public long numberOfRows()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        return DatabaseUtils.queryNumEntries(db, MEDICATION_TABLE);
    }

    /**
     * Create ArrayList of all Medications in MedicationTable
     * @return All Medications in MedicationTable
     */
    public ArrayList<Medication> getMedications()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Medication> allMeds = new ArrayList<>();

        String query = "SELECT * FROM " + MEDICATION_TABLE
                + " ORDER BY " + PATIENT_NAME;

        Cursor meds = db.rawQuery(query, null);
        meds.moveToFirst();

        if (meds.getCount() == 0)
        {
            meds.close();
            return allMeds;
        }

        // Iterates through cursors to create instances of Medication object
        while (!meds.isAfterLast())
        {
            int medId = Integer.parseInt(meds.getString(meds.getColumnIndexOrThrow(MED_ID)));
            float dosage = Float.parseFloat(meds.getString(meds.getColumnIndexOrThrow(MED_DOSAGE)));
            int frequency = Integer.parseInt(meds.getString(meds.getColumnIndexOrThrow(MED_FREQUENCY)));
            String medName = meds.getString(meds.getColumnIndexOrThrow(MED_NAME));
            String patient = meds.getString(meds.getColumnIndexOrThrow(PATIENT_NAME));
            String units = meds.getString(meds.getColumnIndexOrThrow(MED_UNITS));
            String date1 = meds.getString(meds.getColumnIndexOrThrow(START_DATE));
            String alias = meds.getString(meds.getColumnIndexOrThrow(ALIAS));
            boolean acitve = Integer.parseInt(meds.getString(meds.getColumnIndexOrThrow(ACTIVE))) == 1;

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
                ArrayList<LocalDateTime> timesArr = new ArrayList<>();

                for (int i = 0; i < count; i++)
                {
                    LocalTime lt = LocalTime.parse(cursor.getString(cursor.getColumnIndexOrThrow(DRUG_TIME)));

                    timesArr.add(LocalDateTime.of(LocalDate.MIN, lt));

                    cursor.moveToNext();
                }

                Collections.sort(timesArr);

                for (int i = 0; i < count; i++)
                    times[i] = timesArr.get(i);

            }
            else
            {
                times = new LocalDateTime[1];

                times[0] = LocalDateTime.MIN;
            }

            cursor.close();

            Medication medication = new Medication(medName, patient, units, times,
                    startDate, medId, frequency, dosage, alias);
            medication.setActiveStatus(acitve);

            allMeds.add(medication);
            meds.moveToNext();
        }

        meds.close();

        return allMeds;
    }

    /**
     * Retrieves a Medication object based on ID passed to it
     * @param id The ID of the Medication
     * @return Medication retrieved from database
     */
    public Medication getMedication(long id)
    {
        SQLiteDatabase db =this.getReadableDatabase();
        String query = "SELECT * FROM " + MEDICATION_TABLE + " WHERE "
                + MED_ID + " = " + id;

        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        String medName = cursor.getString(cursor.getColumnIndexOrThrow(MED_NAME));
        String patient = cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_NAME));
        String units = cursor.getString(cursor.getColumnIndexOrThrow(MED_UNITS));
        LocalDateTime startDate = TimeFormatting.stringToLocalDateTime(cursor.getString(
                cursor.getColumnIndexOrThrow(START_DATE)));
        long medId = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(MED_ID)));
        long frequency = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(MED_FREQUENCY)));
        float dosage = Float.parseFloat(cursor.getString(cursor.getColumnIndexOrThrow(MED_DOSAGE)));
        String alias = cursor.getString(cursor.getColumnIndexOrThrow(ALIAS));

        LocalDateTime[] times = new LocalDateTime[0];

        cursor.close();

        return new Medication(
                medName, patient, units, times, startDate, medId, frequency, dosage, alias
        );
    }

    /**
     * Retrieves an array of all medication times for given medication from database.
     * @param id The id of the medication whose times should be retrieved.
     * @return An array of all of a medications times.
     */
    public LocalTime[] getMedicationTimes(long id)
    {
        final SQLiteDatabase db = this.getReadableDatabase();
        final String query = "SELECT " + DRUG_TIME + " FROM " + MEDICATION_TIMES + " WHERE "
                + MED_ID + " = " + id;

        Cursor cursor = db.rawQuery(query, null);
        LocalTime[] times = new LocalTime[cursor.getCount()];

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++)
        {
            final String time = cursor.getString(cursor.getColumnIndexOrThrow(DRUG_TIME));
            times[i] = LocalTime.parse(time);
            cursor.moveToNext();
        }

        cursor.close();

        return times;
    }

    /**
     * Updates the given medication in the database.
     * @param medication The medication to update.
     */
    public void updateMedication(Medication medication)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        LocalTime[] prevTimes = getMedicationTimes(medication.getId());
        LocalTime[] newTimes = new LocalTime[medication.getTimes().length];

        for (int i = 0; i < newTimes.length; i++)
        {
            newTimes[i] = medication.getTimes()[i].toLocalTime();
        }

        // Update data in Medication table
        cv.put(MED_NAME, medication.getName());
        cv.put(MED_DOSAGE, medication.getDosage());
        cv.put(MED_FREQUENCY, medication.getFrequency());
        cv.put(MED_UNITS, medication.getDosageUnits());
        cv.put(START_DATE, TimeFormatting.localDateTimeToString(medication.getStartDate()));
        cv.put(PATIENT_NAME, medication.getPatientName());
        cv.put(ALIAS, medication.getAlias());

        db.update(MEDICATION_TABLE, cv, MED_ID + " = " + medication.getId(), null);

        cv.clear();

        cv.put(MED_ID, medication.getId());

        if (medication.getTimes().length > 0)
        {
            // Replace old times in DB
            LocalTime[] oldTimes = getMedicationTimes(medication.getId());
            int diff = medication.getTimes().length - oldTimes.length;
            int maxIndex = diff > 0 ? oldTimes.length : oldTimes.length + diff;
            long[] timeIds = this.getMedicationTimeIds(medication);


            for (int i = 0; i < maxIndex; i++)
            {
                String timeAsString = medication.getTimes()[i].toLocalTime().toString() + ":00";

                String whereClause = TIME_ID + '=' + timeIds[i];

                cv.put(DRUG_TIME, timeAsString);
                db.update(MEDICATION_TIMES, cv, whereClause, null);

                cv.clear();
            }

            // Add additional times
            if (diff > 0)
            {
                for (int i = maxIndex; i < medication.getTimes().length; i++)
                {
                    String timeAsString = medication.getTimes()[i].toLocalTime().toString();

                    cv.put(MED_ID, medication.getId());
                    cv.put(DRUG_TIME, timeAsString);

                    db.insert(MEDICATION_TIMES,  null, cv);

                    cv.clear();
                }
            }
            // Delete excess old times if they exist
            else
            {
                for (int i = maxIndex; i < oldTimes.length; i++)
                {
                    String oldTimeAsString = oldTimes[i].toString();
                    String whereClause = DRUG_TIME + "='" + oldTimeAsString + "' AND " + MED_ID
                            + "=" + medication.getId();

                    db.delete(MEDICATION_TIMES, whereClause, null);
                }
            }
        }

        // Remove old times if times changed - may no longer line up with schedule
        if (prevTimes != newTimes)
        {
            db.execSQL("DELETE FROM " + MEDICATION_TRACKER_TABLE + " WHERE " + MED_ID + " = " + medication.getId());
        }
    }

    /**
     * Deletes Medication passed to it from database
     * @param medication Medication to delete
     */
    public void deleteMedication(Medication medication)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = MED_ID + " = " + medication.getId();

        db.delete(MEDICATION_TABLE, query, null);
    }

    /**
     * Determine of dose is in MedicationTracker
     * @param medication Medication to check
     * @param time Time of dose
     * @return True if present, false if not present
     */
    public boolean isInMedicationTracker(Medication medication, LocalDateTime time)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String dateTime = TimeFormatting.localDateTimeToString(time);

        String query = "SELECT * FROM " + MEDICATION_TRACKER_TABLE + " WHERE " + MED_ID + " = " +
                medication.getId() + " AND " + DOSE_TIME + " = '" + dateTime + "'";

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
     */
    public long addToMedicationTracker(Medication medication, LocalDateTime time)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues medTrackerValues = new ContentValues();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTime = time.format(formatter);

        medTrackerValues.put(MED_ID, medication.getId());
        medTrackerValues.put(DOSE_TIME, dateTime);
        medTrackerValues.put(TAKEN, false);

        return db.insert(MEDICATION_TRACKER_TABLE, null, medTrackerValues);
    }

    public LocalDateTime[] getDoseFromMedicationTracker(Medication medication)
    {
        LocalDateTime[] times;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + DOSE_TIME + " FROM " + MEDICATION_TRACKER_TABLE
                + " WHERE " + MED_ID + "=" + medication.getId();
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();
        times = new LocalDateTime[cursor.getCount()];

        while (!cursor.isAfterLast())
        {
            times[cursor.getPosition()] = TimeFormatting.stringToLocalDateTime(
                            cursor.getString(cursor.getColumnIndexOrThrow(DOSE_TIME))
                    );

            cursor.moveToNext();
        }

        cursor.close();

        return times;
    }

    public void deleteDose(long doseId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = DOSE_ID + " = " + doseId;

        db.delete(MEDICATION_TRACKER_TABLE, whereClause, null);
    }

    /**
     * Get ID of dose
     * @param medId ID of Medication
     * @param doseTime Time of dose
     * @return Dose ID of match found in MedicationTracker table
     */
    public long getDoseId(long medId, String doseTime)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        long rowId;
        String query = "SELECT " + DOSE_ID + " FROM " + MEDICATION_TRACKER_TABLE + " WHERE "
                + MED_ID + "=" + medId + " AND " + DOSE_TIME + "= '" + doseTime + "'";

        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        rowId = cursor.getCount() > 0 ?
                Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(DOSE_ID))) : -1;

        cursor.close();

        return rowId;
    }

    /**
     * Status of entry in MedicationTracker
     * @param doseId ID of dose in table
     * @return Status of dose
     */
    public boolean getTaken(long doseId)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + TAKEN + " FROM " + MEDICATION_TRACKER_TABLE
                + " WHERE " + DOSE_ID + " = " + doseId;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.getCount() == 0)
        {
            cursor.close();

            return false;
        }

        cursor.moveToFirst();

        int taken = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(TAKEN)));

        cursor.close();

        return taken == 1;
    }

    /**
     * Updates status of dose
     * @param doseId ID of dose
     * @param timeTaken Time of dose
     * @param status Status of whether dose has been taken or not
     */
    public void updateDoseStatus(long doseId, String timeTaken, boolean status)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues newValues = new ContentValues();
        int bool = status ? 1 : 0;

        newValues.put(TAKEN, bool);
        newValues.put(TIME_TAKEN, timeTaken);

        db.update(MEDICATION_TRACKER_TABLE, newValues, DOSE_ID + "=?", new String[]{String.valueOf(doseId)});
    }

    /**
     * Deletes all entries from database
     */
    public void purge()
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(MEDICATION_TABLE, "1", null);
    }

    /**
     * Adds a new Note to the Notes table
     * @param note The note to add
     * @param medId ID of medication note is about
     */
    public void addNote(String note, long medId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        String now = TimeFormatting.localDateTimeToString(LocalDateTime.now());

        cv.put(MED_ID, medId);
        cv.put(NOTE, note);
        cv.put(ENTRY_TIME, now);

        db.insert(NOTES_TABLE, null, cv);
    }

    /**
     * Removes a note from the database
     * @param note Note to remove
     */
    public void deleteNote(Note note)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = NOTE + " = \"" + note.getNote() + "\" AND " + ENTRY_TIME
                + " = '" + TimeFormatting.localDateTimeToString(note.getNoteTime()) + "'";

        db.delete(NOTES_TABLE, query, null);
    }

    /**
     * Allows for a note to be updated.
     * @param note The new note.
     */
    public void updateNote(Note note, String newNote)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(NOTE, newNote);

        db.update(NOTES_TABLE, cv, NOTE_ID + "=?", new String[]{String.valueOf(
                note.getNoteId())});
    }

    /**
     * Retrieve all notes pertaining to given Medication.
     * @param medId ID of Medication pertaining to Note.
     * @return An ArrayList of all notes about given Medication.
     */
    public ArrayList<Note> getNotes(long medId)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Note> notes = new ArrayList<>();
        String query = "SELECT * FROM " + NOTES_TABLE + " WHERE " + MED_ID + " = " + medId;

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        if (cursor.getCount() == 0)
            return null;

        while (!cursor.isAfterLast())
        {
            long noteId = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(NOTE_ID)));
            String note = cursor.getString(cursor.getColumnIndexOrThrow(NOTE));
            LocalDateTime entryTime = TimeFormatting.stringToLocalDateTime(cursor.getString(
                    cursor.getColumnIndexOrThrow(ENTRY_TIME)));

            notes.add(new Note(noteId, medId, note, entryTime));

            cursor.moveToNext();
        }

        cursor.close();

        return notes;
    }

    /**
     * Gets IDs in database for times for the provided medication.
     * @param medication Medication whose time IDs must be retrieved.
     * @return An array of time ids.
     */
    public long[] getMedicationTimeIds(Medication medication)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + TIME_ID + " FROM " + MEDICATION_TIMES + " WHERE " + MED_ID
                + " = " + medication.getId();

        Cursor cursor = db.rawQuery(query, null);

        long[] timeIds = new long[cursor.getCount()];

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++)
        {
            timeIds[i] = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(TIME_ID)));
            cursor.moveToNext();
        }

        cursor.close();

        return timeIds;
    }

    /**
     * Stores the maximum amount of time before which a dose cannot be marked taken.
     * @param hoursBeforeDose Number of hours before which a medication can be marked taken.
     */
    public void setTimeBeforeDose(int hoursBeforeDose)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(TIME_BEFORE_DOSE, hoursBeforeDose);

        db.update(SETTINGS_TABLE, cv, null, null);
    }

    /**
     * Retrieves the amount of time before a dose can be marked as taken.
     * @return the number of hours before which a dose cannot be marked taken.
     */
    public int getTimeBeforeDose()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = " SELECT " + TIME_BEFORE_DOSE + " FROM " + SETTINGS_TABLE;

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        int timeBefore = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(TIME_BEFORE_DOSE)));

        cursor.close();

        return timeBefore;
    }

    /**
     * Saves user's choice to allow notifications or not.
     * @param status true if allowed, else false.
     */
    public void setNotificationEnabled(boolean status)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(ENABLE_NOTIFICATIONS, status);

        db.update(SETTINGS_TABLE, cv, null, null);
    }

    /**
     * Retrieves user's notification preference.
     * @return true of notifications are enabled in Settings.
     */
    public boolean getNotificationEnabled()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = " SELECT " + ENABLE_NOTIFICATIONS + " FROM " + SETTINGS_TABLE;

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        boolean enabled
                = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(ENABLE_NOTIFICATIONS))) == 1;

        cursor.close();

        return enabled;
    }

    /**
     * Retrieves theme saved by user.
     * @return User's preferred theme.
     */
    public String getSavedTheme()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + THEME + " FROM " + SETTINGS_TABLE;
        String theme;

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        theme = cursor.getString(cursor.getColumnIndexOrThrow(THEME));

        cursor.close();

        return theme;
    }

    /**
     * Saves user's chosen theme (light, dark, default).
     * @param theme User's preferred theme.
     */
    public void saveTheme(String theme)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(THEME, theme);

        db.update(SETTINGS_TABLE, cv, null, null);
    }

    /**
     * Retrieves the time when a dose was taken.
     * @param doseId ID of dose whose time is sought.
     * @return The time the dose was marked taken or null.
     */
    public LocalDateTime getTimeTaken(long doseId)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        LocalDateTime time;
        String query = "SELECT " + TIME_TAKEN + " FROM " + MEDICATION_TRACKER_TABLE + " WHERE "
                + DOSE_ID + " = " + doseId;

        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        time = cursor.getCount() > 0 ?
                TimeFormatting.stringToLocalDateTime(
                    cursor.getString(cursor.getColumnIndexOrThrow(TIME_TAKEN))
                ) : null;

        cursor.close();

        return time;
    }

    /**
     * Pauses or resumes a chosen medication.
     * @param medication medication to pause or resume.
     * @param active true if making active, false if pausing
     */
    public void pauseResumeMedication(Medication medication, boolean active)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues updateActivityStatusCv = new ContentValues();
        ContentValues addStatusChangeCv = new ContentValues();
        String where = MED_ID + " = ?";

        updateActivityStatusCv.put(ACTIVE, active ? 1 : 0);

        db.update(MEDICATION_TABLE, updateActivityStatusCv, where, new String[] {String.valueOf(medication.getId())});

        addStatusChangeCv.put(PAUSED, active ? 0 : 1);
        addStatusChangeCv.put(CHANGE_DATE, TimeFormatting.localDateTimeToString(LocalDateTime.now()));
        addStatusChangeCv.put(MED_ID, medication.getId());

        db.insert(ACTIVITY_CHANGE_TABLE, "", addStatusChangeCv);
    }

    /**
     * Retrieves active status of medication
     * @param medication Medication to check if is active.
     * @return true if active, false if paused.
     */
    public boolean isMedicationActive(Medication medication)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + ACTIVE + " FROM " + MEDICATION_TABLE
                + " WHERE " + MED_ID + "=" + medication.getId();
        boolean active;

        Cursor cursor =  db.rawQuery(query, null);
        cursor.moveToFirst();

        active = cursor.getString(cursor.getColumnIndexOrThrow(ACTIVE)).equals("1");

        cursor.close();

        return active;
    }

    public ArrayList<Pair<LocalDateTime, LocalDateTime>> getPauseResumePeriods(Medication medication)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Pair<LocalDateTime, LocalDateTime>> intervals = new ArrayList<>();
        ArrayList<LocalDateTime> timesPaused = new ArrayList<>();
        ArrayList<LocalDateTime> timesResumed = new ArrayList<>();
        boolean prevWasPaused = true;

        Cursor cursor;

        cursor = db.rawQuery(
                "SELECT * FROM " + ACTIVITY_CHANGE_TABLE
                        + " WHERE " + MED_ID + "=" + medication.getId(),
                null
        );
        cursor.moveToFirst();

        while(!cursor.isAfterLast())
        {
            LocalDateTime time;

            time = TimeFormatting.stringToLocalDateTime(
                    cursor.getString(cursor.getColumnIndexOrThrow(CHANGE_DATE))
            );

            if (cursor.isFirst())
            {
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(PAUSED))) == 1)
                {
                    timesPaused.add(time);
                    prevWasPaused = true;
                }
                else if (Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(PAUSED))) == 0)
                {
                    timesResumed.add(time);
                    prevWasPaused = false;
                }
            }
            else
            {
                if (prevWasPaused)
                {
                    timesResumed.add(time);
                }
                else
                {
                    timesPaused.add(time);
                }

                prevWasPaused = !prevWasPaused;
            }


            cursor.moveToNext();
        }

        cursor.close();

        if (timesPaused.size() == 0 && timesResumed.size() == 1)
        {
            intervals.add(new Pair<>(null, timesResumed.get(0)));

            return intervals;
        }
        else if (timesPaused.size() == 1 && timesResumed.size() == 0)
        {
            intervals.add(new Pair<>(timesPaused.get(0), null));

            return intervals;
        }

        for (int i = 0; i < timesPaused.size(); i++)
        {
            if (timesResumed.size() - 1 >= i)
            {
                intervals.add(new Pair<>(timesPaused.get(i), timesResumed.get(i)));
            }
            else
            {
                intervals.add(new Pair<>(timesPaused.get(i), null));
            }
        }

        return intervals;
    }
}
