//
// Created by adam on 3/7/24.
//

#ifndef MEDICATIONTRACKER_TABLE_H
#define MEDICATIONTRACKER_TABLE_H

#include <map>
#include <vector>
#include <string>

using namespace std;

/**
 * A class representing a SQL table
 */
class Table {
private:
    map<string, vector<string>> table;
    int currentRow;
    int rowCount;
public:
    /**
     * Class constructor - Initializes table
     */
    Table(map<string, vector<string>> data);

    /**
     * Set current row to first
     */
    void moveToFirst();

    /**
     * Increment currentRow
     */
    void moveToNext();

    /**
     * Sets current row to last row
     */
    void moveToLast();

    /**
     * Moved to provided index - if row does not exist, index will move to after last
     * @param row Row to move to
     */
    void moveToRow(int row);

    /**
     * Get number of rows
     * @return
     */
    int getCount();

    /**
     * Gets item in current row in columnName - returns nullptr if it does not exist
     * @param columnName
     * @return
     */
    string getItem(string columnName);

    /**
     * Is first selected
     * @return true if on first
     */
    bool isFirst();

    /**
     * if currentRow is beyond final retrieved row
     * @return
     */
    bool isAfterLast();
};

#endif //MEDICATIONTRACKER_TABLE_H
