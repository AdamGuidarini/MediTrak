//
// Created by adam on 6/18/23.
//

#include "DbManager.h"

DbManager::DbManager(string fileDescriptor) {
    database_name = std::move(fileDescriptor);
    openDb();
}

DbManager::~DbManager() {
    closeDb();
}

void DbManager::openDb() { sqlite3_open(database_name.c_str(), &db); }
void DbManager::closeDb() { sqlite3_close(db); }

vector<string> DbManager::getTables() {
    int rc;
    sqlite3_stmt *stmt = nullptr;
    string query = "SELECT name FROM sqlite_schema WHERE type ='table' AND name NOT LIKE 'sqlite_%';";
    vector<string> tables;

    rc = sqlite3_prepare(db, query.c_str(), -1, &stmt, nullptr);

    sqlite3_step(stmt);

    try {
        if (rc != SQLITE_OK) {
            sqlite3_finalize(stmt);

            throw runtime_error("Error reading from SQLite database.");
        }

        while (sqlite3_column_text(stmt, 0)) {
            string colText;

            if (sqlite3_column_text(stmt, 0) != nullptr) {
                colText = string(reinterpret_cast<const char*>(sqlite3_column_text(stmt, 0)));
            }

            tables.push_back(colText);

            sqlite3_step(stmt);
        }
    } catch (runtime_error& error) {
        cerr << error.what();
        throw error;
    }

    sqlite3_finalize(stmt);

    return tables;
}

vector<map<string, string>> DbManager::readAllValuesInTable(const string& table) {
    sqlite3_stmt *stmt = nullptr;
    string query = "SELECT * FROM " + table + ";";
    vector<map<string, string>> results;
    int rc;

    rc = sqlite3_prepare(db, query.c_str(), -1, &stmt, nullptr);
    sqlite3_step(stmt);

    try {
        if (rc != SQLITE_OK) {
            sqlite3_finalize(stmt);

            throw runtime_error("Error reading from SQLite database.");
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

            results.push_back(m);
            sqlite3_step(stmt);
        }
    } catch (runtime_error& error) {
        cerr << "SQLITE READ ERROR | Return Code: " << rc << endl;
        throw error;
    }

    sqlite3_finalize(stmt);

    return results;
}

map<string, vector<map<string, string>>> DbManager::getAllRowFromAllTables() {
    map<string, vector<map<string, string>>> tableData;
    vector<string> tables = getTables();

    for (const string& tbl : tables) {
        tableData.insert({ tbl, readAllValuesInTable(tbl) });
    }

    return tableData;
}

void DbManager::exportData(const string& exportFilePath) {
    ofstream outFile;
    map<string, vector<map<string, string>>> data = getAllRowFromAllTables();
    string outData;

    outFile.open(exportFilePath);

    outFile << "{";

    for (const auto& tbl : data) {
        outData += "\"" + tbl.first + "\":[";


        for (const auto& tblInfo : tbl.second) {
            outData += "{";

            for (const auto& col : tblInfo) {
                outData += "\"" + col.first + "\":\"" + col.second + "\",";
            }

            if (outData.at(outData.size() - 1) == ',') {
                outData.erase(outData.size() - 1);
            }

            outData += "},";
        }

        if (outData.at(outData.size() - 1) == ',') {
            outData.erase(outData.size() - 1);
        }

        outData += "],";
    }

    if (outData.at(outData.size() - 1) == ',') {
        outData.erase(outData.size() - 1);
    }

    outFile << outData << "}";

    outFile.close();
}

void DbManager::importData(const std::string &importFilePath) {
    fstream fin;
    string inData;

    try {
        fin.open(importFilePath);

        if (!fin.is_open()) { throw runtime_error("Import file failed to open"); }

        fin.close();
    } catch (runtime_error& error) {
        cerr << error.what() << ": " << importFilePath << endl;

        throw error;
    }
}
