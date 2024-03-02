//
// Created by adam on 12/23/23.
//

#ifndef MEDICATIONTRACKER_DATABASECONTROLLER_H
#define MEDICATIONTRACKER_DATABASECONTROLLER_H

#include <string>
#include <vector>
#include "../DbManager/DbManager.h"

using namespace std;

namespace TimeFormats {
    const string _12_HOUR = "hh:mm a";
    const string _24_HOUR = "HH:mm";
}

namespace DateFormats {
    const string MM_DD_YYYY = "MM/dd/yyyy";
    const string DD_MM_YYYY = "dd/MM/yyyy";
}

class DatabaseController {
private:
    const int DB_VERSION = 11;
    const string DATABASE_NAME = "Medications.db";
    vector<string> tablesToIgnore;
    DbManager manager;
    
    const string ANDROID_METADATA = "android_metadata";

    const string MEDICATION_TABLE = "Medication";
    const string MED_ID = "MedicationID";
    const string MED_NAME = "MedName";
    const string PATIENT_NAME = "PatientName";
    const string MED_DOSAGE = "Dosage";
    const string MED_UNITS = "Units";
    const string ALIAS = "Alias";
    const string ACTIVE = "Active";
    const string PARENT_ID = "ParentId";
    const string CHILD_ID = "ChildId";

    const string MEDICATION_TRACKER_TABLE = "MedicationTracker";
    const string DOSE_TIME = "DoseTime";
    const string TAKEN = "Taken";
    const string TIME_TAKEN = "TimeTaken";
    const string DOSE_ID = "DoseID";
    const string MED_FREQUENCY = "DrugFrequency";

    const string MEDICATION_TIMES = "MedicationTimes";
    const string TIME_ID = "TimeID";
    const string DRUG_TIME = "DrugTime";

    const string MEDICATION_STATS_TABLE = "MedicationStats";
    const string START_DATE = "StartDate";

    // Notes
    const string NOTE_ID = "NoteID";
    const string NOTE = "Note";
    const string ENTRY_TIME = "EntryTime";
    const string TIME_EDITED = "TimeEdited";

    // Settings
    const string TIME_BEFORE_DOSE = "TimeBeforeDose";
    const string ENABLE_NOTIFICATIONS = "EnableNotifications";
    const string SEEN_NOTIFICATION_REQUEST = "SeenNotificationRequest";
    const string THEME = "Theme";
    const string DEFAULT = "default";
    const string LIGHT = "light";
    const string DARK = "dark";
    const string AGREED_TO_TERMS = "AgreedToTerms";
    const string DATE_FORMAT = "DateFormat";
    const string TIME_FORMAT = "TimeFormat";

    // Activity Change
    const string CHANGE_EVENT_ID = "ChangeId";
    const string CHANGE_DATE = "ChangeDate";
    const string PAUSED = "Paused";

    /**
     * Handles creation of database - Called when new instance is create & DB version == 0
     */
    void create();

public:
    const string NOTES_TABLE = "Notes";
    const string SETTINGS_TABLE = "Settings";
    const string ACTIVITY_CHANGE_TABLE = "ActivityChanges";

    /**
     * Class constructor
     * @param dbPath
     */
    DatabaseController(string path);

    /**
     * Class destructor
     */
    ~DatabaseController();

    /**
     * Upgrades database to a newer version
     * @param currentVersion version of database currently stored
     */
    void upgrade(int currentVersion);

    /**
     * Insert a record into the database
     * @param table Table in which to add a row
     * @param values Values to add to new row
     * @return Row id of added row
     */
    long insert(string table, map<string, string> values);

    /**
     * Updates rows matching where clause with with new values
     * @param table Table in which to update records
     * @param values Values to change where key is the column name and value is the updated value
     * @param where Where clause arguments where the key is the column to check and value is the value to matc
     */
    void update(string table, map<string, string> values, map<string, string> where);

    /**
     * Deletes records from the database matching the where arguments
     * @param table Table in which to add a row
     * @param values Values to add to new row
     */
    void deleteRecord(string table, map<string, string> where);

    /**
     * Updates Settings
     * @param values Values to update
     * @return true on success
     */
    void updateSettings(map<string, string> values);

    /**
     * Exports all data stored in DB to provided location as a JSON file.
     * @param exportFilePath Location for file storing database backup with its name ex. /src/myDir/data.json.
     * @param Optional ignoreTables Array of tables to exclude.
     */
    void exportJSON(const string& exportFilePath, const vector<string>& ignoreTables = {});

    /**
     * Imports data from JSON file and writes it to database,
     *  throws an error if attempting to write to a table or
     *  column that does not exist in the provided database/table.
     * @param importFilePath Path to JSON file storing data to import.
     */
    void importJSON(const string& importFilePath, const vector<string>& ignoreTables = {});
};


#endif //MEDICATIONTRACKER_DATABASECONTROLLER_H
