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

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ScheduledBeepTable extends StorageHandler {
	
	private static final String TAG = "ScheduledBeepTable";
	
	private static final String TBL_NAME = "scheduled_beep";
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
	
	public ScheduledBeepTable(Context ctx) {
		super(ctx);
	}
	
	public static String getTableName() {
		return TBL_NAME;
	}
	
	public static void createTable(SQLiteDatabase db) {
		db.execSQL(TBL_CREATE);
	}
	
	public static void dropTable(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TBL_NAME);
	}
	
	public static void truncateTable(SQLiteDatabase db) {
		dropTable(db);
		createTable(db);
	}
	
	public long addScheduledBeep(long time, long uptimeId) {
		long beepId = 0L;
		
		if (time != 0L && uptimeId != 0L) {
			SQLiteDatabase db = getDb();
			 
		    ContentValues values = new ContentValues();
		    values.put("timestamp", time);
		    values.put("created", Calendar.getInstance().getTimeInMillis());
		    values.put("status", 0);
		    values.put("uptime_id", uptimeId);
		    beepId = db.insert(getTableName(), null, values);
		    db.close();
		}
		
		return beepId;
	}
	
	public boolean updateStatus(long beepId, int status) {
		int numRows = 0;
		
		if (beepId != 0L) {
			SQLiteDatabase db = getDb();
			ContentValues values = new ContentValues();
			values.put("status", status);
			values.put("updated", Calendar.getInstance().getTimeInMillis());
			numRows = db.update(getTableName(), values, "_id=?", new String[] { String.valueOf(beepId) });
			db.close();
		}
	
		return numRows == 1;
	}
	
	public boolean receivedScheduledBeep(long beepId, long timestamp) {
		int numRows = 0;
		
		if (beepId != 0L) {
			SQLiteDatabase db = getDb();
			ContentValues values = new ContentValues();
			values.put("received", timestamp);
			numRows = db.update(getTableName(), values, "_id=?", new String[] { String.valueOf(beepId) });
			db.close();
		}
	
		return numRows == 1;
	}
	
	public int getStatus(long beepId) {
		int status = 0;
		
		if (beepId != 0L) {
			SQLiteDatabase db = getDb();
			Cursor cursor = db.query(getTableName(), new String[] {"status"},
					"_id=?", new String[] { String.valueOf(beepId) }, null, null, null);
			
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				status = cursor.getInt(0);
				cursor.close();
			}
			db.close();
		}
	
		return status;
	}
	
	public boolean isExpired(long beepId) {
		boolean expired = false;
		
		if (beepId != 0L) {
			SQLiteDatabase db = getDb();
			Cursor cursor = db.query(getTableName(), new String[] {"timestamp"},
					"_id=?", new String[] { String.valueOf(beepId) }, null, null, null);
			
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				long timestamp = cursor.getLong(0);
				
				if ((Calendar.getInstance().getTimeInMillis() - timestamp) >= 60000) { //difference 1 min
					expired = true;
				}
				cursor.close();
			}
			db.close();
		}
	
		return expired;
	}
	
	public int getNumLastSubsequentCancelledBeeps() {
		int count = 0;
		
		SQLiteDatabase db = getDb();
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar today = new GregorianCalendar(year, month, day);
		long startOfDay = today.getTimeInMillis();
		today.roll(Calendar.DAY_OF_MONTH, true);
		long endOfDay = today.getTimeInMillis();
		
		Cursor cursor = db.query(getTableName(), new String[] {"status"}, "timestamp between ? and ?",
				new String[] { String.valueOf(startOfDay), String.valueOf(endOfDay) }, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToLast();
			do {
				if (cursor.getInt(0) != 0) {
					count += 1;
				}
				else {
					break;
				}
			} while (cursor.moveToPrevious());
			
			cursor.close();
		}
		db.close();
		
		return count;
	}

}
