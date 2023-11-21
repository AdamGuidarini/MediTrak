//
// Created by adam on 6/18/23.
//

#ifndef MEDICATIONTRACKER_DBMANAGER_H
#define MEDICATIONTRACKER_DBMANAGER_H

#include "sqlite3.h"

#include <string>
#include <sstream>
#include <iostream>
#include <fstream>
#include <map>
#include <vector>

using namespace std;

struct ForeignKey {
    string name;
    string referencedTable;
    string referencedColumn;
};

struct SQLiteTable {
    string name;
    string primaryKey;
    vector<ForeignKey> foreignKeys;
    vector<map<string,string>> data;
    int start;
    int maxRecords;
};

class DbManager {
private:

    /**
     * Path to the database file.
     */
    string database_name;

    /**
     * Database reference.
     */
    sqlite3* db{};

    /**
     * Data stored in database
     */
    vector<SQLiteTable>* data;

    /**
     * Searches for characters that can cause issues with parsing when importing and replace them with HTML safe values
     * @param str String to search
     * @return str with unsafe values replaces with HTML safe chars
     */
    string escapeUnsafeChars(string str);

    /**
     * Replaces safe HTML chars with their regular equivalents
     * @param str String to check for HTML escape characters
     * @return str with any HTML escape characters replaced with regular characters
     */
    string unescapeSafeChars(string str);

    /**
     * Replaces all occurrences of from with to in str
     * @param str string that will have characters replaces
     * @param from substring of str to replace with to
     * @param to substring that will replace from
     */
    void replaceAll(string& str, const string& from, const string& to);

    /**
     * Checks if a string is or is not a number
     * @param str string to check if it is a number
     * @return True if a number, else false
     */
    bool isNumber(string str);

    /**
     * Collects and stores data in
     */
    vector<SQLiteTable>* collectData(string& tblName, int offset = 0, int limit = -1);
public:
    /**
     * Constructor for DbManager class, automatically opens database.
     * @param databasePath Name of file containing database.
     * @param enableForeignKeys Whether or not foreign keys should be enabled.
     */
    DbManager(string databasePath, bool enableForeignKeys);

    /**
     * Class destructor, will also automatically close database.
     */
    ~DbManager();

    /**
     * Opens database.
     */
    void openDb();

    /**
     * Closes database.
     */
    void closeDb();

    /**
     * Gets all tables in database.
     * @param Optional ignoreTables Array of tables to exclude.
     * @return A pointer to a string to hold table names.
     */
    vector<string> getTables(const vector<string>& ignoreTables = {});

    /**
     * Retrieves all values from a database table.
     * @param table The table from which to retrieve results.
     * @return A vector containing all values in "table".
     *      Each element of the vector is a map whose key is the column name
     *      and value is the column value.
     */
    vector<map<string, string>> readAllValuesInTable(const string& table);

    /**
     * Retrieves all data from all tables in the database.
     * @param Optional ignoreTables Array of tables to exclude.
     * @return A map to contain all data in the database.
     *      Each key should be a table name.
     *      Each value should be a pointer to vector a maps whose keys are column names
     *      and values are column values.
     */
    map<string, vector<map<string, string>>> getAllRowFromAllTables(const vector<string>& ignoreTables = {});

    /**
     * Exports all data stored in DB to provided location as a JSON file.
     * @param exportFilePath Location for file storing database backup with its name ex. /src/myDir/data.json.
     * @param Optional ignoreTables Array of tables to exclude.
     */
    void exportData(const string& exportFilePath, const vector<string>& ignoreTables = {});

    /**
     * Imports data from JSON file and writes it to database,
     *  throws an error if attempting to write to a table or
     *  column that does not exist in the provided database/table.
     * @param importFilePath Path to JSON file storing data to import.
     */
    void importData(const string& importFilePath, const vector<string>& ignoreTables = {});

    vector<SQLiteTable>* getData();
};


#endif //MEDICATIONTRACKER_DBMANAGER_H
