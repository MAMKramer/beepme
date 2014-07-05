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
import android.util.Log;

import com.glanznig.beepme.data.InputGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the table INPUT_GROUP (grouping of input elements)
 */
public class InputGroupTable extends StorageHandler {

    private static final String TAG = "InputGroupTable";

    private static final String TBL_NAME = "input_group";
    private static final String TBL_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "project_id INTEGER NOT NULL, " +
                    "FOREIGN KEY(project_id) REFERENCES "+ ProjectTable.getTableName() +"(_id)" +
                    ")";

    public InputGroupTable(Context ctx) {
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
     * Populates content values for the set variables of the input group.
     * @param group the input group
     * @return populated content values
     */
    private ContentValues getContentValues(InputGroup group) {
        ContentValues values = new ContentValues();

        if (group.getName() != null) {
            values.put("name", group.getName());
        }
        if (group.getTitle() != null) {
            values.put("title", group.getTitle());
        }
        if (group.getProjectUid() != 0L) {
            values.put("project_id", group.getProjectUid());
        }

        return values;
    }

    /**
     * Populates a input group object by reading values from a cursor
     * @param cursor cursor object
     * @return populated input group object
     */
    private InputGroup populateObject(Cursor cursor) {
        InputGroup inputGroup = new InputGroup(cursor.getLong(0));
        inputGroup.setName(cursor.getString(1));
        inputGroup.setTitle(cursor.getString(2));
        inputGroup.setProjectUid(cursor.getLong(3));

        return inputGroup;
    }

    /**
     * Adds a new input group to the database
     * @param group values to add to the input group table
     * @return new input group object with set values and uid, or null if an error occurred
     */
    public InputGroup addInputGroup(InputGroup group) {
        InputGroup newGroup = null;

        if (group != null) {
            SQLiteDatabase db = getDb();

            ContentValues values = getContentValues(group);

            Log.i(TAG, "inserted values=" + values);
            long groupId = db.insert(getTableName(), null, values);
            closeDb();

            // if no error occurred
            if (groupId != -1) {
                newGroup = new InputGroup(groupId);
                group.copyTo(newGroup);
            }
        }

        return newGroup;
    }

    /**
     * Updates a input group in the database
     * @param group values to update for this input group
     * @return true on success or false if an error occurred
     */
    public boolean updateInputGroup(InputGroup group) {
        int numRows = 0;
        if (group.getUid() != 0L) {
            SQLiteDatabase db = getDb();
            ContentValues values = getContentValues(group);

            numRows = db.update(getTableName(), values, "_id=?", new String[] { String.valueOf(group.getUid()) });
            closeDb();
        }

        return numRows == 1;
    }

    /**
     * Gets a list of all input groups for the specified project.
     * @param projectUid project uid of project where the input groups belong to
     * @return list of input groups, or empty list if none
     */
    public List<InputGroup> getInputGroups(long projectUid) {
        SQLiteDatabase db = getDb();
        ArrayList<InputGroup> inputGroupList = new ArrayList<InputGroup>();

        Cursor cursor = db.query(getTableName(), new String[] { "_id", "name", "title", "project_id" },
                "project_id=?", new String[] { Long.valueOf(projectUid).toString() },
                null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                InputGroup inputGroup = populateObject(cursor);
                inputGroupList.add(inputGroup);
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        closeDb();

        return inputGroupList;
    }
}
