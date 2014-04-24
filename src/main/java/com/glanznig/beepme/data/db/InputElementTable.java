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

import com.glanznig.beepme.data.InputElement;
import com.glanznig.beepme.data.Restriction;

import java.util.HashMap;

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
                    "options TEXT, " +
                    "vocabulary_id INTEGER, " +
                    "input_group_id INTEGER NOT NULL, " +
                    "FOREIGN KEY(vocabulary_id) REFERENCES "+ VocabularyTable.getTableName() +"(_id), " +
                    "FOREIGN KEY(input_group_id) REFERENCES "+ InputGroupTable.getTableName() +"(_id)" +
                    ")";

    private static HashMap<InputElement.InputElementType, Integer> typeMap;
    private static HashMap<Integer, InputElement.InputElementType> invTypeMap;
    private static HashMap<String, Integer> restrictionMap;
    private static HashMap<Integer, String> invRestrictionMap;

    static {
        Integer zero = new Integer(0);
        Integer one = new Integer(1);
        Integer two = new Integer(2);
        Integer three = new Integer(3);

        String none = "none";
        String edit = "edit";
        String editDelete = "edit-delete";

        InputElement.InputElementType text = InputElement.InputElementType.TEXT;
        InputElement.InputElementType tags = InputElement.InputElementType.TAGS;
        InputElement.InputElementType photo = InputElement.InputElementType.PHOTO;

        typeMap = new HashMap<InputElement.InputElementType, Integer>();
        invTypeMap = new HashMap<Integer, InputElement.InputElementType>();
        typeMap.put(text, one);
        typeMap.put(tags, two);
        typeMap.put(photo, three);
        invTypeMap.put(one, text);
        invTypeMap.put(two, tags);
        invTypeMap.put(three, photo);

        restrictionMap = new HashMap<String, Integer>();
        invRestrictionMap = new HashMap<Integer, String>();
        restrictionMap.put(none, zero);
        restrictionMap.put(edit, one);
        restrictionMap.put(editDelete, two);
        invRestrictionMap.put(zero, none);
        invRestrictionMap.put(one, edit);
        invRestrictionMap.put(two, editDelete);
    }

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

    /**
     * Truncates (deletes the content of) the table.
     */
    public void truncate() {
        SQLiteDatabase db = getDb();
        dropTable(db);
        createTable(db);
    }

    /**
     * Populates content values for the set variables of the input element.
     * @param element input element
     * @return populated content values
     */
    private ContentValues getContentValues(InputElement element) {
        ContentValues values = new ContentValues();
        if (element.getType() != null) {
            values.put("type", typeMap.get(element.getType()));
        }
        if (element.getName() != null) {
            values.put("name", element.getName());
        }
        if (element.getVocabularyUid() != 0L) {
            values.put("vocabulary_id", element.getVocabularyUid());
        }
        if (element.getInputGroupUid() != 0L) {
            values.put("input_group_id", element.getInputGroupUid());
        }

        if (element.isMandatory()) {
            values.put("mandatory", 1);
        }
        else {
            values.put("mandatory", 0);
        }

        if (element.getRestriction(Restriction.RestrictionType.EDIT) != null &&
                element.getRestriction(Restriction.RestrictionType.DELETE) != null) {
            values.put("restrict", restrictionMap.get("edit-delete"));
        }
        else if (element.getRestriction(Restriction.RestrictionType.EDIT) != null) {
            values.put("restrict", restrictionMap.get("edit"));
        }
        else if (element.getRestriction(Restriction.RestrictionType.DELETE) != null) {
            values.put("restrict", restrictionMap.get("delete"));
        }
        else {
            values.put("restrict", restrictionMap.get("none"));
        }

        values.put("options", element.getOptions());

        return values;
    }

    /**
     * Adds a new input element to the database
     * @param element values to add to the input element table
     * @return new input element object with set values and uid, or null if an error occurred
     */
    public InputElement addInputElement(InputElement element) {
        InputElement newElement = null;

        if (element != null) {
            SQLiteDatabase db = getDb();
            ContentValues values = getContentValues(element);

            Log.i(TAG, "inserted values=" + values);
            long elementId = db.insert(getTableName(), null, values);
            db.close();

            // if no error occurred
            if (elementId != -1) {
                newElement = new InputElement(elementId);
                element.copyTo(newElement);
            }
        }

        return newElement;
    }

    /**
     * Updates a input element in the database
     * @param element values to update for this input element
     * @return true on success or false if an error occurred
     */
    public boolean updateInputElement(InputElement element) {
        int numRows = 0;
        if (element.getUid() != 0L) {
            SQLiteDatabase db = getDb();
            ContentValues values = getContentValues(element);

            Log.i(TAG, "updated values=" + values);
            numRows = db.update(getTableName(), values, "_id=?", new String[] { String.valueOf(element.getUid()) });
            db.close();
        }

        return numRows == 1;
    }
}
