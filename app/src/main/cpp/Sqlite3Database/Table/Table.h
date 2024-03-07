//
// Created by adam on 3/7/24.
//

#ifndef MEDICATIONTRACKER_TABLE_H
#define MEDICATIONTRACKER_TABLE_H

#include <map>
#include <vector>
#include <string>
#include "DbManager.h"

using namespace std;

class Table {
private:
    map<string, vector<string>> table;
public:
    Table();
    ~Table();
};


#endif //MEDICATIONTRACKER_TABLE_H
