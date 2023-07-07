//
// Created by adam on 6/18/23.
//

#ifndef MEDICATIONTRACKER_DBMANAGER_H
#define MEDICATIONTRACKER_DBMANAGER_H

#include "../sqlite/sqlite3.h"

#include <string>
#include <iostream>
#include <map>
#include <vector>
#include <android/log.h>

using namespace std;

class DbManager {
private:
    /**
     * Path to the database file
     */
    string database_name;
    /**
     * Database reference
     */
    sqlite3* db{};

public:
    /**
     * Constructor for DbManager class
     * @param fileDescriptor Name of file containing database
     */
    DbManager(string fileDescriptor);

    /**
     * Opens database
     */
    void open();

    /**
     * Closes database
     */
    void close();

    /**
     * Gets all tables in database
     * @param tables A pointer to a string to hold table names
     * @return SQLite return code
     */
    int getTables(string* tables);

    /**
     * Retrieves all values from a database table
     * @param results A pointer to the the value of a vector containing all values in "table".
     *      Each element of the vector is a map whose key is the column name
     *      and value is the column value.
     * @param table The table from which to retrieve results
     * @return The return code from SQLite
     */
    int readAllValuesInTable(vector<map<string, string>>* results, const string& table);

    /**
     * Retrieves all data from all tables in the database
     * @param allDatabaseData A map to contain all data in the database.
     *      Each key should be a table name.
     *      Each value should be a pointer to vector a maps whose keys are column names
     *          and values are column values
     * @return The return code from SQLite
     */
    int getAllRowFromAllTables(map<string, vector<map<string, string>>*>* allDatabaseData);
};


#endif //MEDICATIONTRACKER_DBMANAGER_H
