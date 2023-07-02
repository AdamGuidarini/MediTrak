#ifndef DATA_EXPORTER_H
#define DATA_EXPORTER_H

#include "../DbManager/DbManager.h"

#include <string>
#include <vector>
#include <map>

class DataExporter {
private:
    DbManager* manager;
    std::string database_path;
    vector<map<string, string>>* medication_table_res;

public:
    DataExporter(std::string databasePath);
    ~DataExporter();
    void getDataFromTables();
};

#endif
