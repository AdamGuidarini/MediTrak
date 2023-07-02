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
    string callbackRes = "";
    string database_name;
    sqlite3* db{};

public:
    DbManager(string fileDescriptor);
    void open();
    void close();
    int readAllValuesInTable(vector<map<string, string>>* results, const string& table);
};


#endif //MEDICATIONTRACKER_DBMANAGER_H
