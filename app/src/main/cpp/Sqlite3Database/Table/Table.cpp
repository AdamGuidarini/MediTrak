//
// Created by adam on 3/7/24.
//

#include "Table.h"

Table::Table(map<string, vector<string>> data) {
    table = std::move(data);
    currentRow = 0;
    rowCount = table.begin()->second.size();
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
