//
// Created by adam on 12/23/23.
//

#ifndef MEDICATIONTRACKER_MEDITRAKDBHELPER_H
#define MEDICATIONTRACKER_MEDITRAKDBHELPER_H

#include <string>
#include <vector>
#include "../DbManager/DbManager.h"

using namespace std;

class MediTrakDbHelper {
private:
    const int DB_VERSION = 8;
    const string DATABASE_NAME = "Medications.db";
    int currentVersion;
    vector<string> tablesToIgnore;
    DbManager manager;

    void create();
    void upgrade(int newVersion);
public:
    /**
     * Class constructor
     * @param dbPath
     */
    MediTrakDbHelper();

    /**
     * Class destructor
     */
    ~MediTrakDbHelper();
    void addIgnoredTables(vector<string> tables);
};


#endif //MEDICATIONTRACKER_MEDITRAKDBHELPER_H
