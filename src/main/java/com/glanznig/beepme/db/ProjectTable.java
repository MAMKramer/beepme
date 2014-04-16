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

package com.glanznig.beepme.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Represents the table PROJECT (logical units related to research projects)
 */
public class ProjectTable extends StorageHandler {
	
private static final String TAG = "ProjectTable";
	
	private static final String TBL_NAME = "project";
	private static final String TBL_CREATE = 
			"CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"name TEXT NOT NULL UNIQUE, " +
			"type INTEGER NOT NULL, " +
			"status INTEGER NOT NULL, " +
			"start INTEGER, " +
			"expire INTEGER, " +
			"lang TEXT NOT NULL, " +
			"restrictions TEXT, " +
			"timer TEXT NOT NULL, " +
            "options TEXT NOT NULL" +
			")";

	public ProjectTable(Context ctx) {
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
	
	public Timer getTimerProfile(long id) {
		SQLiteDatabase db = getDb();
		Timer tp = null;
		
		Cursor cursor = db.query(TBL_NAME, new String[] {"_id", "name", "minUptimeDuration", "avgBeepInterval",
				"maxBeepInterval", "minBeepInterval", "uptimeCountMoveToAverage",
				"numCancelledBeepsMoveToAverage", "minSizeBeepInterval"},
				"_id=?", new String[] { String.valueOf(id) }, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			tp = new Timer(cursor.getLong(0));
			tp.setName(cursor.getString(1));
			tp.setMinUptimeDuration(cursor.getInt(2));
			tp.setAvgBeepInterval(cursor.getInt(3));
			tp.setMaxBeepInterval(cursor.getInt(4));
			tp.setMinBeepInterval(cursor.getInt(5));
			tp.setUptimeCountMoveToAverage(cursor.getInt(6));
			tp.setNumCancelledBeepsMoveToAverage(cursor.getInt(7));
			tp.setMinSizeBeepInterval(cursor.getInt(8));
		}
		cursor.close();
		db.close();
		
		return tp;
	}
	
	public List<Timer> getTimerProfiles() {
		SQLiteDatabase db = getDb();
		List<Timer> profileList = new ArrayList<Timer>();
		
		Cursor cursor = db.query(TBL_NAME, new String[] {"_id", "name", "minUptimeDuration", "avgBeepInterval",
				"maxBeepInterval", "minBeepInterval", "uptimeCountMoveToAverage",
				"numCancelledBeepsMoveToAverage", "minSizeBeepInterval"},
				null, null, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			do {
				Timer tp = new Timer(cursor.getLong(0));
				tp.setName(cursor.getString(1));
				tp.setMinUptimeDuration(cursor.getInt(2));
				tp.setAvgBeepInterval(cursor.getInt(3));
				tp.setMaxBeepInterval(cursor.getInt(4));
				tp.setMinBeepInterval(cursor.getInt(5));
				tp.setUptimeCountMoveToAverage(cursor.getInt(6));
				tp.setNumCancelledBeepsMoveToAverage(cursor.getInt(7));
				tp.setMinSizeBeepInterval(cursor.getInt(8));
				
				profileList.add(tp);
			}
			while(cursor.moveToNext());
		}
		cursor.close();
		db.close();
		
		return profileList;
	}

}
