//
// Created by adam on 12/23/23.
//

#include "DatabaseController.h"

DatabaseController::DatabaseController(string path) {
    manager = DbManager(path, true);

    manager.openDb();
    create();

    int currentVersion = manager.getVersionNumber();

    if (DB_VERSION > currentVersion) {
        upgrade(currentVersion);
    }
}

DatabaseController::~DatabaseController() {}

void DatabaseController::create() {
    manager.execSql("CREATE TABLE IF NOT EXISTS " + MEDICATION_TABLE + "("
        + MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + MED_NAME + " TEXT,"
        + PATIENT_NAME + " Text,"
        + MED_DOSAGE + " DECIMAL(3,2),"
        + MED_UNITS + " TEXT,"
        + START_DATE + " DATETIME,"
        + MED_FREQUENCY + " INT,"
        + ALIAS + " TEXT,"
        + ACTIVE + " BOOLEAN DEFAULT 1,"
        + PARENT_ID + " INTEGER,"
        + CHILD_ID + " INTEGER,"
        + "FOREIGN KEY (" + PARENT_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE,"
        + "FOREIGN KEY (" + CHILD_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE"
        + ");"
    );

    manager.execSql("CREATE TABLE IF NOT EXISTS " + MEDICATION_TRACKER_TABLE + "("
        + DOSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + MED_ID + " INT,"
        + DOSE_TIME + " DATETIME,"
        + TAKEN + " BOOLEAN,"
        + TIME_TAKEN + " DATETIME,"
        + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE"
        + ");"
    );

    manager.execSql(
            "CREATE TABLE IF NOT EXISTS " + MEDICATION_TIMES + "("
            + TIME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + MED_ID + " INT,"
            + DRUG_TIME + " TEXT,"
            + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE"
            + ");"
    );

    manager.execSql(
            "CREATE TABLE IF NOT EXISTS " + NOTES_TABLE + "("
            + NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + MED_ID + " INT, "
            + NOTE + " TEXT, "
            + ENTRY_TIME + " DATETIME,"
            + TIME_EDITED + " DATETIME,"
            + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE"
            + ");"
    );

    manager.execSql(
            "CREATE TABLE IF NOT EXISTS " + SETTINGS_TABLE + "("
            + TIME_BEFORE_DOSE + " INT DEFAULT 2, "
            + ENABLE_NOTIFICATIONS + " BOOLEAN DEFAULT 1, "
            + THEME + " TEXT DEFAULT '" + DEFAULT + "',"
            + AGREED_TO_TERMS + " BOOLEAN DEFAULT 0,"
            + SEEN_NOTIFICATION_REQUEST + " BOOLEAN DEFAULT 0);"
    );

    manager.execSql("INSERT INTO " + SETTINGS_TABLE + "("
                + ENABLE_NOTIFICATIONS + ", " + TIME_BEFORE_DOSE + ")"
                + "VALUES (1, 2);"
    );

    manager.execSql(
            "CREATE TABLE IF NOT EXISTS " + ACTIVITY_CHANGE_TABLE + "("
            + CHANGE_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + MED_ID + " INT,"
            + CHANGE_DATE + " DATETIME,"
            + PAUSED + " BOOLEAN,"
            + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE"
            + ");"
    );

    manager.execSql("PRAGMA schema_version = " + to_string(DB_VERSION));
}

void DatabaseController::upgrade(int currentVersion) {
    if (currentVersion < 2) {
        manager.execSql("ALTER TABLE " + MEDICATION_TABLE + " ADD COLUMN " + ACTIVE + " BOOLEAN DEFAULT 1;");
    }

    if (currentVersion < 3) {
        manager.execSql("DROP TABLE IF EXISTS " + MEDICATION_STATS_TABLE + ";");
    }

    if (currentVersion < 4) {
        manager.execSql(
                "CREATE TABLE IF NOT EXISTS " + ACTIVITY_CHANGE_TABLE + "("
                + CHANGE_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MED_ID + " INT,"
                + CHANGE_DATE + " DATETIME,"
                + PAUSED + " BOOLEAN,"
                + "FOREIGN KEY (" + MED_ID + ") REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE"
                + ");"
                );
    }

    if (currentVersion < 5) {
        manager.execSql("ALTER TABLE " + SETTINGS_TABLE + " ADD COLUMN " + AGREED_TO_TERMS + " BOOLEAN DEFAULT 0;");
    }

    if (currentVersion < 6) {
        manager.execSql("ALTER TABLE " + MEDICATION_TABLE + " ADD COLUMN " + PARENT_ID + " INTEGER REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE;");
        manager.execSql("ALTER TABLE " + MEDICATION_TABLE + " ADD COLUMN " + CHILD_ID + " INTEGER REFERENCES " + MEDICATION_TABLE + "(" + MED_ID + ") ON DELETE CASCADE;");
    }

    if (currentVersion < 7) {
        manager.execSql("ALTER TABLE " + SETTINGS_TABLE + " ADD COLUMN " + SEEN_NOTIFICATION_REQUEST + " BOOLEAN DEFAULT 0;");
    }

    if (currentVersion < 8) {
        manager.execSql("ALTER TABLE " + NOTES_TABLE + " ADD COLUMN " + TIME_EDITED + " DATETIME;");
    }

    manager.execSql("PRAGMA schema_version = " + to_string(DB_VERSION));
}

void DatabaseController::update(string table, map<string, string> values, map<string, string> where) {
    stringstream query;

    query << "UPDATE " << table << " SET ";

    for (const auto &updates : values) {
        query << updates.first << "= \'" << updates.second << "\',";
    }

    query.seekp(-1, ios_base::end);
    query << " WHERE ";

    for (const auto &whereArgs : where) {
        query << whereArgs.first << "=\'" << whereArgs.second << "\' AND";
    }

    for (int i = 0; i < 3; i++) {
        query.seekp(-1, ios_base::end);
    }
    query << ';';

    manager.execSql(query.str());
}

void DatabaseController::exportJSON(
        const string &exportFilePath,
        const vector<string> &ignoreTables
) {
    manager.exportData(exportFilePath, ignoreTables);
}

void DatabaseController::importJSON(
        const string &importFilePath,
        const vector<string> &ignoreTables
) {
    manager.importData(importFilePath, ignoreTables);
}
