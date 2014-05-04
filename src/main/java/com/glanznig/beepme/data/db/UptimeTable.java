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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.glanznig.beepme.BeepMeApp;
import com.glanznig.beepme.data.Uptime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UptimeTable extends StorageHandler {
	
	private static final String TAG = "UptimeTable";
	
	private static final String TBL_NAME = "uptime";
	private static final String TBL_CREATE = 
			"CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"start INTEGER NOT NULL UNIQUE, " +
			"end INTEGER UNIQUE, " +
			"project_id INTEGER NOT NULL, " +
			"FOREIGN KEY (project_id) REFERENCES "  + ProjectTable.getTableName() + " (_id)" +
			")";

    public UptimeTable(Context ctx) {
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
     * Populates content values for the set variables of the uptime.
     * @param uptime the uptime object
     * @return populated content values
     */
    private ContentValues getContentValues(Uptime uptime) {
        ContentValues values = new ContentValues();

        if (uptime.getStart() != null) {
            values.put("start", uptime.getStart().getTime());
        }
        if (uptime.getEnd() != null) {
            values.put("end", uptime.getEnd().getTime());
        }
        if (uptime.getProjectUid() != 0L) {
            values.put("project_id", uptime.getProjectUid());
        }

        return values;
    }

    /**
     * Populates a uptime object by reading values from a cursor
     * @param cursor cursor object
     * @return populated uptime object
     */
    private Uptime populateObject(Cursor cursor) {
        Uptime uptime = new Uptime(cursor.getLong(0));
        uptime.setStart(new Date(cursor.getLong(1)));
        if (!cursor.isNull(2)) {
            uptime.setEnd(new Date(cursor.getLong(2)));
        }
        uptime.setProjectUid(cursor.getInt(3));

        return uptime;
    }

    /**
     * Adds a new uptime to the database
     * @param uptime values to add to the uptime table
     * @return new uptime object with set values and uid, or null if an error occurred
     */
    public Uptime addUptime(Uptime uptime) {
        Uptime newUptime = null;

        if (uptime != null) {
            SQLiteDatabase db = getDb();

            ContentValues values = getContentValues(uptime);

            long uptimeId = db.insert(getTableName(), null, values);
            db.close();

            // if no error occurred
            if (uptimeId != -1) {
                newUptime = new Uptime(uptimeId);
                uptime.copyTo(newUptime);
            }
        }

        return newUptime;
    }

    /**
     * Updates a uptime object in the database
     * @param uptime values to update for this uptime object
     * @return true on success or false if an error occurred
     */
    public boolean updateUptime(Uptime uptime) {
        int numRows = 0;
        if (uptime.getUid() != 0L) {
            SQLiteDatabase db = getDb();
            ContentValues values = getContentValues(uptime);

            numRows = db.update(getTableName(), values, "_id=?", new String[] { String.valueOf(uptime.getUid()) });
            db.close();
        }

        return numRows == 1;
    }

    /**
     * Gets an uptime entry by its uid.
     * @return uptime entry, or null if not found
     */
    public Uptime getUptime(long uid) {
        SQLiteDatabase db = getDb();
        Uptime u = null;

        Cursor cursor = db.query(getTableName(), new String[] { "_id", "start", "end", "timerProfileId" },
                "_id=?", new String[] { Long.valueOf(uid).toString() }, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            u = populateObject(cursor);
            cursor.close();
        }
        db.close();

        return u;
    }


    /**
     * Lists all uptime entries in the database ordered by descending start time.
     * @return list of uptime entries, or empty list if none in the database
     */
	public List<Uptime> getUptimes() {
		ArrayList<Uptime> list = new ArrayList<Uptime>();
		SQLiteDatabase db = getDb();
		
		Cursor cursor = db.query(getTableName(), new String[] { "_id", "start", "end", "timerProfileId" },
				null, null, null, null, "start DESC", null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				list.add(populateObject(cursor));
			}
			while (cursor.moveToNext());
			cursor.close();
		}
		db.close();
		
		return list;
	}

    /**
     * Lists all uptime entries of a certain day, ordered by descending start date.
     * @param day calendar object for certain day
     * @return list of uptime entries, or empty list if none for this day
     */
	public List<Uptime> getUptimesOfDay(Calendar day) {
        ArrayList<Uptime> list = new ArrayList<Uptime>();

		// all uptimes that BEGIN or END on that day
		if (day.isSet(Calendar.YEAR) && day.isSet(Calendar.MONTH) && day.isSet(Calendar.DAY_OF_MONTH)) {
			
			// get start and end timestamp of day
			long startOfDay = day.getTimeInMillis();
			day.roll(Calendar.DAY_OF_MONTH, true);
			long endOfDay = day.getTimeInMillis();
			day.roll(Calendar.DAY_OF_MONTH, false);
			
			SQLiteDatabase db = getDb();
			
			// distinct start values
			Cursor cursor = db.query(true, getTableName(), new String[] { "_id", "start", "end", "timerProfileId" },
					"start between ? and ? OR end between ? and ?", new String[] { String.valueOf(startOfDay),
					String.valueOf(endOfDay), String.valueOf(startOfDay),
					String.valueOf(endOfDay) }, "start", null, "start DESC", null);
			
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					list.add(populateObject(cursor));
				}
				while (cursor.moveToNext());
				cursor.close();
			}
			db.close();
		}
		return list;
	}

    /**
     * Gets the most recent uptime entry.
     * @return most recent uptime entry, or null if none
     */
    public Uptime getMostRecentUptime() {
        SQLiteDatabase db = getDb();

        Cursor cursor = db.query(getTableName(), new String[] { "_id", "start", "end", "timerProfileId" },
                null, null, null, null, "start DESC", null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            Uptime u = populateObject(cursor);
            cursor.close();
            db.close();
            return u;
        }
        db.close();

        return null;
    }
}
