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
 * Represents the table VOCABULARY_ITEM (content of vocabularies)
 */
public class VocabularyItemTable extends StorageHandler {
	
	private static final String TAG = "VocabularyItemTable";
	
	private static final String TBL_NAME = "vocabulary_item";
	private static final String TBL_CREATE =
			"CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"name TEXT NOT NULL, " +
            "lang TEXT NOT NULL, " +
            "value TEXT NOT NULL, " +
            "predefined INTEGER NOT NULL, " +
            "vocabulary_id INTEGER NOT NULL, " +
            "translation_of INTEGER, " +
			"FOREIGN KEY(vocabulary_id) REFERENCES " + VocabularyTable.getTableName() + "(_id), " +
			"FOREIGN KEY(translation_of) REFERENCES " + VocabularyItemTable.getTableName() + "(_id)" +
			")";
	
	public VocabularyItemTable(Context ctx) {
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
