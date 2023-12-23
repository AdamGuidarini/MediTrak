//
// Created by adam on 12/23/23.
//

#include "MediTrakDbHelper.h"

MediTrakDbHelper::MediTrakDbHelper() {
    manager = DbManager(DATABASE_NAME, true);

    currentVersion = manager.getVersionNumber();
}

MediTrakDbHelper::~MediTrakDbHelper() {}

void MediTrakDbHelper::addIgnoredTables(vector<std::string> tables) {
    tablesToIgnore = tables;
}
