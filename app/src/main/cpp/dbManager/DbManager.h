//
// Created by adam on 6/18/23.
//

#ifndef MEDICATIONTRACKER_DBMANAGER_H
#define MEDICATIONTRACKER_DBMANAGER_H

#include <string>

using namespace std;

class DbManager {
private:
    string fd;

public:
    DbManager(string fileDescriptor);

};


#endif //MEDICATIONTRACKER_DBMANAGER_H
