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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.data.VocabularyItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Represents the table VALUE_VOCABULARY_ITEM (vocabulary items as values of input elements)
 */
public class ValueVocabularyItemTable extends StorageHandler {
	
	private static final String TAG = "ValueVocabularyItem";
	
	private static final String TBL_NAME = "value_vocabulary_item";
	private static final String TBL_CREATE =
			"CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
			"value_id INTEGER NOT NULL, " +
			"vocabulary_item_id INTEGER NOT NULL, " +
			"PRIMARY KEY(value_id, vocabulary_item_id) " +
			")";
	
	public ValueVocabularyItemTable(Context ctx) {
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

    /**
     * Adds a new value - vocabulary item connection to the database
     * @param valueUid uid of the value
     * @param vocabularyItemUid uid of the vocabulary item
     */
    public void addValueVocabularyItem(long valueUid, long vocabularyItemUid) {
        if (valueUid != 0L && vocabularyItemUid != 0L) {
            SQLiteDatabase db = getDb();
            ContentValues values = new ContentValues();
            values.put("value_id", valueUid);
            values.put("vocabulary_item_id", vocabularyItemUid);
            db.insert(getTableName(), null, values);
            closeDb();
        }
    }

    /**
     * Removes a value - vocabulary item connection from the database
     * @param valueUid uid of the value
     * @param vocabularyItemUid uid of the vocabulary item
     * @return true, if one item has been deleted, false otherwise
     */
    public boolean deleteValueVocabularyItem(long valueUid, long vocabularyItemUid) {
        if (valueUid != 0L && vocabularyItemUid != 0L) {
            SQLiteDatabase db = getDb();
            ContentValues values = new ContentValues();
            values.put("value_id", valueUid);
            values.put("vocabulary_item_id", vocabularyItemUid);

            int rows = db.delete(TBL_NAME, "value_id=? AND vocabulary_item_id=?", new String[] { String.valueOf(valueUid), String.valueOf(vocabularyItemUid) });
            closeDb();

            return rows == 1;
        }

        return false;
    }

    /**
     * Removes all value - vocabulary item connections of a specific value from the database
     * @param valueUid uid of the value
     * @return true, if one item has been deleted, false otherwise
     */
    public boolean deleteAllOfValue(long valueUid) {
        if (valueUid != 0L) {
            SQLiteDatabase db = getDb();
            ContentValues values = new ContentValues();
            values.put("value_id", valueUid);

            int rows = db.delete(TBL_NAME, "value_id=?", new String[] { String.valueOf(valueUid) });
            closeDb();

            return rows == 1;
        }

        return false;
    }
}
