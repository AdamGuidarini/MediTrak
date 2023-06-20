#include "dataExporter.h"

DataExporter::DataExporter(std::string databaseName) {
    database_path = databaseName;
    manager = new DbManager(database_path);
}
