//
// Created by adam on 11/20/23.
//

#include "SqliteTable.h"

SqliteTable::SqliteTable(
        string name,
        string primary_key,
        vector<SecondaryKey> secondary_keys,
        vector<map<string, string>> table_data
) {
    this->name = name;
    primaryKey = primary_key;
    secondaryKeys = secondary_keys;
    data = table_data;
}

void SqliteTable::setData(vector<map<string, string>> table_data) { data = table_data; }
vector<map<string, string>> SqliteTable::getData() { return data; }
