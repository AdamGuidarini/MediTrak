//
// Created by adam on 12/23/23.
//

#include "DatabaseController.h"

DatabaseController::DatabaseController(string path) {
    manager = DbManager(path, true);

    manager.openDb();

    int currentVersion = manager.getVersionNumber();

    if (currentVersion == 0) {
        create();
    } else if (DB_VERSION > currentVersion) {
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
            + DATE_FORMAT + " TEXT DEFAULT '" + DateFormats::MM_DD_YYYY + "',"
            + TIME_FORMAT + " TEXT DEFAULT '" + TimeFormats::_12_HOUR + "',"
            + SEEN_NOTIFICATION_REQUEST + " BOOLEAN DEFAULT 0);"
    );

    manager.execSql(
            "INSERT INTO " + SETTINGS_TABLE + " (" + TIME_BEFORE_DOSE + ")"
            + " VALUES (2);"
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

    if (currentVersion < 10) {
        manager.execSql("ALTER TABLE " + NOTES_TABLE + " ADD COLUMN " + TIME_EDITED + " DATETIME;");
    }

    if (currentVersion < 11) {
        manager.execSql("ALTER TABLE " + SETTINGS_TABLE + " ADD COLUMN " + DATE_FORMAT + " TEXT DEFAULT '" + DateFormats::MM_DD_YYYY + "';");
        manager.execSql("ALTER TABLE " + SETTINGS_TABLE + " ADD COLUMN " + TIME_FORMAT + " TEXT DEFAULT '" + TimeFormats::_12_HOUR + "';");
    }

    manager.execSql("PRAGMA schema_version = " + to_string(DB_VERSION));
}

long DatabaseController::insert(string table, map<string, string> values) {
    return manager.insert(table, values);
}

void DatabaseController::update(string table, map<string, string> values, map<string, string> where) {
    manager.update(table, values, where);
}

void DatabaseController::deleteRecord(string table, map<string, string> where) {
    manager.deleteRecord(table, where);
}

void DatabaseController::updateSettings(map<std::string, std::string> values) {
    manager.update(SETTINGS_TABLE, values, {});
}

void DatabaseController::exportJSON(
        const string &exportFilePath,
        const vector<string> &ignoreTables
) {
    manager.exportData(exportFilePath, ignoreTables);
}

void DatabaseController::importJSONFile(
        const string &importFilePath,
        const vector<string> &ignoreTables
) {
    manager.importDataFromFile(importFilePath, ignoreTables);
}

void DatabaseController::importJSONString(
        string &data,
        const vector<string> &ignoreTables
) {
    manager.importData(data, ignoreTables);
}

Medication DatabaseController::getMedication(long medicationId) {
    string query = "SELECT * FROM " + MEDICATION_TABLE + " m "
            + " INNER JOIN " + MEDICATION_TIMES + " mt "
            + " ON " + "m." + MED_ID + "= mt." + MED_ID
            + " WHERE m." + MED_ID + "=" + to_string(medicationId);
    Medication medication;
    Table* table = manager.execSqlWithReturn(query);
    long parentId = 0;
    vector<string> times;

    table->moveToFirst();

    medication = Medication(
            table->getItem(MED_NAME),
            table->getItem(PATIENT_NAME),
            table->getItem(MED_UNITS),
            {},
            table->getItem(START_DATE),
            stol(table->getItem(MED_ID)),
            stoi(table->getItem(MED_DOSAGE)),
            stoi(table->getItem(MED_FREQUENCY)),
            table->getItem(ACTIVE) == "1",
            table->getItem(ALIAS)
    );

    if (!table->getItem(PARENT_ID).empty()) {
        parentId = stol(table->getItem(PARENT_ID));
    }

    while (!table->isAfterLast()) {
        times.push_back(table->getItem(DRUG_TIME));

        table->moveToNext();
    }

    medication.times = std::move(times);

    delete table;

    if (parentId > 0) {
        Medication parent = getMedication(parentId);
        medication.parent = make_shared<Medication>(parent);
        medication.parent->child = make_shared<Medication>(medication);
    }

    return medication;
}

vector<Dose> DatabaseController::getDoses(Medication medication) {
    shared_ptr<Medication> nextMed = medication.parent;
    vector<long> ids = {medication.id};
    vector<Dose> doses;

    while (nextMed != nullptr) {
        ids.push_back(nextMed->id);

        nextMed = nextMed->parent;
    }

    return getDoses(ids);
}

vector<Dose> DatabaseController::getDoses(long medId) {
    vector<long> ids = {medId};

    return getDoses(ids);
}

vector<Dose> DatabaseController::getDoses(vector<long> medicationIds) {
    string query = "SELECT * FROM " + MEDICATION_TRACKER_TABLE
                   + " WHERE " + MED_ID + "=" + to_string(medicationIds.at(0));
    vector<Dose> doses;

    for (int i = 1; i < medicationIds.size(); i++) {
        query += " OR " + MED_ID + "=" + to_string(medicationIds.at(i));
    }

    Table* table = manager.execSqlWithReturn(query);

    while (!table->isAfterLast() && table->getCount() > 0) {
        doses.push_back(
            Dose(
                stol(table->getItem(DOSE_ID)),
                stol(table->getItem(MED_ID)),
                table->getItem(TAKEN) == "1",
                table->getItem(DOSE_TIME),
                table->getItem(TIME_TAKEN)
            )
        );

        table->moveToNext();
    }

    delete table;

    return doses;
}