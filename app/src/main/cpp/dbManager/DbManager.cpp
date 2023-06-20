//
// Created by adam on 6/18/23.
//

#include "DbManager.h"

DbManager::DbManager(jstring fileDescriptor) {
    database_name = fileDescriptor;
}

void DbManager::open() { sqlite3_open16(database_name, &db); }
void DbManager::close() { sqlite3_close(db); }

int DbManager::read() {
    int rc = 0;

    try {
        rc = sqlite3_exec(db, "SELECT * FROM Medication", callback, 0,
                          (char **) "Failed to retrieve data.");
    } catch (exception e) {
        std::cerr << e.what() << std::endl;
    }

    return rc;
}
