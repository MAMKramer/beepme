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

import com.glanznig.beepme.data.MultiValue;
import com.glanznig.beepme.data.SingleValue;
import com.glanznig.beepme.data.Value;
import com.glanznig.beepme.data.VocabularyItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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

    private Context ctx;

    public ValueTable(Context ctx) {
        super(ctx);
        this.ctx = ctx.getApplicationContext();
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
     * Populates content values for the set variables of the value.
     * @param value the value
     * @return populated content values
     */
    private ContentValues getContentValues(Value value) {
        ContentValues values = new ContentValues();

        if (value.getMomentUid() != 0L) {
            values.put("moment_id", value.getMomentUid());
        }
        if (value.getInputElementUid() != 0L) {
            values.put("input_element_id", value.getInputElementUid());
        }

        if (value instanceof SingleValue) {
            if (((SingleValue)value).getValue() != null) {
                values.put("value", ((SingleValue)value).getValue());
            }
        }

        return values;
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

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            value = populateObject(cursor);
            cursor.close();

            if (value instanceof MultiValue) {
                MultiValue multiValue = (MultiValue)value;
                VocabularyItemTable vocabularyItemTable = new VocabularyItemTable(ctx);
                List<VocabularyItem> valueItems = vocabularyItemTable.getVocabularyItemsOfValue(multiValue.getUid(), Locale.getDefault());
                Iterator<VocabularyItem> valueItemsIterator = valueItems.iterator();
                while (valueItemsIterator.hasNext()) {
                    multiValue.setValue(valueItemsIterator.next());
                }
            }
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

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            do {
                Value value = populateObject(cursor);

                if (value instanceof MultiValue) {
                    MultiValue multiValue = (MultiValue)value;
                    VocabularyItemTable vocabularyItemTable = new VocabularyItemTable(ctx);
                    List<VocabularyItem> valueItems = vocabularyItemTable.getVocabularyItemsOfValue(multiValue.getUid(), Locale.getDefault());
                    Iterator<VocabularyItem> valueItemsIterator = valueItems.iterator();
                    while (valueItemsIterator.hasNext()) {
                        multiValue.setValue(valueItemsIterator.next());
                    }
                }

                valueList.add(value);
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        return valueList;
    }

    /**
     * Adds a new value (SingleValue or MultiValue) to the database
     * @param value value to add
     * @return new value object with set variables and uid, or null if an error occurred
     */
    public Value addValue(Value value) {
        Value newValue = null;

        if (value != null) {
            SQLiteDatabase db = getDb();
            ContentValues values = getContentValues(value);

            long valueUid = db.insert(getTableName(), null, values);

            if (value instanceof MultiValue) {
                VocabularyItemTable vocabularyItemTable = new VocabularyItemTable(ctx);
                ValueVocabularyItemTable valueVocabularyItemTable = new ValueVocabularyItemTable(ctx);
                Iterator<VocabularyItem> valueIterator = ((MultiValue)value).getValues().iterator();

                while (valueIterator.hasNext()) {
                    VocabularyItem vocabularyItem = valueIterator.next();
                    if (vocabularyItem.getUid() == 0L) {
                        vocabularyItem = vocabularyItemTable.addVocabularyItem(vocabularyItem);
                    }
                    valueVocabularyItemTable.addValueVocabularyItem(valueUid, vocabularyItem.getUid());
                }
            }

            db.close();
            // only if no error occurred
            if (valueUid != -1) {
                if (value instanceof SingleValue) {
                    newValue = new SingleValue(valueUid);
                    ((SingleValue)value).copyTo((SingleValue)newValue);
                }
                else if (value instanceof MultiValue) {
                    newValue = new MultiValue(valueUid);
                    ((MultiValue)value).copyTo((MultiValue)newValue);
                }
            }
        }

        return newValue;
    }

    /**
     * Updates a value object in the database
     * @param value values to update for this value object
     * @return true on success or false if an error occurred
     */
    public boolean updateValue(Value value) {
        int numRows = 0;
        if (value.getUid() != 0L) {
            SQLiteDatabase db = getDb();
            ContentValues values = getContentValues(value);

            numRows = db.update(getTableName(), values, "_id=?", new String[] { String.valueOf(value.getUid()) });

            if (value instanceof MultiValue) {
                ValueVocabularyItemTable valueVocabularyItemTable = new ValueVocabularyItemTable(ctx);
                // delete all connections first and then re-add new/changed ones
                valueVocabularyItemTable.deleteAllOfValue(value.getUid());

                Iterator<VocabularyItem> valueIterator = ((MultiValue)value).getValues().iterator();
                while (valueIterator.hasNext()) {
                    VocabularyItem vocabularyItem = valueIterator.next();
                    valueVocabularyItemTable.addValueVocabularyItem(value.getUid(), vocabularyItem.getUid());
                }
            }
            db.close();
        }

        return numRows == 1;
    }

    /**
     * Deletes all values of the given moment from the database.
     * @param momentUid uid of the moment
     */
    public void deleteValues(long momentUid) {
        SQLiteDatabase db = getDb();

        int rows = db.delete(getTableName(), "moment_id=?", new String[] { Long.valueOf(momentUid).toString() });
        // todo: multivalues (also delete orphaned user-added vocabulary items?)
        db.close();
    }
}
