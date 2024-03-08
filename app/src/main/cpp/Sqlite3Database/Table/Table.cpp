//
// Created by adam on 3/7/24.
//

#include "Table.h"

Table::Table(sqlite3_stmt* stmt) {
    currentRow = 0;
    rowCount = table.begin()->second.size();
    table = map<string, vector<string>>();

    if (stmt == nullptr) {
        throw runtime_error("Table received nullptr statement");
    }

    // GetRows
    while (sqlite3_column_text(stmt, 0)) {
        for (int i = 0; i < sqlite3_column_count(stmt); i++) {
            string colText = "";
            string colName = string(sqlite3_column_name(stmt, i));

            if (sqlite3_column_text(stmt, i) != nullptr) {
                colText = string(reinterpret_cast<const char*>(sqlite3_column_text(stmt, i)));
            }

            if (table.count(colName) != 0) {
                table.at(colName).push_back(colText);
            } else {
                table.insert({ colName, { colText } });
            }
        }

        sqlite3_step(stmt);
    }

    sqlite3_finalize(stmt);
}

void Table::moveToFirst() {
    currentRow = 0;
}

void Table::moveToNext() {
    currentRow++;
}

void Table::moveToLast() {
    currentRow = -1;
}

void Table::moveToRow(int row) {
    currentRow = row > rowCount - 1 ? -1 : row;
}

int Table::getCount() {
    return rowCount;
}

string Table::getItem(string columnName) {
    return table.at(columnName).at(currentRow);
}

bool Table::isFirst() {
    return rowCount == 0;
}

bool Table::isAfterLast() {
    return rowCount == -1;
}
