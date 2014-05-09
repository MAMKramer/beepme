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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.glanznig.beepme.data.MultiValue;
import com.glanznig.beepme.data.SingleValue;
import com.glanznig.beepme.data.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the table VALUE (values of a input element for a certain moment)
 */
public class ValueTable extends StorageHandler {

    private static final String TAG = "ValueTable";

    private static final String TBL_NAME = "value";
    private static final String TBL_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "value TEXT, " +
                    "input_element_id INTEGER NOT NULL, " +
                    "moment_id INTEGER NOT NULL, " +
                    "FOREIGN KEY(input_element_id) REFERENCES "+ InputElementTable.getTableName() +"(_id), " +
                    "FOREIGN KEY(moment_id) REFERENCES "+ MomentTable.getTableName() +"(_id)" +
                    ")";

    public ValueTable(Context ctx) {
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
     * Populates a value object by reading values from a cursor
     * @param cursor cursor object
     * @return populated value object
     */
    private Value populateObject(Cursor cursor) {
        Value value = null;
        if (cursor.isNull(1)) {
            value = new MultiValue(cursor.getLong(0));
            // todo: multivalues
        }
        else {
            SingleValue singleValue = new SingleValue(cursor.getLong(0));
            singleValue.setValue(cursor.getString(1));
            value = singleValue;
        }
        value.setInputElementUid(cursor.getLong(2));
        value.setMomentUid(cursor.getLong(3));
        value.setInputElementName(cursor.getString(4));

        return value;
    }

    /**
     * Gets an value entry by the associated moment uid and input element uid.
     * @param momentUid uid of the moment
     * @param inputElementUid uid of the input element
     * @return value entry, or null if not found
     */
    public Value getValue(long momentUid, long inputElementUid) {
        SQLiteDatabase db = getDb();
        Value value = null;

        Cursor cursor = db.rawQuery("SELECT v._id, v.value, v.input_element_id, v.moment_id, t.name FROM " +
                        getTableName() + " v, " + InputElementTable.getTableName() + " t " +
                        "WHERE v.moment_id=? AND v.input_element_id=? AND v.input_element_id=t._id",
                new String[] {  Long.valueOf(momentUid).toString(), Long.valueOf(inputElementUid).toString() });

        // todo: multivalues
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            value = populateObject(cursor);
            cursor.close();
        }
        db.close();

        return value;
    }

    /**
     * Gets a list of values (SingleValues or MultiValues) that are associated to a specified moment.
     * @param momentUid uid of the moment
     * @return list of value objects (SingleValue or MultiValue), or empty list if none
     */
    public List<Value> getValues(long momentUid) {
        SQLiteDatabase db = getDb();
        ArrayList<Value> valueList = new ArrayList<Value>();

        Cursor cursor = db.rawQuery("SELECT v._id, v.value, v.input_element_id, v.moment_id, t.name FROM " +
                getTableName() + " v, " + InputElementTable.getTableName() + " t " +
                "WHERE v.moment_id=? AND v.input_element_id=t._id",
                new String[] { Long.valueOf(momentUid).toString() });

        // todo: multivalues
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            do {
                Value value = populateObject(cursor);
                valueList.add(value);
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        return valueList;
    }
}
