//
// Created by adam on 6/18/23.
//

#include "DbManager.h"

DbManager::DbManager() {}

DbManager::DbManager(string databasePath, bool enableForeignKeys) {
    char* err;

    database_name = std::move(databasePath);
    openDb();

    if (enableForeignKeys) {
        sqlite3_exec(db, "PRAGMA foreign_keys = ON", nullptr, nullptr, &err);
    }
}

DbManager::~DbManager() {
    closeDb();
}

string DbManager::escapeUnsafeChars(string str) {
    if (str.find(' ') != string::npos) {
        replaceAll(str, " ", "&dbsp;");
    }

    if(str.find('\"') != string::npos) {
        replaceAll(str, "\"", "&qout;");
    }

    if (str.find('{') != string::npos) {
        replaceAll(str, "{", "&#123;");
    }

    if (str.find('}') != string::npos) {
        replaceAll(str, "}", "&#125;");
    }

    if (str.find(':') != string::npos) {
        replaceAll(str, ":", "&#58;");
    }

    if (str.find(',') != string::npos) {
        replaceAll(str, ",", "&#130;");
    }

    return str;
}

string DbManager::unescapeSafeChars(string str) {
    if (str.find("&dbsp;") != string::npos) {
        replaceAll(str, "&dbsp;", " ");
    }

    if(str.find("&qout;") != string::npos) {
        replaceAll(str, "&qout;", "\"");
    }

    if (str.find("&#123;") != string::npos) {
        replaceAll(str, "&#123;", "{");
    }

    if (str.find("&#125;") != string::npos) {
        replaceAll(str, "&#125;", "}");
    }

    if (str.find("&#58;") != string::npos) {
        replaceAll(str, "&#58;", ":");
    }

    if (str.find("&#130;") != string::npos) {
        replaceAll(str, "&#130;", ",");
    }

    return str;
}

void DbManager::replaceAll(string &str, const string& from, const string& to) {
    size_t start_pos = 0;
    while((start_pos = str.find(from, start_pos)) != std::string::npos) {
        str.replace(start_pos, from.length(), to);
        start_pos += to.length();
    }
}

void DbManager::openDb() {
    const int rc = sqlite3_open(database_name.c_str(), &db);

    if (rc != SQLITE_OK) {
        throw runtime_error("Failed to open database file at: " + database_name);
    }
}
void DbManager::closeDb() { sqlite3_close(db); }

int DbManager::getVersionNumber() {
    sqlite3_stmt *stmt = nullptr;
    int version;
    int rc = sqlite3_prepare_v2(db, "PRAGMA version_number", -1, &stmt, nullptr);

    if (rc != SQLITE_OK) {
        throw runtime_error("An error occurred while querying version_number");
    }

    sqlite3_step(stmt);

    version = atoi(reinterpret_cast<const char *>(sqlite3_column_text(stmt, 0)));

    sqlite3_finalize(stmt);

    return version;
}

vector<string> DbManager::getTables(const vector<string>& ignoreTables) {
    int rc;
    sqlite3_stmt *stmt;
    string query = "SELECT name FROM sqlite_schema WHERE type ='table' AND name NOT LIKE 'sqlite_%'";
    vector<string> tables;

    for (auto &tbl : ignoreTables) {
        query += " AND name != '" + tbl + "'";
    }

    rc = sqlite3_prepare_v2(db, query.c_str(), -1, &stmt, nullptr);

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

    rc = sqlite3_prepare_v2(db, query.c_str(), -1, &stmt, nullptr);
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

                m.insert({ string(sqlite3_column_name(stmt, i)),string(colText) });
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

map<string, vector<map<string, string>>> DbManager::getAllRowFromAllTables(const vector<string>& ignoreTables) {
    map<string, vector<map<string, string>>> tableData;
    vector<string> tables = getTables(ignoreTables);

    for (const string& tbl : tables) {
        tableData.insert({ tbl, readAllValuesInTable(tbl) });
    }

    return tableData;
}

void DbManager::exportData(const string& exportFilePath, const vector<string>& ignoreTables) {
    ofstream outFile;
    map<string, vector<map<string, string>>> data = getAllRowFromAllTables(ignoreTables);
    string outData;

    try {
        outFile.open(exportFilePath, fstream::trunc);

        if (!outFile.is_open()) {
            throw runtime_error("Could not open file: " + exportFilePath);
        }
    } catch (exception& e) {
        cerr << e.what() << endl;

        throw runtime_error("Could not open file: " + exportFilePath);
    }

    outFile << "{";

    for (const auto& tbl : data) {
        outData += "\"" + tbl.first + "\":[";


        for (const auto& tblInfo : tbl.second) {
            outData += "{";

            for (const auto& col : tblInfo) {
                outData += "\"" + col.first + "\":\"" + escapeUnsafeChars(col.second) + "\",";
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

void DbManager::importData(const std::string &importFilePath, const vector<string>& ignoreTables) {
    fstream fin;
    string inData;
    map<string, vector<map<string, string>>> data;
    vector<string> tables = getTables(ignoreTables);
    stringstream importQuery;
    char* err;

    try {
        if (importFilePath.substr(importFilePath.find_last_of('.') + 1) != "json") {
            throw runtime_error("Provided file is not a JSON file");
        }

        fin.open(importFilePath);

        if (!fin.is_open()) { throw runtime_error("Import file failed to open"); }

        inData = string(istreambuf_iterator<char>{fin}, {});

        fin.close();
    } catch (runtime_error& error) {
        cerr << error.what() << ": " << importFilePath << endl;

        throw error;
    }

    // Remove unneeded chars
    inData.erase(
            remove_if(inData.begin(), inData.end(), [](unsigned char x) { return std::isspace(x); }),
            inData.end()
    );

    inData.erase(0, 1);
    inData.erase(inData.end() - 1, inData.end());

    inData.erase(
            remove_if(inData.begin(), inData.end(), [](unsigned char x) { return x == '\"'; }),
            inData.end()
    );

    try {
        for (const string &tbl : tables) {
            vector<map<string, string>> table;
            size_t pos;
            string tblStr;
            size_t tblStart = inData.find(tbl + ":[");
            size_t endTblData = inData.find(']', tblStart + string(tbl + ":[").size());

            if (tblStart == string::npos) {
                throw runtime_error("Table \"" + tbl + "\" not found in input file");
            }

            tblStr = inData.substr(tblStart, endTblData - tblStart);

            tblStr.erase(0, (tbl + ":[").size());

            if (tblStr.empty()) {
                data.insert({ tbl, {} });
                continue;
            }

            tblStr.erase(0, 1);
            tblStr.erase(tblStr.size() - 1, 1);

            int ind = 0;
            table.resize(count(tblStr.begin(), tblStr.end(), '}') + 1);

            while ((pos = tblStr.find(',')) != string::npos || tblStr.length() > 0) {
                unsigned int end = tblStr.find(',') != string::npos ? tblStr.find(',') : tblStr.length() - 1;
                string token = tblStr.substr(0, end);
                bool incrementInd = false;

                if (token.at(0) == '{') token.erase(0, 1);
                if (token.at(token.size() - 1) == '}') {
                    token.erase(token.find('}'), 1);
                    incrementInd = true;
                }

                pair<string, string> col = {
                        token.substr(0, token.find(':')),
                        unescapeSafeChars(token.substr(token.find(':') + 1, token.size() - 1))
                };

                table.at(ind).insert(col);

                if (incrementInd) ind++;
                if (pos != string::npos) {
                    tblStr.erase(0, pos + 1);
                } else {
                    tblStr = "";
                }
            }

            data.insert({tbl, table });
        }
    } catch (exception& e) {
        cerr << e.what() << endl;

        throw e;
    }

    importQuery << "BEGIN TRANSACTION;";

    for (const string &tbl : tables) {
        importQuery << "DELETE FROM " << tbl << ';';
    }

    for (const auto& tbl : data) {
        if (tbl.second.empty()) continue;

        importQuery << "INSERT INTO "
                    << tbl.first
                    << "(";

        for (auto& col : tbl.second.at(0)) {
            importQuery << col.first << ',';
        }

        importQuery.seekp(-1, ios_base::end);
        importQuery << ") VALUES ";

        for (auto& row : tbl.second) {
            importQuery << '(';
            for (auto& col : row) {
                if (isNumber(col.second)) {
                    importQuery << col.second << ',';
                } else if (col.second.empty()) {
                    importQuery << "NULL,";
                } else {
                    importQuery << "\"" << col.second << "\"" << ',';
                }
            }
            importQuery.seekp(-1, ios_base::end);
            importQuery << "),";
        }

        importQuery.seekp(-1, ios_base::end);

        importQuery << ";";
    }

    importQuery << "COMMIT;";

    try {
        const int retVal = sqlite3_exec(db, importQuery.str().c_str(), nullptr, nullptr, &err);

        if (retVal != SQLITE_OK) {
            throw runtime_error(err);
        }
    } catch (exception& e) {
        cerr << e.what() << endl;

        string error = "SQLite Error: ";
        error += err;

        throw runtime_error(error);
    }
}

bool DbManager::isNumber(string str) {
    string::const_iterator it = str.begin();
    while (it != str.end() && std::isdigit(*it)) ++it;

    return !str.empty() && it == str.end();
}
