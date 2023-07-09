#include "DataExporter.h"

#include <utility>

DataExporter::DataExporter(std::string databaseName) {
    database_path = std::move(databaseName);
    manager = new DbManager(database_path);
}

DataExporter::~DataExporter() {
    delete manager;
    delete medication_table_res;
}

void DataExporter::getDataFromTables() {
    manager->open();

    manager->getAllRowFromAllTables();

    manager->close();
}
