#include "DataExporter.h"

#include <utility>

DataExporter::DataExporter(std::string databaseName) {
    database_path = std::move(databaseName);
    manager = new DbManager(database_path);
    medication_table_res = new std::vector<map<string, string>>(0);
}

DataExporter::~DataExporter() {
    delete manager;
    delete medication_table_res;
}

void DataExporter::getDataFromTables() {
    manager->open();

    manager->readAllValuesInTable(medication_table_res, "Medication");

    manager->close();
}
