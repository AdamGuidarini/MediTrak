//
// Created by adam on 11/20/23.
//

#ifndef MEDICATIONTRACKER_SQLITETABLE_H
#define MEDICATIONTRACKER_SQLITETABLE_H

#include <string>
#include <vector>
#include <map>

using namespace std;

struct SecondaryKey {
    string name;
    string referencedTable;
    string referencedValue;
};

class SqliteTable {
private:
    /**
     * table name
     */
    string name;
    /**
     * Primary key for
     */
    string primaryKey;
    /**
     * Secondary keys in table
     */
    vector<SecondaryKey> secondaryKeys;
    vector<map<string,string>> data;
public:
    SqliteTable(string name, string primary_key, vector<SecondaryKey> secondary_keys, vector<map<string,string>> table_data = {});
    void setData(vector<map<string,string>> table_data);
    vector<map<string,string>> getData();
};


#endif //MEDICATIONTRACKER_SQLITETABLE_H
