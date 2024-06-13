//
// Created by adam on 12/23/23.
//

#include "DatabaseController.h"

DatabaseController::DatabaseController(string path) {
    manager = DbManager(path, true);

    manager.openDb();

    int currentVersion = manager.getVersionNumber();

    if (currentVersion <= 1) {
        create();
    } else if (DB_VERSION > currentVersion) {
        upgrade(currentVersion);
    }

    if (currentVersion != DB_VERSION) {
        manager.execSql("PRAGMA schema_version = " + to_string(DB_VERSION));
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
        + INSTRUCTIONS + " TEXT,"
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
        + OVERRIDE_DOSE_AMOUNT + " INT,"
        + OVERRIDE_DOSE_UNIT + " TEXT,"
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

    manager.execSql(
            "CREATE TABLE IF NOT EXISTS " + NOTIFICATIONS + "("
            + NOTIFICATION_ID + " INT PRIMARY KEY,"
            + MED_ID + " INT, "
            + DOSE_ID + " INT, "
            + SCHEDULED_TIME + " DATETIME);"
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

    if (currentVersion < 13) {
        manager.execSql(
                "CREATE TABLE IF NOT EXISTS " + NOTIFICATIONS + "("
                + NOTIFICATION_ID + " INT PRIMARY KEY,"
                + MED_ID + " INT, "
                + DOSE_ID + " INT, "
                + SCHEDULED_TIME + " DATETIME);"
        );


        manager.execSql("ALTER TABLE " + MEDICATION_TRACKER_TABLE + " ADD COLUMN " + OVERRIDE_DOSE_AMOUNT + " INT;");
        manager.execSql("ALTER TABLE " + MEDICATION_TRACKER_TABLE + " ADD COLUMN " + OVERRIDE_DOSE_UNIT + " TEXT;");
        manager.execSql("ALTER TABLE " + MEDICATION_TABLE + " ADD COLUMN " + INSTRUCTIONS + " TEXT;");
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

void DatabaseController::exportCsv(string exportPath, map<string, vector<string>> data) {
    ofstream file(exportPath, std::fstream::trunc);
    stringstream fileContents;
    int longestValue = 0;

    for (auto& item : data) {
        fileContents << item.first << ',';
        if (item.second.size() > longestValue) {
            longestValue = item.second.size();
        }
    }

    fileContents.seekp(-1, std::ios_base::end);
    fileContents << '\n';

    for (int i = 0; i < longestValue; i++) {
        for (auto& item : data) {
            if (!item.second.at(i).empty()) {
                fileContents << item.second.at(i);
            } else {
                fileContents << "";
            }

            fileContents << ',';
        }

        fileContents.seekp(-1, std::ios_base::end);
        fileContents << '\n';
    }

    file << fileContents.rdbuf();
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

    medication.doses = getTakenDoses(medication.id);

    return medication;
}

vector<Dose> DatabaseController::getTakenDoses(long medicationId) {
    string query = "SELECT * FROM " + MEDICATION_TRACKER_TABLE
                   + " WHERE " + MED_ID + "=" + to_string(medicationId)
                   + " AND " + TAKEN + " = TRUE";
    vector<Dose> doses;

    Table* table = manager.execSqlWithReturn(query);

    while (table->getCount() > 0 && !table->isAfterLast()) {
        int overrideDose = -1;
        string overrideUnit = "";

        if (!empty(table->getItem(OVERRIDE_DOSE_AMOUNT))) {
            overrideDose = stoi(table->getItem(OVERRIDE_DOSE_AMOUNT));
        }

        if (!empty(table->getItem(OVERRIDE_DOSE_UNIT))) {
            overrideUnit = table->getItem(OVERRIDE_DOSE_UNIT);
        }

        doses.push_back(
            Dose(
                stol(table->getItem(DOSE_ID)),
                stol(table->getItem(MED_ID)),
                table->getItem(TAKEN) == "1",
                table->getItem(DOSE_TIME),
                table->getItem(TIME_TAKEN),
                overrideDose,
                overrideUnit
            )
        );

        table->moveToNext();
    }

    delete table;

    return doses;
}

Dose* DatabaseController::setDose(Table* table) {
    Dose* dose = nullptr;

    if (table->getCount() > 0) {
        table->moveToFirst();

        int overrideDose = -1;
        string overrideUnit = "";

        if (!empty(table->getItem(OVERRIDE_DOSE_AMOUNT))) {
            overrideDose = stoi(table->getItem(OVERRIDE_DOSE_AMOUNT));
        }

        if (!empty(table->getItem(OVERRIDE_DOSE_UNIT))) {
            overrideUnit = table->getItem(OVERRIDE_DOSE_UNIT);
        }

        dose = new Dose(
                stol(table->getItem(DOSE_ID)),
                stol(table->getItem(MED_ID)),
                table->getItem(TAKEN) == "1",
                table->getItem(DOSE_TIME),
                table->getItem(TIME_TAKEN),
                overrideDose,
                overrideUnit
        );
    }

    return dose;
}

Dose* DatabaseController::findDose(long medicationId, std::string scheduledTime) {
    Table* result = manager.execSqlWithReturn(
        "SELECT * FROM " + MEDICATION_TRACKER_TABLE
        + " WHERE " + MED_ID + "=" + to_string(medicationId)
        + " AND " + DOSE_TIME + "='" + scheduledTime +"'"
        + " AND " + TAKEN + " = TRUE"
    );

    Dose* dose = setDose(result);

    delete result;

    return dose;
}

Dose* DatabaseController::getDoseById(long doseId) {
    Table* result = manager.execSqlWithReturn(
      "SELECT * FROM " + MEDICATION_TRACKER_TABLE
      + " WHERE " + DOSE_ID + "=" + to_string(doseId)
    );

    Dose* dose = setDose(result);

    delete result;

    return dose;
}

bool DatabaseController::updateDose(Dose dose) {
    map<string, string> values;

    values.insert(pair<string, string>(TIME_TAKEN, dose.timeTaken));
    values.insert(pair<string, string>(DOSE_TIME, dose.doseTime));

    if (dose.overrideDoseAmount != -1) {
        values.insert(pair<string, string>(OVERRIDE_DOSE_AMOUNT, to_string(dose.overrideDoseAmount)));
    }

    if (!dose.overrideDoseUnit.empty()) {
        values.insert(pair<string, string>(DOSE_TIME, dose.overrideDoseUnit));
    }

    try {
        manager.update(
                MEDICATION_TRACKER_TABLE,
                values,
                {pair<string, string>(DOSE_ID, to_string(dose.id))}
        );

        return true;
    } catch (exception e) {
        cerr << "Failed to update dose " << dose.id << endl;

        return false;
    }
}
