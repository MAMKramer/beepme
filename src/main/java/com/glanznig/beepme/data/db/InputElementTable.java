/*
This file is part of BeepMe.

BeepMe is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

BeepMe is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with BeepMe. If not, see <http://www.gnu.org/licenses/>.

Copyright 2012-2014 Michael Glanznig
http://beepme.yourexp.at
*/

package com.glanznig.beepme.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Represents the table INPUT_ELEMENT (input element belonging to a input group of a project)
 */
public class InputElementTable extends StorageHandler {

    private static final String TAG = "InputElementTable";

    private static final String TBL_NAME = "input_element";
    private static final String TBL_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "type INTEGER NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "mandatory INTEGER NOT NULL, " +
                    "restrict INTEGER NOT NULL, " +
                    "status INTEGER NOT NULL, " +
                    "title TEXT, " +
                    "help TEXT, " +
                    "options TEXT, " +
                    "vocabulary_id INTEGER, " +
                    "input_group_id INTEGER NOT NULL, " +
                    "FOREIGN KEY(vocabulary_id) REFERENCES "+ VocabularyTable.getTableName() +"(_id), " +
                    "FOREIGN KEY(input_group_id) REFERENCES "+ InputGroupTable.getTableName() +"(_id)" +
                    ")";

    public InputElementTable(Context ctx) {
        super(ctx);
    }

    /**
     * Returns the table name.
     * @return table name
     */
    public static String getTableName() {
        return TBL_NAME;
    }

    /**
     * Creates the table.
     * @param db database object.
     */
    public static void createTable(SQLiteDatabase db) {
        db.execSQL(TBL_CREATE);
    }

    /**
     * Drops the table.
     * @param db database object.
     */
    public static void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TBL_NAME);
    }
}
