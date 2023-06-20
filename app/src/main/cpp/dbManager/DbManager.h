//
// Created by adam on 6/18/23.
//

#ifndef MEDICATIONTRACKER_DBMANAGER_H
#define MEDICATIONTRACKER_DBMANAGER_H

#include <string>
#include <jni.h>
#include <iostream>
#include "../sqlite/sqlite3.h"

using namespace std;

class DbManager {
private:
    jstring database_name;
    sqlite3* db{};
    sqlite3_callback callback;

public:
    DbManager(jstring fileDescriptor);
    void open();
    void close();
    int read();
};


#endif //MEDICATIONTRACKER_DBMANAGER_H
