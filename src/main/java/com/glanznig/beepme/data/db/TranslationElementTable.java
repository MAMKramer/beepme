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

import com.glanznig.beepme.data.TranslationElement;

import java.util.HashMap;

/**
 * Represents the table TRANSLATION_ELEMENT, which is used to translate UI components in
 * different languages and thus enable multi-language projects.
 */
public class TranslationElementTable extends StorageHandler {
    private static final String TAG = "TranslationElementTable";

    private static final String TBL_NAME = "translation_element";
    private static final String TBL_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "lang TEXT NOT NULL, " +
                    "content TEXT NOT NULL, " +
                    "target INTEGER NOT NULL, " +
                    "input_element_id INTEGER NOT NULL, " +
                    "translation_of INTEGER, " +
                    "FOREIGN KEY(input_element_id) REFERENCES "+ InputElementTable.getTableName() +"(_id), " +
                    "FOREIGN KEY(translation_of) REFERENCES "+ TranslationElementTable.getTableName() +"(_id)" +
                    ")";

    private static HashMap<TranslationElement.Target, Integer> targetMap;
    private static HashMap<Integer, TranslationElement.Target> invTargetMap;

    static {
        Integer zero = new Integer(0);
        Integer one = new Integer(1);
        Integer two = new Integer(2);

        TranslationElement.Target content = TranslationElement.Target.CONTENT;
        TranslationElement.Target title = TranslationElement.Target.TITLE;
        TranslationElement.Target help = TranslationElement.Target.HELP;

        targetMap = new HashMap<TranslationElement.Target, Integer>();
        invTargetMap = new HashMap<Integer, TranslationElement.Target>();
        targetMap.put(content, zero);
        targetMap.put(title, one);
        targetMap.put(help, two);
        invTargetMap.put(zero, content);
        invTargetMap.put(one, title);
        invTargetMap.put(two, help);
    }

    public TranslationElementTable(Context ctx) {
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
     * Populates content values for the set variables of the translation element.
     * @param element the translation element
     * @return populated content values
     */
    private ContentValues getContentValues(TranslationElement element) {
        ContentValues values = new ContentValues();

        if (element.getLang() != null) {
            values.put("lang", element.getLang().getLanguage());
        }
        if (element.getContent() != null) {
            values.put("content", element.getContent());
        }
        if (element.getTarget() != null) {
            values.put("target", targetMap.get(element.getTarget()));
        }
        if (element.getInputElementUid() != 0L) {
            values.put("input_element_id", element.getInputElementUid());
        }
        if (element.getTranslationOfUid() != 0L) {
            values.put("translation_of", element.getTranslationOfUid());
        }

        return values;
    }

    /**
     * Adds a new translation element to the database
     * @param element values to add to the translation element table
     * @return new translation element object with set values and uid, or null if an error occurred
     */
    public TranslationElement addTranslationElement(TranslationElement element) {
        TranslationElement newElement = null;

        if (element != null) {
            SQLiteDatabase db = getDb();

            ContentValues values = getContentValues(element);

            Log.i(TAG, "inserted values=" + values);
            long elementId = db.insert(getTableName(), null, values);
            closeDb();

            // if no error occurred
            if (elementId != -1) {
                newElement = new TranslationElement(elementId);
                element.copyTo(newElement);
            }
        }

        return newElement;
    }

    /**
     * Updates a translation element in the database
     * @param element values to update for this translation element
     * @return true on success or false if an error occurred
     */
    public boolean updateTranslationElement(TranslationElement element) {
        int numRows = 0;
        if (element.getUid() != 0L) {
            SQLiteDatabase db = getDb();
            ContentValues values = getContentValues(element);

            numRows = db.update(getTableName(), values, "_id=?", new String[] { String.valueOf(element.getUid()) });
            closeDb();
        }

        return numRows == 1;
    }
}
