//
// Created by adam on 6/18/23.
//

#include "DbManager.h"

DbManager::DbManager() {}

DbManager::DbManager(string databasePath, bool enableForeignKeys) {
    char *err;

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

    if (str.find('\"') != string::npos) {
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

    if (str.find("&qout;") != string::npos) {
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

void DbManager::replaceAll(string &str, const string &from, const string &to) {
    size_t start_pos = 0;
    while ((start_pos = str.find(from, start_pos)) != std::string::npos) {
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
    sqlite3_stmt *stmt;
    int version = 0;
    string query = "PRAGMA schema_version;";
    int rc;

    rc = sqlite3_prepare_v2(db, query.c_str(), -1, &stmt, nullptr);

    if (rc != SQLITE_OK) {
        throw runtime_error("An error occurred while preparing schema_version query");
    }

    rc = sqlite3_step(stmt);

    if (rc == SQLITE_ROW) {
        version = sqlite3_column_int(stmt, 0);
    } else {
        sqlite3_finalize(stmt);
        throw runtime_error("An error occurred while querying schema_version: " + to_string(rc));
    }

    sqlite3_finalize(stmt);

    return version;
}

void DbManager::execSql(string sql, int (*callback)(void *, int, char **, char **)) {
    char *err;
    int rc = sqlite3_exec(db, sql.c_str(), callback, 0, &err);

    if (rc != SQLITE_OK) {
        string errorMessage =
                "An error occurred while attempting to execute query: " + sql + "\n" + err;

        throw runtime_error(errorMessage);
    }
}

long DbManager::insert(const string &table, map<string, string> values) {
    stringstream query;
    map<string, string>::iterator it;
    sqlite3_stmt *stmt;
    long rowId = 0;

    query << "INSERT INTO " << table << " (";

    for (it = values.begin(); it != values.end();) {
        query << it->first;

        if (++it != values.end()) {
            query << ',';
        }
    }

    query << ") VALUES(";

    for (it = values.begin(); it != values.end();) {
        if (isNumber(it->second)) {
            query << it->second;
        } else if (it->second.empty()) {
            query << "NULL";
        } else {
            query << "\'" << it->second << "\'";
        }

        if (++it != values.end()) {
            query << ',';
        }
    }

    query << ");";

    execSql(query.str());

    sqlite3_prepare_v2(db, "SELECT last_insert_rowid()", -1, &stmt, nullptr);
    if (sqlite3_step(stmt) == SQLITE_ROW) {
        string colName = string(reinterpret_cast<const char *>(sqlite3_column_name(stmt, 0)));

        if (colName == "last_insert_rowid()") {
            const char* text = reinterpret_cast<const char *>(sqlite3_column_text(stmt, 0));
            if (text) {
                rowId = stol(text);
            }
        }
    }

    sqlite3_finalize(stmt);

    return rowId;
}

void DbManager::update(string table, map<string, string> values, map<string, string> where) {
    stringstream query;
    map<string, string>::iterator it;

    query << "UPDATE " << table << " SET ";

    for (it = values.begin(); it != values.end();) {
        query << it->first << "=\'" << it->second << "\'";

        if (++it != values.end()) {
            query << ',';
        }
    }

    if (!where.empty()) {
        query << " WHERE ";

        for (it = where.begin(); it != where.end();) {
            query << it->first << "=";

            if (isNumber(it->second)) {
                query << it->second;
            } else if (it->second.empty()) {
                query << "NULL";
            } else {
                query << "\'" << it->second << "\'";
            }

            if (++it != where.end()) {
                query << " AND ";
            }
        }
    }

    query << ';';

    execSql(query.str());
}

void DbManager::update(
        string table, map<string, string> values, string whereClause, string whereArgs[]
) {
    int argIndex = 0;
    stringstream query;
    map<string, string>::iterator it;
    vector<size_t> positions;
    size_t pos;

    query << "UPDATE " << table << " SET ";

    for (it = values.begin(); it != values.end();) {
        query << it->first << "=\'" << it->second << "\'";

        if (++it != values.end()) {
            query << ',';
        }
    }

    pos = whereClause.find('?', 0);
    while (pos != string::npos) {
        positions.push_back(pos);
        pos = whereClause.find('?', pos + 1);
    }

    // TODO map where args to ?s and fire query
}

void DbManager::deleteRecord(string table, map<string, string> where) {
    stringstream query;

    query << "DELETE FROM " << table;

    if (!where.empty()) {
        map<string, string>::iterator it;

        query << " WHERE ";

        for (it = where.begin(); it != where.end();) {
            query << it->first << "=";

            if (isNumber(it->second)) {
                query << it->second;
            } else if (it->second.empty()) {
                query << "NULL";
            } else {
                query << "\'" << it->second << "\'";
            }

            if (++it != where.end()) {
                query << " AND ";
            }
        }
    }

    execSql(query.str());
}

vector<string> DbManager::getTables(const vector<string> &ignoreTables) {
    int rc;
    sqlite3_stmt *stmt;
    string query = "SELECT name FROM sqlite_schema WHERE type ='table' AND name NOT LIKE 'sqlite_%'";
    vector<string> tables;

    for (auto &tbl: ignoreTables) {
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
                colText = string(reinterpret_cast<const char *>(sqlite3_column_text(stmt, 0)));
            }

            tables.push_back(colText);

            sqlite3_step(stmt);
        }
    } catch (runtime_error &error) {
        cerr << error.what();
        throw error;
    }

    sqlite3_finalize(stmt);

    return tables;
}

vector<map<string, string>> DbManager::readAllValuesInTable(const string &table) {
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
                    colText = string(
                            reinterpret_cast<const char *>(sqlite3_column_text(stmt, i))
                    );
                }

                m.insert({string(sqlite3_column_name(stmt, i)), colText});
            }

            results.push_back(m);
            sqlite3_step(stmt);
        }
    } catch (runtime_error &error) {
        cerr << "SQLITE READ ERROR | Return Code: " << rc << endl;
        throw error;
    }

    sqlite3_finalize(stmt);

    return results;
}

map<string, vector<map<string, string>>>
DbManager::getAllRowFromAllTables(const vector<string> &ignoreTables) {
    map<string, vector<map<string, string>>> tableData;
    vector<string> tables = getTables(ignoreTables);

    for (const string &tbl: tables) {
        tableData.insert({tbl, readAllValuesInTable(tbl)});
    }

    return tableData;
}

void DbManager::exportData(const string &exportFilePath, const vector<string> &ignoreTables) {
    ofstream outFile;
    map<string, vector<map<string, string>>> data = getAllRowFromAllTables(ignoreTables);
    string outData;

    outFile.exceptions(std::ifstream::failbit | std::ifstream::badbit);

    try {
        outFile.open(exportFilePath, fstream::trunc);
    } catch (system_error &error) {
        const string errMessage = "File failed to open at '" + exportFilePath
                                  + "' with error '" + error.code().message() + "'"
                                  + " error number: " + to_string(errno);

        cerr << errMessage << endl;

        if (outFile.is_open()) {
            outFile.close();
        }

        throw runtime_error(errMessage);
    }

    outFile << "{";

    for (const auto &tbl: data) {
        outData += "\"" + tbl.first + "\":[";


        for (const auto &tblInfo: tbl.second) {
            outData += "{";

            for (const auto &col: tblInfo) {
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

void DbManager::importDataFromFile(const std::string &importFilePath,
                                   const vector<string> &ignoreTables) {
    string inData;
    ifstream fin;

    fin.exceptions(std::ifstream::failbit | std::ifstream::badbit);

    try {
        if (importFilePath.substr(importFilePath.find_last_of('.') + 1) != "json") {
            throw runtime_error("Provided file is not a JSON file");
        }

        fin.open(importFilePath, ios::in);

        inData = string(istreambuf_iterator<char>{fin}, {});

        fin.close();
    } catch (system_error &error) {
        const string errMessage = "File failed to open at '" + importFilePath
                                  + "' with error '" + error.code().message() + "'"
                                  + " error number: " + to_string(errno);

        cerr << errMessage << endl;

        if (fin.is_open()) {
            fin.close();
        }

        throw runtime_error(errMessage);
    } catch (runtime_error &error) {
        cerr << error.what() << ": " << importFilePath << endl;

        throw error;
    }

    importData(inData, ignoreTables);
}

void DbManager::importData(string &inData, const vector<string> &ignoreTables) {
    map<string, vector<map<string, string>>> data;
    vector<string> tables = getTables(ignoreTables);
    stringstream importQuery;
    char *err;

    // Remove unneeded chars
    inData.erase(
            remove_if(inData.begin(), inData.end(),
                      [](unsigned char x) { return std::isspace(x); }),
            inData.end()
    );

    inData.erase(0, 1);
    inData.erase(inData.end() - 1, inData.end());

    inData.erase(
            remove_if(
                    inData.begin(),
                    inData.end(),
                    [](unsigned char x) { return x == '\"'; }
            ),
            inData.end()
    );

    try {
        for (const string &tbl: tables) {
            vector<map<string, string>> table;
            size_t pos;
            string tblStr;
            size_t tblStart = inData.find(tbl + ":[");
            size_t endTblData = inData.find(']', tblStart + string(tbl + ":[").size());

            if (tblStart == string::npos) {
                continue;
            }

            tblStr = inData.substr(tblStart, endTblData - tblStart);

            tblStr.erase(0, (tbl + ":[").size());

            if (tblStr.empty()) {
                data.insert({tbl, {}});
                continue;
            }

            tblStr.erase(0, 1);
            tblStr.erase(tblStr.size() - 1, 1);

            int ind = 0;
            table.resize(count(tblStr.begin(), tblStr.end(), '}') + 1);

            while ((pos = tblStr.find(',')) != string::npos || tblStr.length() > 0) {
                unsigned int end =
                        tblStr.find(',') != string::npos ? tblStr.find(',') : tblStr.length();
                string token = tblStr.substr(0, end);
                bool incrementInd = false;

                if (token.at(0) == '{') token.erase(0, 1);
                if (token.at(token.size() - 1) == '}') {
                    token.erase(token.find('}'), 1);
                    incrementInd = true;
                }

                pair<string, string> col = {
                        token.substr(0, token.find(':')),
                        unescapeSafeChars(
                                token.substr(token.find(':') + 1, token.size() - 1)
                        )
                };

                table.at(ind).insert(col);

                if (incrementInd) ind++;
                if (pos != string::npos) {
                    tblStr.erase(0, pos + 1);
                } else {
                    tblStr = "";
                }
            }

            data.insert({tbl, table});
        }
    } catch (exception &e) {
        cerr << e.what() << endl;

        throw e;
    }

    importQuery << "BEGIN TRANSACTION;";

    for (const string &tbl: tables) {
        importQuery << "DELETE FROM " << tbl << ';';
    }

    map<string, string>::iterator it;

    for (const auto &tbl: data) {
        if (tbl.second.empty()) continue;

        importQuery << "INSERT INTO "
                    << tbl.first
                    << "(";

        for (auto &col: tbl.second.at(0)) {
            importQuery << col.first << ',';
        }

        importQuery.seekp(-1, ios_base::end);
        importQuery << ") VALUES ";

        for (auto &row: tbl.second) {
            importQuery << '(';
            for (auto &col: row) {
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
        const int retVal = sqlite3_exec(
                db, importQuery.str().c_str(), nullptr, nullptr, &err
        );

        if (retVal != SQLITE_OK) {
            throw runtime_error(err);
        }
    } catch (exception &e) {
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

Table *DbManager::execSqlWithReturn(string sql) {
    sqlite3_stmt *stmt = nullptr;
    Table *table;
    map<string, vector<string>> data;

    int rc = sqlite3_prepare_v2(db, sql.c_str(), -1, &stmt, nullptr);

    try {
        if (rc != SQLITE_OK) {
            sqlite3_finalize(stmt);

            throw runtime_error("Error reading from SQLite database.");
        }
    } catch (runtime_error &error) {
        cerr << "SQLITE READ ERROR | Return Code: " << rc << endl;
        throw error;
    }

    table = new Table(stmt);

    return table;
}
