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
#include <cstdlib>
#include <future>

using namespace std;

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
public:
    /**
     * Empty default constructor
     */
    DbManager();

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
     * Retrieves DB version number
     * @return Version number stored in DB
     */
    int getVersionNumber();

    /**
     * Executes the provided SQL query
     * @param sql  query to execute
     */
    void execSql(string sql, int (*callback) (void *, int, char**, char **) = nullptr);

    /**
     * Insert a record into the database
     * @param table Table in which to add a row
     * @param values Values to add to new row
     * @return Row id of added row
     */
    long insert(string table, map<string, string> values);

    /**
     * Performs an update query
     * @param table Table in which to update records
     * @param values Values to change where key is the column name and value is the updated value
     * @param where Where clause arguments where the key is the column to check and value is the value to match
     */
    void update(string table,  map<string, string> values, map<string, string> where);

    void deleteRecord(string table, map<string, string> where);

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
};


#endif //MEDICATIONTRACKER_DBMANAGER_H
