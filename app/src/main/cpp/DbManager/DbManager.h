//
// Created by adam on 6/18/23.
//

#ifndef MEDICATIONTRACKER_DBMANAGER_H
#define MEDICATIONTRACKER_DBMANAGER_H

#include <string>
#include <iostream>
#include <map>
#include <vector>
#include <android/log.h>
#include "../sqlite/sqlite3.h"

using namespace std;

class DbManager {
private:
    string callbackRes = "";
    string database_name;
    sqlite3* db;
    vector<map<string, string>> medication_table_res;

public:
    DbManager(string fileDescriptor);
    void open();
    void close();
    int read();
};


#endif //MEDICATIONTRACKER_DBMANAGER_H
