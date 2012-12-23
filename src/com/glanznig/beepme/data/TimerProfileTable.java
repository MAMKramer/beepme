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

Copyright since 2012 Michael Glanznig
http://beepme.glanznig.com
*/

package com.glanznig.beepme.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TimerProfileTable extends StorageHandler {
	
private static final String TAG = "TimerProfileTable";
	
	private static final String TBL_NAME = "timer_profile";
	private static final String TBL_CREATE = 
			"CREATE TABLE IF NOT EXISTS " + TBL_NAME + " (" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"name TEXT NOT NULL UNIQUE, " +
			"minUptimeDuration INTEGER NOT NULL, " +
			"avgBeepInterval INTEGER NOT NULL, " +
			"maxBeepInterval INTEGER NOT NULL, " +
			"minBeepInterval INTEGER NOT NULL, " +
			"uptimeCountMoveToAverage INTEGER NOT NULL, " +
			"numCancelledBeepsMoveToAverage INTEGER NOT NULL" +
			")";

	public TimerProfileTable(Context ctx) {
		super(ctx);
	}
	
	public static String getTableName() {
		return TBL_NAME;
	}
	
	public static void createTable(SQLiteDatabase db) {
		db.execSQL(TBL_CREATE);
		
		insertData(db);
	}
	
	public static void dropTable(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TBL_NAME);
	}
	
	public static void truncateTable(SQLiteDatabase db) {
		dropTable(db);
		createTable(db);
	}
	
	public static void insertData(SQLiteDatabase db) {
		ContentValues values = null;
		
		values = new ContentValues();
		values.put("_id", 1);
		values.put("name", "General");
		values.put("minUptimeDuration", 60); //1 min
		values.put("avgBeepInterval", 1800); //30 min
		values.put("maxBeepInterval", 3600); //60 min
		values.put("minBeepInterval", 600); //10 min
		values.put("uptimeCountMoveToAverage", 3);
		values.put("numCancelledBeepsMoveToAverage", 2);
		db.insert(TBL_NAME, null, values);
		
		values = new ContentValues();
		values.put("_id", 2);
		values.put("name", "HCI");
		values.put("minUptimeDuration", 60); //1 min
		values.put("avgBeepInterval", 300); //5 min
		values.put("maxBeepInterval", 600); //10 min
		values.put("minBeepInterval", 120); //2 min
		values.put("uptimeCountMoveToAverage", 3);
		values.put("numCancelledBeepsMoveToAverage", 2);
		db.insert(TBL_NAME, null, values);
	}
	
	public TimerProfile getTimerProfile(long id) {
		SQLiteDatabase db = getDb();
		TimerProfile tp = null;
		
		Cursor cursor = db.query(TBL_NAME, new String[] {"_id", "name", "minUptimeDuration", "avgBeepInterval",
				"maxBeepInterval", "minBeepInterval", "uptimeCountMoveToAverage", "numCancelledBeepsMoveToAverage"},
				"_id=?", new String[] { String.valueOf(id) }, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			tp = new TimerProfile(cursor.getLong(0));
			tp.setName(cursor.getString(1));
			tp.setMinUptimeDuration(cursor.getInt(2));
			tp.setAvgBeepInterval(cursor.getInt(3));
			tp.setMaxBeepInterval(cursor.getInt(4));
			tp.setMinBeepInterval(cursor.getInt(5));
			tp.setUptimeCountMoveToAverage(cursor.getInt(6));
			tp.setNumCancelledBeepsMoveToAverage(cursor.getInt(7));
		}
		cursor.close();
		db.close();
		
		return tp;
	}
	
	public List<TimerProfile> getTimerProfiles() {
		SQLiteDatabase db = getDb();
		List<TimerProfile> profileList = new ArrayList<TimerProfile>();
		
		Cursor cursor = db.query(TBL_NAME, new String[] {"_id", "name", "minUptimeDuration", "avgBeepInterval",
				"maxBeepInterval", "minBeepInterval", "uptimeCountMoveToAverage", "numCancelledBeepsMoveToAverage"},
				null, null, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			
			do {
				TimerProfile tp = new TimerProfile(cursor.getLong(0));
				tp.setName(cursor.getString(1));
				tp.setMinUptimeDuration(cursor.getInt(2));
				tp.setAvgBeepInterval(cursor.getInt(3));
				tp.setMaxBeepInterval(cursor.getInt(4));
				tp.setMinBeepInterval(cursor.getInt(5));
				tp.setUptimeCountMoveToAverage(cursor.getInt(6));
				tp.setNumCancelledBeepsMoveToAverage(cursor.getInt(7));
				
				profileList.add(tp);
			}
			while(cursor.moveToNext());
		}
		cursor.close();
		db.close();
		
		return profileList;
	}

}
