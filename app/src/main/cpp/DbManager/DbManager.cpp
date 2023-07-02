//
// Created by adam on 6/18/23.
//

#include "DbManager.h"

#include <utility>

DbManager::DbManager(string fileDescriptor) {
    database_name = std::move(fileDescriptor);
}

void DbManager::open() { sqlite3_open(database_name.c_str(), &db); }
void DbManager::close() { sqlite3_close(db); }

int DbManager::readAllValuesInTable(vector<map<string, string>>* results, const string& table) {
    sqlite3_stmt *stmt = nullptr;
    string query = "SELECT * FROM " + table + ";";
    int rc;

    rc = sqlite3_prepare(db, query.c_str(), -1, &stmt, nullptr);
    sqlite3_step(stmt);

    if (rc != SQLITE_OK) {
        sqlite3_finalize(stmt);

        return rc;
    }

    while (sqlite3_column_text(stmt, 0)) {
        map<string, string> m = map<string, string>();

        for (int i = 0; i < sqlite3_column_count(stmt); i++) {
            string colText;

            if (sqlite3_column_text(stmt, i) != nullptr) {
                colText = string(reinterpret_cast<const char*>(sqlite3_column_text(stmt, i)));
            }

            m.insert(
                    {
                            string(sqlite3_column_name(stmt, i)),
                            string(colText)
                    }
            );
        }

        results->push_back(m);
        sqlite3_step(stmt);
    }

    sqlite3_finalize(stmt);

    return rc;
}
