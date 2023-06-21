#ifndef DATA_EXPORTER_H
#define DATA_EXPORTER_H

#include <string>
#include "../DbManager/DbManager.h"

class DataExporter {
private:
    DbManager* manager;
    std::string database_path;

public:
    DataExporter(std::string databasePath);
};

#endif
