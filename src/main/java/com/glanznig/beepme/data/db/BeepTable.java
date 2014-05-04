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

import com.glanznig.beepme.data.Beep;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * Represents the table BEEP (internal data and statistics about beeps and their status).
 */
public class BeepTable extends StorageHandler {

    private static final String TAG = "BeepTable";

    private static final String TBL_NAME = "beep";
    private static final String TBL_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "timestamp INTEGER NOT NULL, " +
                    "created INTEGER NOT NULL, " +
                    "received INTEGER, " +
                    "updated INTEGER, " +
                    "status INTEGER NOT NULL, " +
                    "uptime_id INTEGER NOT NULL, " +
                    "FOREIGN KEY(uptime_id) REFERENCES "+ UptimeTable.getTableName() +"(_id)" +
                    ")";

    private static HashMap<Beep.BeepStatus, Integer> statusMap;
    private static HashMap<Integer, Beep.BeepStatus> invStatusMap;

    static {
        Integer one = new Integer(1);
        Integer two = new Integer(2);
        Integer three = new Integer(3);
        Integer four = new Integer(4);

        Beep.BeepStatus active = Beep.BeepStatus.ACTIVE;
        Beep.BeepStatus received = Beep.BeepStatus.RECEIVED;
        Beep.BeepStatus cancelled = Beep.BeepStatus.CANCELLED;
        Beep.BeepStatus expired = Beep.BeepStatus.EXPIRED;

        statusMap = new HashMap<Beep.BeepStatus, Integer>();
        invStatusMap = new HashMap<Integer, Beep.BeepStatus>();
        statusMap.put(cancelled, one);
        statusMap.put(expired, two);
        statusMap.put(active, three);
        statusMap.put(received, four);
        invStatusMap.put(one, cancelled);
        invStatusMap.put(two, expired);
        invStatusMap.put(three, active);
        invStatusMap.put(four, received);
    }

    public BeepTable(Context ctx) {
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
     * Populates content values for the set variables of the beep.
     * @param beep the beep object
     * @return populated content values
     */
    private ContentValues getContentValues(Beep beep) {
        ContentValues values = new ContentValues();

        if (beep.getTimestamp() != null) {
            values.put("timestamp", beep.getTimestamp().getTime());
        }
        if (beep.getCreated() != null) {
            values.put("created", beep.getCreated().getTime());
        }
        if (beep.getReceived() != null) {
            values.put("received", beep.getReceived().getTime());
        }
        if (beep.getUpdated() != null) {
            values.put("updated", beep.getUpdated().getTime());
        }
        if (beep.getStatus() != null) {
            values.put("status", statusMap.get(beep.getStatus()));
        }
        if (beep.getUptimeUid() != 0L) {
            values.put("uptime_id", beep.getUptimeUid());
        }

        return values;
    }

    /**
     * Populates a beep object by reading values from a cursor
     * @param cursor cursor object
     * @return populated beep object
     */
    private Beep populateObject(Cursor cursor) {
        Beep beep = new Beep(cursor.getLong(0));
        beep.setTimestamp(new Date(cursor.getLong(1)));
        beep.setCreated(new Date(cursor.getLong(2)));
        if (!cursor.isNull(3)) {
            beep.setReceived(new Date(cursor.getLong(3)));
        }
        if (!cursor.isNull(4)) {
            beep.setUpdated(new Date(cursor.getLong(4)));
        }
        beep.setStatus(invStatusMap.get(cursor.getInt(5)));
        beep.setUptimeUid(cursor.getInt(6));

        return beep;
    }

    /**
     * Adds a new beep to the database
     * @param beep values to add to the beep table
     * @return new beep object with set values and uid, or null if an error occurred
     */
    public Beep addBeep(Beep beep) {
        Beep newBeep = null;

        if (beep != null) {
            SQLiteDatabase db = getDb();

            ContentValues values = getContentValues(beep);

            long beepId = db.insert(getTableName(), null, values);
            db.close();

            // if no error occurred
            if (beepId != -1) {
                newBeep = new Beep(beepId);
                beep.copyTo(newBeep);
            }
        }

        return newBeep;
    }

    /**
     * Updates a beep object in the database
     * @param beep values to update for this beep object
     * @return true on success or false if an error occurred
     */
    public boolean updateBeep(Beep beep) {
        int numRows = 0;
        if (beep.getUid() != 0L) {
            SQLiteDatabase db = getDb();
            ContentValues values = getContentValues(beep);

            numRows = db.update(getTableName(), values, "_id=?", new String[] { String.valueOf(beep.getUid()) });
            db.close();
        }

        return numRows == 1;
    }

    /**
     * Gets an beep entry by its uid.
     * @return beep entry, or null if not found
     */
    public Beep getBeep(long uid) {
        SQLiteDatabase db = getDb();
        Beep beep = null;

        Cursor cursor = db.query(getTableName(), new String[] { "_id", "timestamp", "created",
                "received", "updated", "status", "uptime_id" },
                "_id=?", new String[] { Long.valueOf(uid).toString() }, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            beep = populateObject(cursor);
            cursor.close();
        }
        db.close();

        return beep;
    }
}
