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

    medication_table_res = new std::vector<map<string, string>>(manager->readAllValuesInTable("Medication"));

    manager->close();
}
