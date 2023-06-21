//
// Created by adam on 6/18/23.
//

#include "DbManager.h"

DbManager::DbManager(string fileDescriptor) {
    database_name = fileDescriptor;
//    medication_table_res = vector<map<string, string>>();
}

void DbManager::open() { sqlite3_open(database_name.c_str(), &db); }
void DbManager::close() { sqlite3_close(db); }

int DbManager::read() {
    sqlite3_stmt *stmt = nullptr;

    sqlite3_prepare(db, "SELECT * FROM Medication;", -1, &stmt, nullptr);
    sqlite3_step(stmt);

    while (sqlite3_column_text(stmt, 0)) {
        for (int i = 0; i < sqlite3_column_count(stmt); i++) {
            map<string, string> m;

            m.insert(
                {
                    string(sqlite3_column_name(stmt, i)),
                    string(reinterpret_cast<const char*>(sqlite3_column_text(stmt, i)))
                }
            );

            medication_table_res.push_back(m);
        }

        sqlite3_step(stmt);
    }

    sqlite3_finalize(stmt);

    return 0;
}
