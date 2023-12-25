//
// Created by adam on 12/23/23.
//

#include "DatabaseController.h"

DatabaseController::DatabaseController(string path) {
    manager = DbManager(path, true);

    manager.openDb();

    int currentVersion = manager.getVersionNumber();

    if (DB_VERSION > currentVersion) {
        upgrade(currentVersion);
    }
}

DatabaseController::~DatabaseController() {}

void DatabaseController::create() {

}

void DatabaseController::upgrade(int currentVersion) {
    const vector<string> upgrades;

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

void DatabaseController::exportJSON(
        const std::string &exportFilePath,
        const vector<std::string> &ignoreTables
) {
    manager.exportData(exportFilePath, ignoreTables);
}

void DatabaseController::importJSON(
        const std::string &importFilePath,
        const vector<std::string> &ignoreTables
) {
    manager.importData(importFilePath, ignoreTables);
}
