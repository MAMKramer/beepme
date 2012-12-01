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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.glanznig.beepme.helper.TimerProfile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UptimeTable extends StorageHandler {
	
	private static final String TAG = "UptimeTable";
	
	private static final String TBL_NAME = "uptime";
	private static final String TBL_CREATE = 
			"CREATE TABLE " + TBL_NAME + " (" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"start INTEGER NOT NULL UNIQUE, " +
			"end INTEGER UNIQUE" +
			")";
	
	public UptimeTable(Context ctx) {
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
	
	public long startUptime(Date start) {
		if (start != null) {
			SQLiteDatabase db = getDb();
			ContentValues values = new ContentValues();
			values.put("start", start.getTime());
			
			long id = db.insert(getTableName(), null, values);
			db.close();
			
			return id;
		}
	
		return 0L;
	}
	
	public boolean endUptime(long uptimeId, Date end) {
		int numRows = 0;
		long startTime = 0L;
		
		if (uptimeId != 0L && end != null) {
			SQLiteDatabase db = getDb();
			
			Cursor cursor = db.query(getTableName(), new String[] { "start" },
					"_id = ?", new String[] { String.valueOf(uptimeId) }, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				startTime = cursor.getLong(0);
				cursor.close();
			}
			
			//remove very short uptimes from statistics
			if (startTime != 0L && end.getTime() - startTime > TimerProfile.MIN_UPTIME_DURATION * 1000) {
				ContentValues values = new ContentValues();
				values.put("end", end.getTime());
				numRows = db.update(getTableName(), values, "_id=?", new String[] { String.valueOf(uptimeId) });
			}
			else if (startTime != 0L) {
				numRows = db.delete(getTableName(), "_id = ?", new String[] { String.valueOf(uptimeId) });
				if (numRows == 1) {
					db.delete(ScheduledBeepTable.getTableName(), "uptime_id = ?", new String[] { String.valueOf(uptimeId) });
				}
			}
		    db.close();
		}
		
		return numRows == 1 ? true : false;
	}
	
	public long getUptimeDurToday() {
		return (long)getAvgUpDurToday("dur");
	}
	
	public int getUptimeCountToday() {
		return (int)getAvgUpDurToday("cnt");
	}
	
	public double getAvgUptimeDurToday() {
		return getAvgUpDurToday("avg");
	}
	
	private double getAvgUpDurToday(String returnType) {
		long duration = 0L;
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
		
		Cursor cursor = db.query(getTableName(), new String[] { "start", "end" },
				"start between ? and ?", new String[] { String.valueOf(startOfDay), String.valueOf(endOfDay) }, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				if (!cursor.isNull(0) && !cursor.isNull(1)) {
					count += 1;
					duration += cursor.getLong(1) - cursor.getLong(0);
				}
				//provided that there were no force closes, a missing end time as last row should
				//indicate the currently running uptime interval, include it with end time "now".
				//if the currently running uptime interval's duration is larger than TimerProfile.MIN_UPTIME_DURATION
				else if (cursor.isNull(1) && cursor.isLast()) {
					long nowTime = new Date().getTime();
					if (nowTime - cursor.getLong(0) > TimerProfile.MIN_UPTIME_DURATION * 1000) {
						count += 1;
						duration += nowTime - cursor.getLong(0);
					}
				}
			}
			while (cursor.moveToNext());
			cursor.close();
		}
		db.close();
		
		//transform from milliseconds to seconds
		duration = duration / 1000;
		
		if (returnType.equals("avg")) {
			return duration/count;
		}
		else if (returnType.equals("dur")) {
			return duration;
		}
		else if (returnType.equals("cnt")) {
			return count;
		}
		else {
			return 0;
		}
	}

}
