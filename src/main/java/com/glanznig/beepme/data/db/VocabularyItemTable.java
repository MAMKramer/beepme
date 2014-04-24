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
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.glanznig.beepme.data.VocabularyItem;

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

    /**
     * Truncates (deletes the content of) the table.
     */
    public void truncate() {
        SQLiteDatabase db = getDb();
        dropTable(db);
        createTable(db);
    }

    /**
     * Populates content values for the set variables of the vocabulary item.
     * @param vocabularyItem the vocabulary item
     * @return populated content values
     */
    private ContentValues getContentValues(VocabularyItem vocabularyItem) {
        ContentValues values = new ContentValues();

        if (vocabularyItem.getName() != null) {
            values.put("name", vocabularyItem.getName());
        }
        if (vocabularyItem.getLanguage() != null) {
            values.put("lang", vocabularyItem.getLanguage().getLanguage());
        }
        if (vocabularyItem.getValue() != null) {
            values.put("value", vocabularyItem.getValue());
        }
        if (vocabularyItem.isPredefined()) {
            values.put("predefined", 1);
        }
        else {
            values.put("predefined", 0);
        }
        if (vocabularyItem.getVocabularyUid() != 0L) {
            values.put("vocabulary_id", vocabularyItem.getVocabularyUid());
        }
        if (vocabularyItem.getTranslationOfUid() != 0L) {
            values.put("translation_of", vocabularyItem.getTranslationOfUid());
        }

        return values;
    }

    /**
     * Adds a new vocabulary item to the database
     * @param vocabularyItem values to add to the vocabulary item table
     * @return new vocabulary item object with set values and uid, or null if an error occurred
     */
    public VocabularyItem addVocabularyItem(VocabularyItem vocabularyItem) {
        VocabularyItem newVocabularyItem = null;

        if (vocabularyItem != null) {
            SQLiteDatabase db = getDb();

            ContentValues values = getContentValues(vocabularyItem);

            Log.i(TAG, "inserted values=" + values);
            long vocabularyItemId = db.insert(getTableName(), null, values);
            db.close();

            // if no error occurred
            if (vocabularyItemId != -1) {
                newVocabularyItem = new VocabularyItem(vocabularyItemId);
                vocabularyItem.copyTo(newVocabularyItem);
            }
        }

        return newVocabularyItem;
    }

    /**
     * Updates a vocabulary item in the database
     * @param vocabularyItem values to update for this vocabulary item
     * @return true on success or false if an error occurred
     */
    public boolean updateVocabularyItem(VocabularyItem vocabularyItem) {
        int numRows = 0;
        if (vocabularyItem.getUid() != 0L) {
            SQLiteDatabase db = getDb();
            ContentValues values = getContentValues(vocabularyItem);

            numRows = db.update(getTableName(), values, "_id=?", new String[] { String.valueOf(vocabularyItem.getUid()) });
            db.close();
        }

        return numRows == 1;
    }
}